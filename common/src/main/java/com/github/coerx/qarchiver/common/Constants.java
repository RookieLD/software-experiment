package com.github.coerx.qarchiver.common;

import java.io.File;

public class Constants {

    public static final String TEST_DIR = "C:\\Users\\17523\\Desktop\\compress-test\\tmp\\qarchiver";
    public static final File TEST_DIR_FILE = new File(TEST_DIR);
    public static final String TEST_DIR_PATH = "/tmp/qarchiver";

    public static final String TEMP_DIR = "C:\\Users\\17523\\Desktop\\compress-test\\tmp\\qarchiver";
    public static final String TEMP_DIR_PATH = "C:\\Users\\17523\\Desktop\\compress-test\\tmp\\qarchiver";
    public static final File TEMP_DIR_FILE = new File(TEMP_DIR);

    public static final String USER_HOME_PATH = System.getProperty("user.home");
    public static final File USER_HOME_FILE = new File(USER_HOME_PATH);

    //todo
    public static final String BAIDU_NETDISK_UPLOAD_DIR = "/apps/qarchiver";

    public static final String CONFIG_DIR_PATH = USER_HOME_PATH+"C:\\Users\\17523\\Desktop\\compress-test\\.config\\qarchiver";
    public static final File CONFIG_DIR_FILE = new File(CONFIG_DIR_PATH);

    public static final String SCRIPT_PATH = CONFIG_DIR_PATH+"\\script";
    public static final String CRON_TASK_CREATION_SCRIPT_PATH = SCRIPT_PATH+"\\cronTaskCreation.sh";

    public static final String APPLICATION_HOME = USER_HOME_PATH+"/.local/share/qarchiver";

    //todo need change
    public static final String APPLICATION_EXECUTABLE_JAR_PATH = "/home/coer/project/qarchiver/gui/target/gui-1.0-SNAPSHOT.jar";
    public static final String APPLICATION_CONSOLE_OUTPUT_REDIRECT_PATH = "C:\\Users\\17523\\Desktop\\compress-test\\tmp\\qarchiver_console.txt";

    public static final String FILE_TYPE_1 = "mytar.myz";
    public static final String FILE_TYPE_1_ENCRIPTION = "mytar.myz.aes";
}
