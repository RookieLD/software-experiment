package com.github.coerx.qarchiver.gui.test;

import com.github.coerx.qarchiver.common.Constants;
import com.github.coerx.qarchiver.gui.App;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

public class runScriptForCronFileCreationTest {
    @Test
    public void test(){
        File targetFile = new File(Constants.TEST_DIR+"/test.mytar.myz");
        File file = new File(Constants.TEST_DIR+"/testFile1.txt");
        File file2 = new File(Constants.TEST_DIR+"/testFile2.txt");
        File file3 = new File(Constants.TEST_DIR+"/testFile3.txt");
        List<File> fileList = List.of(file, file2, file3);
        App.runScriptForCronFileCreation(targetFile,fileList,"1234",false,"* * * * *");
    }
}
