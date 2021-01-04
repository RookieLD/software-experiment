package com.github.coerx.qarchiver.gui;

import com.github.coerx.qarchiver.gui.common.PackAndCompressUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.coerx.qarchiver.common.Constants.*;

@Slf4j
public class GuiLanucher {

    public static void main(String[] args) {
        File dir = new File(TEMP_DIR_PATH);
        dir.mkdirs();
        //System.out.println(dir.mkdir());

        if (args == null || args.length == 0) {
            log.info("Launching the GUI without parameters");
            App.main(args);
        } else {
            cmd(args);
        }
    }

    private static void cmd(String[] args) {
        log.info("Launching the cli, parameters={}", Arrays.toString(args));
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption(Option.builder("c")
                .longOpt("compress")
                .desc("压缩模式，压缩文件格式为mytar.myz或者mytar.myz.aes，其中mytar.myz.aes支持加密")
                .build());
        options.addOption(Option.builder("d")
                .longOpt("decompress")
                .desc("解压模式")
                .build());
        options.addOption(Option.builder("s")
                .longOpt("source")
                .hasArg()
                .desc("-s path[,path[... path] 源文件或者源文件夹，如果有多个用,分隔。压缩时指定被压缩的文件和文件夹路径，解压时指定压缩文件路径")
                .build());
        options.addOption(Option.builder("t")
                .longOpt("target")
                .desc("-t path 目标文件或者目标文件夹。压缩模式时指定生成的压缩文件路径，解压时指定解压的文件夹路径")
                .hasArg()
                .build());
        options.addOption(Option.builder("p")
                .longOpt("password")
                .desc("密码，压缩文件类型必须为mytar.myz.aes")
                .hasArg()
                .build());
        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("See the help.")
                .build());
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("h")) {
                formatter.printHelp("指令格式", options);
                return ;
            }
            if (!line.hasOption("c") && !line.hasOption("d")) {
                formatter.printHelp("缺少-c或-t参数", options);
                return;
            }
            if (!line.hasOption("t") || !line.hasOption("s")) {
                formatter.printHelp("缺少-t或-s参数", options);
                return;
            }
            if (line.hasOption("c")) {
                List<File> fileList = new ArrayList<>();
                String source = line.getOptionValue("s");
                String target = line.getOptionValue("t");
                if (!target.endsWith(FILE_TYPE_1) && !target.endsWith(FILE_TYPE_1_ENCRIPTION)) {
                    formatter.printHelp("生成的压缩文件格式只支持mytar.myz或者mytar.myz.aes", options);
                    return;
                }
                for (String str : source.split(",")) {
                    File file = new File(str);
                    if (!file.exists()) {
                        formatter.printHelp("文件路径不存在,file=" + file, options);
                        return;
                    }
                    File canonicalFile = file.getCanonicalFile();
                    fileList.add(canonicalFile);
                }
                File targetFile = new File(target);
                if (target.endsWith(FILE_TYPE_1_ENCRIPTION)) {
                    if (!line.hasOption("p")) {
                        formatter.printHelp("需要密码", options);
                        return;
                    }
                    String password = line.getOptionValue("p");
                    PackAndCompressUtil.packAndCompressAndEncrypt(fileList, targetFile, password.toCharArray());
                } else {
                    PackAndCompressUtil.packAndCompress(fileList, targetFile);
                }

            } else if (line.hasOption("d")) {
                String source = line.getOptionValue("s");
                String target = line.getOptionValue("t");

                File compressdFile = new File(source);
                if (!compressdFile.exists()) {
                    formatter.printHelp("文件路径:" + source + ",压缩文件不存在", options);
                    return;
                }
                File rootDir = new File(target);
                if (!rootDir.exists() || !rootDir.isDirectory()) {
                    formatter.printHelp("文件夹路径:" + target + ",目标文件夹不存在", options);
                    return;
                }
                if (source.endsWith(FILE_TYPE_1_ENCRIPTION)) {
                    if (!line.hasOption("p")) {
                        formatter.printHelp("需要密码", options);
                        return;
                    }
                    String password = line.getOptionValue("p");
                    PackAndCompressUtil.decryptAndDeCompressAndUnpack(compressdFile,rootDir,password.toCharArray());
                } else {
                    PackAndCompressUtil.deCompressAndUnpack(compressdFile.getAbsolutePath(), rootDir.getAbsolutePath());
                }
            }
        } catch ( ParseException parseException) {
            formatter.printHelp("未识别的指令参数", options);
        } catch (Exception e){
            log.error("出现错误",e);
        }
    }
}
