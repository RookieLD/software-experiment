package com.github.coerx.qarchiver.gui.controller;

import javafx.scene.control.*;

import java.io.File;

public class MainWindowBoxController {
    public Button addFileButton;
    public Button startCompressButton;
    public TextField targetFilePathTextField;
    public TableView<File> sourceFilesTable;
    public Button addDirButton;
    public TableColumn<File, String> filenameColumn;
    public TableColumn<File, String> filesizeColumn;
    public TableColumn<File, String> filetypeColumn;
    /*public ClickableMenu compressMenu;
    public ClickableMenu decompressMenu;
    public ClickableMenu baiduwangpanMenu;*/
    public MenuBar menubar;
    public Label loginStatus;
    public Button createCronTaskButton;

    public void backInitialState(){
        addFileButton.setDisable(true);
        addDirButton.setDisable(true);
        startCompressButton.setDisable(true);
        createCronTaskButton.setDisable(true);
        sourceFilesTable.setDisable(true);
        sourceFilesTable.getItems().clear();
    }
}
