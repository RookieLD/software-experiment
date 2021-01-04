package com.github.coerx.qarchiver.core.test;

import com.github.coerx.qarchiver.core.netdisk.baidupan.response.PreCreateResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CommonTest {
    @Test
    public void test() {
        List<String> stringList = List.of("ff", "aa", "哈哈哈");
        String json = "{\"path\":\"/apps/qarchiver/fileUploadTest_SJTW1gkNLZ\",\"uploadid\":\"N1-MTEwLjE4NC4xNzkuMTc4OjE2MDg4ODgwMjE6ODM0Mjk3OTYzOTUxNDA5Mjg3Mw==\",\"return_type\":1,\"block_list\":[],\"errno\":0,\"request_id\":8342979639514092873}";
        Gson gson = new GsonBuilder().create();
        PreCreateResponse map = gson.fromJson(json, PreCreateResponse.class);
        System.out.println(map);
        System.out.println(gson.toJson(stringList));
    }
}
