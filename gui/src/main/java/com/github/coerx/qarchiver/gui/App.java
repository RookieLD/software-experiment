package com.github.coerx.qarchiver.gui;

import com.github.coerx.qarchiver.common.Constants;
import com.github.coerx.qarchiver.common.util.ByteUtil;
import com.github.coerx.qarchiver.common.util.FileUtil;
import com.github.coerx.qarchiver.common.util.StringUtil;
import com.github.coerx.qarchiver.core.netdisk.baidupan.BaiduPanClient;
import com.github.coerx.qarchiver.core.netdisk.baidupan.dispatch.BaiduWangPanUploadCallback;
import com.github.coerx.qarchiver.core.netdisk.baidupan.response.CreateResponse;
import com.github.coerx.qarchiver.gui.common.ClickableMenu;
import com.github.coerx.qarchiver.gui.common.PackAndCompressUtil;
import com.github.coerx.qarchiver.gui.controller.BaiduNetdiskUploadFileWindowBoxController;
import com.github.coerx.qarchiver.gui.controller.CompressWindowController;
import com.github.coerx.qarchiver.gui.controller.DeCompressBoxController;
import com.github.coerx.qarchiver.gui.controller.MainWindowBoxController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.github.coerx.qarchiver.common.Constants.TEMP_DIR_PATH;

/**
 * JavaFX App
 */
@Slf4j
public class App extends Application {

    static final String ENCRYPT_FILE_TYPE = ".mytar.myz.aes";
    static final String BAIDUWANGPAN_LOGIN_SUCCESS_URL_PREFIX = "https://openapi.baidu.com/oauth/2.0/login_success";
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(1);

    FileChooser fileChooser = new FileChooser();
    DirectoryChooser directoryChooser = new DirectoryChooser();
    Alert alert = new Alert(Alert.AlertType.ERROR);
    Alert progressWindow = new Alert(Alert.AlertType.INFORMATION);
    Stage webStage = new Stage();
    WebView webView = new WebView();
    WebEngine engine = webView.getEngine();
    private File archiveFile;
    private List<File> needCompressFiles = new LinkedList<>();
    /**
     * 压缩窗口关闭是否是因为点击了取消按钮
     */
    private boolean compressWindowCloseForCancelButton = true;
    /**
     * state状态
     * 0 -> 初始状态
     * 1 -> 压缩文件
     * 2 -> 创建定时任务
     */
    private int state = 0;
    private CompressWindowController compressWindowController;
    private String baiduWangPanAccessToken = "123.20cb2898efc473179641f0b345078d4e.Y72_CXqL96NZ2hTfIbra2HxJsuoum4cp62sHidY.VwzkFA";
    private String bduss = "HlwZWZTc1J6RjBZZjlDZnVFck5hR1UxaTNiVTZ-YjFZU0w5TWJJSmo2Wi1SQXhnRVFBQUFBJCQAAAAAAAAAAAEAAADKMnhTY29lcngAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAH635F9-t-RfO";
    private BaiduPanClient baiduPanClient;

    public static void main(String[] args) {
        launch(args);
    }

    //todo 用fxml重写主窗口
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Q Archiver - 文件压缩与解压工具");
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                threadPool.shutdown();
                Platform.exit();
                System.exit(0);

            }
        });
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        //web浏览器
        webStage.initOwner(primaryStage);
        webStage.initModality(Modality.WINDOW_MODAL);
        Scene webScene = new Scene(new VBox(webView), 960, 600);
        webStage.setScene(webScene);

       /* //menuBar
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-border-color: #000000");
        menuBar.setStyle("-fx-border-width: 0 0 2px 0");*/

        //主window的box
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(App.class.getClassLoader().getResource("mainWindowBox.fxml"));
        VBox mainVBox = loader.load();
        MainWindowBoxController mainWindowBoxController = loader.getController();


        mainWindowBoxController.filenameColumn
                .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAbsolutePath()));
        mainWindowBoxController.filenameColumn
                .prefWidthProperty().bind(mainWindowBoxController.sourceFilesTable.widthProperty().multiply(0.8));

        mainWindowBoxController.filesizeColumn
                .setCellValueFactory(c -> new SimpleStringProperty(ByteUtil.humanReadableByteCountBin(c.getValue().length())));
        mainWindowBoxController.filesizeColumn
                .prefWidthProperty().bind(mainWindowBoxController.sourceFilesTable.widthProperty().multiply(0.1));

        mainWindowBoxController.filetypeColumn
                .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isFile() ? "文件" : "文件夹"));
        mainWindowBoxController.filetypeColumn
                .prefWidthProperty().bind(mainWindowBoxController.sourceFilesTable.widthProperty().multiply(0.1));

        //主体VBox和Scene
        Scene scene = new Scene(mainVBox, 800, 400);
        primaryStage.setScene(scene);

        alert.initOwner(primaryStage);
        progressWindow.initOwner(primaryStage);

        MenuBar menuBar = mainWindowBoxController.menubar;
        //创建压缩文件的菜单项
        Menu compressMenu = new ClickableMenu("新建压缩文件");
        menuBar.getMenus().add(compressMenu);

        //创建解压文件的菜单项
        Menu decompressMenu = new ClickableMenu("解压文件");
        menuBar.getMenus().addAll(decompressMenu);

        //百度网盘上传文件的菜单项
        Menu baiduNetdiskUploadMenu = new ClickableMenu("百度网盘上传文件");
        menuBar.getMenus().add(baiduNetdiskUploadMenu);

        //百度网盘登录的菜单项
        Menu baiduwangpanLoginMenu = new ClickableMenu("登录百度网盘");
        menuBar.getMenus().add(baiduwangpanLoginMenu);

        //测试的菜单项
        Menu testMenu = new ClickableMenu("测试按钮");
        menuBar.getMenus().add(testMenu);

        //创建压缩文件的窗口
        Stage createArchiverWindow = getCompressWindow(primaryStage);

        //创建解压文件的窗口
        Stage decompressWindow = getDecompressWindow(primaryStage);

        //百度网盘上传文件窗口
        Stage baiduNetdiskUploadFileWindow = getBaiduNetdiskUploadFileWindow(primaryStage);

        //在最后添加各种监听器
        //测试 菜单项
        testMenu.setOnAction(e -> {
            System.out.println(baiduWangPanAccessToken);
            baiduPanClient = new BaiduPanClient(baiduWangPanAccessToken, bduss);
            threadPool.execute(() -> {
                try {
                    String name = baiduPanClient.getNetdiskName();
                    System.out.println(name);
                    if (name != null) {
                        Platform.runLater(() -> mainWindowBoxController.loginStatus.setText("百度网盘已登录,用户名:" + name));
                    }
                } catch (IOException ioException) {
                    log.error("获取百度网盘用户名失败", ioException);
                }
            });
        });

        //解压 菜单项
        decompressMenu.setOnAction(e -> {
            if (decompressWindow != null) {
                decompressWindow.showAndWait();
            }
        });

        //百度网盘上传文件菜单项
        baiduNetdiskUploadMenu.setOnAction(e -> {
            if (baiduPanClient == null) {
                alert.setTitle("错误");
                alert.setHeaderText("还没有登录百度网盘");
                alert.setContentText("");
                alert.showAndWait();
                return;
            }
            if (baiduNetdiskUploadFileWindow != null) {
                baiduNetdiskUploadFileWindow.showAndWait();
            }
        });

        //百度网盘登录 菜单项
        baiduwangpanLoginMenu.setOnAction(e -> {
            engine.load("http://openapi.baidu.com/oauth/2.0/authorize?client_id=1Zu5cweFMeFUkfQ6Kyaolkwg&response_type=token&redirect_uri=oob&scope=basic,netdisk");
            engine.getLoadWorker().stateProperty().addListener(((observable, oldValue, newValue) -> {
                if (newValue != Worker.State.SUCCEEDED) {
                    return;
                }
                String location = engine.getLocation();
                if (location.startsWith(BAIDUWANGPAN_LOGIN_SUCCESS_URL_PREFIX)) {
                    String baiduWangPanAccessToken = null;
                    String[] bduss = new String[1];
                    for (String kv : location.split("#")[1].split("&")) {
                        if (kv.startsWith("access_token")) {
                            baiduWangPanAccessToken = kv.split("=")[1];
                            break;
                        }
                    }
                    //百度云cookie BDUSS
                    try {
                        CookieHandler.getDefault().get(URI.create(location), new HashMap<>())
                                .forEach((url, cookieList) -> {
                                    log.info("百度云cookie url={},cookieList={}", url, cookieList);
                                    cookieList.forEach(cookies -> {
                                        for (String cookie : cookies.split(";")) {
                                            if (cookie.split("=")[0].trim().equals("BDUSS")) {
                                                bduss[0] = cookie.split("=")[1].trim();
                                            }
                                        }
                                    });
                                });
                    } catch (IOException ioException) {
                        log.error("获取百度云cookie bduss失败", ioException);
                    }
                    log.info("百度网盘 access_token={}  BDUSS={}", baiduWangPanAccessToken, bduss[0]);
                    if (baiduWangPanAccessToken != null && bduss[0] != null) {
                        baiduPanClient = new BaiduPanClient(baiduWangPanAccessToken, bduss[0]);
                        threadPool.execute(() -> {
                            try {
                                String name = baiduPanClient.getNetdiskName();
                                if (name != null) {
                                    Platform.runLater(() -> {
                                        mainWindowBoxController.loginStatus.setText("百度网盘已登录，用户:" + name);
                                        menuBar.getMenus().remove(baiduwangpanLoginMenu);
                                    });
                                }
                            } catch (IOException ioException) {
                                log.error("获取百度网盘用户名失败", ioException);
                            }
                        });
                    } else {
                        log.error("百度网盘登录失败");
                    }
                    webStage.close();
                }
            }));
            webStage.showAndWait();
        });

        //创建压缩文件 菜单项
        compressMenu.setOnAction(e -> {
            compressWindowCloseForCancelButton = true;
            if (createArchiverWindow != null) {
                createArchiverWindow.showAndWait();
            }
            //因为点击了取消按钮，不改变状态
            if (compressWindowCloseForCancelButton) return;
            if (state == 1) {
                mainWindowBoxController.addFileButton.setDisable(false);
                mainWindowBoxController.addDirButton.setDisable(false);
                mainWindowBoxController.startCompressButton.setDisable(false);
                mainWindowBoxController.targetFilePathTextField.setDisable(false);
                mainWindowBoxController.sourceFilesTable.setDisable(false);
                mainWindowBoxController.createCronTaskButton.setDisable(true);
                mainWindowBoxController.targetFilePathTextField.setText(archiveFile.getAbsolutePath());
                mainWindowBoxController.sourceFilesTable.getItems().clear();
            } else if (state == 2) {
                mainWindowBoxController.addFileButton.setDisable(false);
                mainWindowBoxController.addDirButton.setDisable(false);
                mainWindowBoxController.startCompressButton.setDisable(true);
                mainWindowBoxController.targetFilePathTextField.setDisable(false);
                mainWindowBoxController.sourceFilesTable.setDisable(false);
                mainWindowBoxController.createCronTaskButton.setDisable(false);
                mainWindowBoxController.targetFilePathTextField.setText(archiveFile.getAbsolutePath());
                mainWindowBoxController.sourceFilesTable.getItems().clear();
            }
        });

        //压缩时添加压缩文件的按钮
        mainWindowBoxController.addFileButton.setOnAction(e -> {
            if (!(state == 1 || state == 2)) return;
            List<File> fileList = fileChooser.showOpenMultipleDialog(primaryStage);
            if (fileList == null) {
                return;
            }
            needCompressFiles.addAll(fileList);
            mainWindowBoxController.sourceFilesTable.getItems().addAll(fileList);
        });
        //压缩时添加压缩文件夹的按钮
        mainWindowBoxController.addDirButton.setOnAction(e -> {
            if (!(state == 1 || state == 2)) return;
            File dir = directoryChooser.showDialog(primaryStage);
            if (dir == null) return;
            needCompressFiles.add(dir);
            mainWindowBoxController.sourceFilesTable.getItems().addAll(dir);
        });


        //开始压缩按钮
        mainWindowBoxController.startCompressButton.setOnAction(e -> {
            if (!(state == 1)) return;
            if (archiveFile == null || !archiveFile.exists() || !archiveFile.isFile()) {
                alert.setTitle("错误");
                alert.setHeaderText("还没有建立压缩文件");
                alert.setContentText("");
                alert.showAndWait();
                return;
            }
            if (needCompressFiles.isEmpty()) {
                alert.setTitle("错误");
                alert.setHeaderText("没有添加需要压缩的文件和文件夹");
                alert.setContentText("");
                alert.showAndWait();
                return;
            }

            progressWindow.setTitle("压缩进度");
            progressWindow.setHeaderText("压缩中......");

            List<File> tmpNeedCompressFiles = needCompressFiles;

            //todo
            File tmpArchiveFile = archiveFile;

            String destination = archiveFile.getAbsolutePath();
            boolean needCheck = compressWindowController.checkGeneratedFileCheckBox.isSelected();
            String password = compressWindowController.textFieldForPassword.getText();
            threadPool.execute(() -> {
                try {
                    Platform.runLater(progressWindow::showAndWait);
                    if (!password.isBlank()) {
                        //System.out.println(password);
                        PackAndCompressUtil.packAndCompressAndEncrypt(tmpNeedCompressFiles, tmpArchiveFile, password.toCharArray());
                    } else {
                        PackAndCompressUtil.packAndCompress(tmpNeedCompressFiles, tmpArchiveFile);
                    }
                } catch (Exception ex) {
                    log.error("压缩过程发生错误,needCompressFiles={},archiveFile={}", tmpNeedCompressFiles, tmpArchiveFile, ex);
                    Platform.runLater(() -> progressWindow.setHeaderText("压缩出现错误"));
                    return;
                }

                if (needCheck) {
                    if (checkGeneratedCompressdFile(tmpNeedCompressFiles, tmpArchiveFile, password)) {
                        Platform.runLater(() -> progressWindow.setHeaderText("压缩完成，压缩文件内容正确"));
                    } else {
                        Platform.runLater(() -> progressWindow.setHeaderText("压缩失败,压缩文件内容受损"));
                        tmpArchiveFile.delete();
                    }
                } else {
                    Platform.runLater(() -> progressWindow.setHeaderText("压缩完成"));
                }
            });

            //不能needCompressFiles.clear()，而应该new一个新的，因为前面线程池的任务需要使用needCompressFiles指向的对象
            needCompressFiles = new LinkedList<>();
            archiveFile = null;
            compressWindowController.backToInitialState();
            mainWindowBoxController.backInitialState();
            state = 0;
        });



        //创建定时任务按钮
        mainWindowBoxController.createCronTaskButton.setOnAction(event -> {
            if (state != 2) {
                return;
            }
            if (archiveFile == null || !archiveFile.exists() || !archiveFile.isFile()) {
                alert.setTitle("错误");
                alert.setHeaderText("还没有建立压缩文件");
                alert.setContentText("");
                alert.showAndWait();
                return;
            }

            if (needCompressFiles.isEmpty()) {
                alert.setTitle("错误");
                alert.setHeaderText("没有添加需要压缩的文件和文件夹");
                alert.setContentText("");
                alert.showAndWait();
                return;
            }

            if (archiveFile.exists()) {
                archiveFile.delete();
            }
            List<File> tmpNeedCompressFiles = needCompressFiles;
            File tmpArchiveFile = archiveFile;
            boolean needCheck = compressWindowController.checkGeneratedFileCheckBox.isSelected();
            String password = compressWindowController.textFieldForPassword.getText();
            String cronExpression = compressWindowController.cronExpression.getText().trim();

            if (runScriptForCronFileCreation(archiveFile, needCompressFiles,
                    password.isBlank() ? null : password, needCheck, cronExpression)) {
                progressWindow.setHeaderText("创建定时任务成功，cron文件路径为/etc/cron.d/qarchiver，如果需要移除定时任务删掉cron文件即可");
                progressWindow.showAndWait();
            } else {
                alert.setHeaderText("创建定时任务失败");
                alert.setContentText("");
                alert.showAndWait();
            }
            //不能needCompressFiles.clear()，而应该new一个新的，因为前面线程池的任务需要使用needCompressFiles指向的对象
            needCompressFiles = new LinkedList<>();
            archiveFile = null;
            compressWindowController.backToInitialState();
            mainWindowBoxController.backInitialState();
            state = 0;
        });

        primaryStage.show();
    }

    private Stage getCompressWindow(Stage primaryStage) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            Stage stage = new Stage();
            stage.initOwner(primaryStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("新建压缩文件");
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            File[] chooseDir = new File[]{new File(System.getProperty("user.home"))};
            //String fileType = ".mytar.myz";

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(App.class.getClassLoader().getResource("compressWindow.fxml"));
            VBox vBox = loader.load();
            compressWindowController = loader.getController();


            //origin 210, extend 350
            int originHeight = 220;
            int extendHeight = 410;
            Scene scene = new Scene(vBox, 500, originHeight);
            stage.setScene(scene);
            stage.setResizable(false);

            compressWindowController.fileTypeChoiceBox.getSelectionModel().selectFirst();

            compressWindowController.fileTypeChoiceBox.setOnAction(e -> {
                if (compressWindowController.fileTypeChoiceBox.getSelectionModel()
                        .getSelectedItem().equals(".mytar.myz.aes")) {
                    compressWindowController.textFieldForPassword.setDisable(false);
                } else {
                    compressWindowController.textFieldForPassword.setText("");
                    compressWindowController.textFieldForPassword.setDisable(true);
                }
            });
            compressWindowController.cancelButton.setOnAction(e -> {
                compressWindowCloseForCancelButton = true;
                stage.close();
            });

            compressWindowController.createButton.setOnAction(e -> {
                //如果是定时任务，检查cron表达式
                if (compressWindowController.cronTaskCheckBox.isSelected()
                        && !StringUtil.validateCronExpression(compressWindowController.cronExpression.getText().trim())) {
                    alert.setTitle("Error");
                    alert.setHeaderText("定时任务的cron表达式错误");
                    alert.setContentText("");
                    alert.showAndWait();
                    return;

                }
                String fileName = compressWindowController.textFieldForFilename.getText();
                if (fileName.isBlank()) {
                    alert.setTitle("Error");
                    alert.setHeaderText("错误,文件名为空");
                    alert.setContentText("");
                    alert.showAndWait();
                    return;
                }
                fileName = fileName.trim();
                //System.out.println(fileName);
                String fileType = compressWindowController.fileTypeChoiceBox.getSelectionModel().getSelectedItem();
                if (fileType.equals(ENCRYPT_FILE_TYPE) && compressWindowController.textFieldForPassword.getText().isBlank()) {
                    alert.setTitle("Error");
                    alert.setHeaderText("mytar.myz.aes文件类型需要密码");
                    alert.showAndWait();
                    return;
                }
                File file = new File(chooseDir[0].getAbsolutePath() + "\\" + fileName + fileType);

                if (file.exists()) {
                    alert.setTitle("Error");
                    alert.setHeaderText("错误,文件已存在");
                    alert.setContentText("文件路径: " + file.getAbsolutePath());
                    alert.showAndWait();
                    return;
                }

                boolean created = false;
                try {
                    created = file.createNewFile();
                } catch (IOException ioException) {
                    log.error("创建文件 {} 失败", file, ioException);
                }
                if (!created) {
                    alert.setTitle("Error");
                    alert.setHeaderText("错误,文件创建失败");
                    alert.setContentText("文件路径: " + file.getAbsolutePath());
                    alert.showAndWait();
                    return;
                }
                archiveFile = file;
                compressWindowCloseForCancelButton = false;

                //如果是定时任务，检查cron表达式
                if (compressWindowController.cronTaskCheckBox.isSelected()) {
                    state = 2;
                } else {
                    state = 1;
                }
                stage.close();
            });

            compressWindowController.choosePathButton.setOnAction(e -> {
                File pathDir = directoryChooser.showDialog(stage);
                if (pathDir == null) {
                    return;
                }
                chooseDir[0] = pathDir;
                compressWindowController.pathLabel.setText(pathDir.getAbsolutePath());
            });

            Double[] compressWindowDecorationHeight = new Double[1];
            compressWindowController.otherOpinion.setOnAction(e -> {
                if (compressWindowDecorationHeight[0] == null) {
                    compressWindowDecorationHeight[0] = stage.getHeight() - originHeight;
                }
                if (compressWindowController.otherOpinion.isSelected()) {
                    stage.setHeight(extendHeight + compressWindowDecorationHeight[0]);
                } else {
                    stage.setHeight(originHeight + compressWindowDecorationHeight[0]);
                }
            });
            stage.setOnShowing(e -> {
                compressWindowController.pathLabel.setText(System.getProperty("user.home"));
                compressWindowController.textFieldForFilename.setText("");
                chooseDir[0] = new File(System.getProperty("user.home"));
            });
            return stage;
        } catch (Exception e) {
            log.error("加载压缩窗口出错", e);
            return null;
        }
    }

    private Stage getDecompressWindow(Stage primaryStage) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            File[] sourceFile = new File[1];
            File[] targetDir = new File[1];
            Alert alert = new Alert(Alert.AlertType.ERROR);
            Alert progressWindow = new Alert(Alert.AlertType.INFORMATION);
            progressWindow.setTitle("解压进度");
            progressWindow.initOwner(primaryStage);

            Stage stage = new Stage();
            stage.initOwner(primaryStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.setTitle("解压文件");


            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(App.class.getClassLoader().getResource("decompressBox.fxml"));
            VBox vBox = loader.load();

            int originHeight = 130;
            int extendHeight = 180;
            Scene scene = new Scene(vBox, 500, originHeight);
            stage.setScene(scene);

            DeCompressBoxController deCompressBoxController = loader.getController();
            deCompressBoxController.cancelButton.setOnAction(e -> stage.close());

            Double[] decorationHeight = new Double[1];
            deCompressBoxController.chooseSouceFileButton.setOnAction(e -> {
                sourceFile[0] = fileChooser.showOpenDialog(stage);
                if (sourceFile[0] == null) return;
                deCompressBoxController.sourfilePathTextField.setText(sourceFile[0].getAbsolutePath());

                if (decorationHeight[0] == null) {
                    decorationHeight[0] = stage.getHeight() - originHeight;
                }

                if (sourceFile[0].getName().endsWith(ENCRYPT_FILE_TYPE)) {
                    stage.setHeight(extendHeight + decorationHeight[0]);
                } else {
                    stage.setHeight(originHeight + decorationHeight[0]);
                }
            });

            deCompressBoxController.chooseTargetDirButton.setOnAction(e -> {
                targetDir[0] = directoryChooser.showDialog(stage);
                if (targetDir[0] == null) return;
                deCompressBoxController.targetDirPathTextField.setText(targetDir[0].getAbsolutePath());
            });

            deCompressBoxController.decompressButton.setOnAction(e -> {
                if (sourceFile[0] == null || !sourceFile[0].exists() || !sourceFile[0].isFile()) {
                    alert.setTitle("错误");
                    alert.setHeaderText("压缩文件找不到或者指定路径不是文件");
                    alert.setContentText("文件路径:" + (sourceFile[0] == null ? "空" : sourceFile[0].getAbsolutePath()));
                    alert.showAndWait();
                    return;
                }

                if (targetDir[0] == null || !targetDir[0].exists() || !targetDir[0].isDirectory()) {
                    alert.setTitle("错误");
                    alert.setHeaderText("解压文件夹路径找不到或者指定路径不是文件夹");
                    alert.setContentText("文件夹路径:" + (sourceFile[0] == null ? "空" : sourceFile[0].getAbsolutePath()));
                    alert.showAndWait();
                    return;
                }
                String password = deCompressBoxController.passwordField.getText();
                boolean needPassword = sourceFile[0].getName().endsWith(ENCRYPT_FILE_TYPE);
                if (needPassword && password.isBlank()) {
                    alert.setTitle("错误");
                    alert.setHeaderText(sourceFile[0].getName() + " 需要密码");
                    alert.showAndWait();
                    return;
                }

                final File tmpSourceFile = sourceFile[0];
                final File tmpTargetDir = targetDir[0];
                progressWindow.setHeaderText("解压中......");
                threadPool.execute(() -> {
                    try {
                        Platform.runLater(stage::close);
                        Platform.runLater(progressWindow::showAndWait);
                        if (needPassword) {
                            PackAndCompressUtil.decryptAndDeCompressAndUnpack(tmpSourceFile, tmpTargetDir, password.toCharArray());
                        } else {
                            PackAndCompressUtil.deCompressAndUnpack(tmpSourceFile.getAbsolutePath(), tmpTargetDir.getAbsolutePath());
                        }
                    } catch (Exception e1) {
                        log.error("解压出错,sourceFile {},targetDir {}", tmpSourceFile, tmpTargetDir, e1);
                        Platform.runLater(() -> progressWindow.setHeaderText("解压出现错误"));
                        return;
                    }
                    Platform.runLater(() -> progressWindow.setHeaderText("解压完成"));
                });
            });

            return stage;
        } catch (Exception e) {
            log.error("加载解压窗口出错", e);
            return null;
        }
    }

    private boolean checkGeneratedCompressdFile(List<File> souceFiles, File targetFile, String password) {
        log.info("检查备份文件正确性 sourceFiles: {} ,targetFile:{}", souceFiles, targetFile);
        try {
            File rootDir = new File(TEMP_DIR_PATH + "\\temp_dir_" + RandomStringUtils.randomAlphabetic(10));
            if (!rootDir.mkdirs()) {
                log.error("创建临时文件夹失败 {}", rootDir.getAbsolutePath());
                return false;
            }
            if (!password.isBlank()) {
                PackAndCompressUtil.decryptAndDeCompressAndUnpack(targetFile, rootDir, password.toCharArray());
            } else {
                PackAndCompressUtil.deCompressAndUnpack(targetFile.getAbsolutePath(), rootDir.getAbsolutePath());
            }

            Map<String, File> fileMap = new HashMap<>();
            for (File file : souceFiles) {
                fileMap.put(file.getName() + (file.isDirectory() ? "\\" : ""), file);
            }

            for (File file : rootDir.listFiles()) {
                File souceFile = fileMap.remove(file.getName() + (file.isDirectory() ? "\\" : ""));
                if (souceFile == null) {
                    log.error("{} 在源文件集中找不到", file.getName());
                    return false;
                }
                if (file.isFile()) {
                    byte[] bytes1 = Files.readAllBytes(file.toPath());
                    byte[] bytes2 = Files.readAllBytes(souceFile.toPath());
                    if (!Arrays.equals(bytes1, bytes2)) {
                        log.error("{}和{}的文件内容不同", file, souceFile);
                        return false;
                    }
                } else if (!FileUtil.compareTwoDirTree(souceFile, file)) {
                    log.error("文件夹{} {} 内容不一致", souceFile, file);
                    return false;
                }
            }
            if (!fileMap.isEmpty()) {
                log.error("有的源文件在生成文件集中找不到 {} ", fileMap);
                return false;
            }
        } catch (Exception e) {
            log.error("备份数据正确性检查失败", e);
            return false;
        }
        return true;
    }

    private Stage getBaiduNetdiskUploadFileWindow(Stage primaryStage) {
        try {
            File[] chooseFile = new File[1];
            Stage stage = new Stage();
            stage.initOwner(primaryStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(App.class.getClassLoader().getResource("baiduNetdiskUploadFileWindowBox.fxml"));
            VBox vBox = loader.load();
            BaiduNetdiskUploadFileWindowBoxController baiduNetdiskUploadFileWindowBoxController = loader.getController();

            baiduNetdiskUploadFileWindowBoxController.cancelButton.setOnAction(e -> stage.close());
            baiduNetdiskUploadFileWindowBoxController.chooseFile.setOnAction(e -> {
                fileChooser.setInitialDirectory(Constants.USER_HOME_FILE);
                chooseFile[0] = fileChooser.showOpenDialog(stage);
                if (chooseFile[0] == null) return;
                baiduNetdiskUploadFileWindowBoxController.chooseFileTextField.setText(chooseFile[0].getAbsolutePath());
            });
            baiduNetdiskUploadFileWindowBoxController.uploadButton.setOnAction(e -> {
                if (chooseFile[0] == null) {
                    alert.setTitle("错误");
                    alert.setHeaderText("选择要上传的文件");
                    alert.setContentText("");
                    alert.showAndWait();
                    return;
                }
                String fileName = chooseFile[0].getName();
                BaiduWangPanUploadCallback baiduWangPanUploadCallback = new BaiduWangPanUploadCallback();
                threadPool.execute(() -> {
                    try {
                        log.info("上传文件 path={}", chooseFile[0].getAbsolutePath());
                        Platform.runLater(() -> {
                            progressWindow.setTitle("百度云上传文件");
                            progressWindow.setHeaderText(fileName + " 上传中.....");
                            progressWindow.showAndWait();
                        });
                        Platform.runLater(stage::close);
                        CompletableFuture<CreateResponse> future = baiduPanClient.fileUpload(Constants.BAIDU_NETDISK_UPLOAD_DIR + "\\" + chooseFile[0].getName(), chooseFile[0], baiduWangPanUploadCallback);
                        future.thenAcceptAsync(createResponse -> {
                            if (createResponse.getErrno() == 0) {
                                log.info("上传成功, path={}", chooseFile[0].getAbsolutePath());
                                Platform.runLater(() -> {
                                    //progressWindow.setTitle("百度云上传文件");
                                    progressWindow.setHeaderText(fileName + " 上传成功");
                                });
                            } else {
                                log.error("上传失败, path={}", chooseFile[0].getAbsolutePath());
                                Platform.runLater(() -> {
                                    //progressWindow.setTitle("百度云上传文件");
                                    progressWindow.setHeaderText(fileName + " 上传失败");
                                });
                            }
                        }).exceptionally((ex) -> {
                            log.error("上传失败, path={}", chooseFile[0].getAbsolutePath(), ex);
                            Platform.runLater(() -> {
                                //progressWindow.setTitle("百度云上传文件");
                                progressWindow.setHeaderText(fileName + " 上传失败");
                            });
                            return null;
                        });
                    } catch (IOException ioException) {
                        log.error("上传失败", ioException);
                        Platform.runLater(() -> {
                            //progressWindow.setTitle("百度云上传文件");
                            progressWindow.setHeaderText(fileName + " 上传失败");
                        });
                    }
                });

            });
            Scene scene = new Scene(vBox, 500, 170);
            stage.setScene(scene);

            return stage;
        } catch (IOException e) {
            log.error("加载百度网盘上传文件窗口出错", e);
        }
        return null;
    }

    /**
     * 创建cron定时任务，用于定时压缩
     * @param archiveFile
     * @param needCompressFiles
     * @param password
     * @param needCheck
     * @param cronExpression
     * @return
     */
    public static boolean runScriptForCronFileCreation(File archiveFile,
                                                 List<File> needCompressFiles,
                                                 String password,
                                                 boolean needCheck,
                                                 String cronExpression) {
        log.info("创建定时任务 archiverFile={},needCompressFiles={},needCheck={},cronExpression={}", archiveFile, needCompressFiles, needCheck, cronExpression);
        try {
            StringBuilder builder = new StringBuilder();
            for(File file : needCompressFiles){
                builder.append(file.getCanonicalPath()).append(",");
            }
            String souceFiles = builder.substring(0,builder.length()-1);

            builder = new StringBuilder();
            builder.append("java -jar").append(" ").append(Constants.APPLICATION_EXECUTABLE_JAR_PATH)
                    .append(" ").append("-c")
                    .append(" ").append("-t").append(" ").append(archiveFile.getCanonicalPath())
                    .append(" ").append("-s").append(" ").append(souceFiles);
            if(password != null){
                builder.append(" ").append("-p").append(" ").append(password);
            }
            builder.append(" ").append(">").append(Constants.APPLICATION_CONSOLE_OUTPUT_REDIRECT_PATH)
                    .append(" ").append("2>&1");

            String cronCommand = builder.toString();
            ProcessBuilder pb = new ProcessBuilder(Constants.CRON_TASK_CREATION_SCRIPT_PATH,
                    cronExpression, cronCommand);
            pb.directory(new File(Constants.SCRIPT_PATH));
            pb.inheritIO();
            Process p = pb.start();
            p.waitFor();
        } catch (Exception e) {
            log.error("创建定时任务出错", e);
            return false;
        }
        return true;
    }
}