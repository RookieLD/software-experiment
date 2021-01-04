package com.github.coerx.qarchiver.core.netdisk.baidupan;

import com.github.coerx.qarchiver.common.Constants;
import com.github.coerx.qarchiver.core.netdisk.baidupan.dispatch.BaiduWangPanUploadCallback;
import com.github.coerx.qarchiver.core.netdisk.baidupan.response.CreateResponse;
import com.github.coerx.qarchiver.core.netdisk.baidupan.response.PreCreateResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Disabled
public class BaiduPanClientTest {

    static BaiduPanClient baiduPanClient;
    Random random = new Random();

    @BeforeAll
    public static void init() {
        Constants.TEST_DIR_FILE.mkdirs();
        String baiduWangPanAccessToken = "123.20cb2898efc473179641f0b345078d4e.Y72_CXqL96NZ2hTfIbra2HxJsuoum4cp62sHidY.VwzkFA";
        String bduss = "HlwZWZTc1J6RjBZZjlDZnVFck5hR1UxaTNiVTZ-YjFZU0w5TWJJSmo2Wi1SQXhnRVFBQUFBJCQAAAAAAAAAAAEAAADKMnhTY29lcngAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAH635F9-t-RfO";
        baiduPanClient = new BaiduPanClient(baiduWangPanAccessToken, bduss);
    }

    @Test
    public void preUploadTest() throws IOException, ExecutionException, InterruptedException {
        int bytesSize = random.nextInt(1024 * 256) + 1;
        byte[] bytes = new byte[2];
        random.nextBytes(bytes);
        File file = new File(Constants.TEST_DIR + "/fileUploadTest_" + RandomStringUtils.randomAlphanumeric(10));
        file.createNewFile();
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
        PreCreateResponse preCreateResponse = baiduPanClient.precreate("/apps/qarchiver/" + file.getName(), file, new BaiduWangPanUploadCallback())
                .get();
        System.out.println(preCreateResponse);
        Assertions.assertEquals(0, preCreateResponse.getErrno());
    }

    @Test
    public void fileUploadTest() throws IOException, ExecutionException, InterruptedException {
        File file2 = new File("/home/coer/tmp/gui-1.0-SNAPSHOT.jar");
        CreateResponse createResponse = baiduPanClient.fileUpload("/apps/qarchiver/" + file2.getName(), file2, new BaiduWangPanUploadCallback()).get();
        System.out.println(createResponse);
    }

    @Test
    public void sliceUploadTest() throws ExecutionException, InterruptedException {
        File file2 = new File("/home/coer/tmp/some.txt");
        String uploadid = "N1-MTEwLjE4NC4xNzkuMTc4OjE2MDg5NjIxMDM6ODM2Mjg2NTg5Njg4NzQ1MTcyMA==";
        LinkedList<String> list = new LinkedList<>();
        list.add("0");
        List<String> strings = baiduPanClient.sliceUploadSequentially("/apps/qarchiver/" + file2.getName(), file2, uploadid, list, new BaiduWangPanUploadCallback())
                .get();
        System.out.println(strings);
    }

    @Test
    public void fileCreate() throws ExecutionException, InterruptedException {
        String uploadId = "N1-MTEwLjE4NC4xNzkuMTc4OjE2MDg5MTY3OTQ6ODM1MDcwMzQzMTU5Njg2ODc0OQ==";
        List<String> blockList = List.of("6f5902ac237024bdd0c176cb93063dc4");
        CompletableFuture<CreateResponse> file = baiduPanClient.createFile("/apps/qarchiver/test.txt", new File("/home/coer/tmp/test.txt"), uploadId, blockList, new BaiduWangPanUploadCallback());
        System.out.println(file.get());
    }

}
