package com.github.coerx.qarchiver.core.netdisk.baidupan.service;


import com.github.coerx.qarchiver.core.netdisk.baidupan.response.CreateResponse;
import com.github.coerx.qarchiver.core.netdisk.baidupan.response.PreCreateResponse;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.Map;

/**
 * base url 是http://pan.baidu.com/
 */
public interface BaiduNetDiskServiceOfPan {
    @GET("rest/2.0/xpan/nas?method=uinfo")
    Call<Map<String, String>> userInfo(@Query(value = "access_token", encoded = true) String accessToken);

    /**
     * <a href="https://pan.baidu.com/union/document/basic#%E9%A2%84%E4%B8%8A%E4%BC%A0">https://pan.baidu.com/union/document/basic#%E9%A2%84%E4%B8%8A%E4%BC%A0</a>
     *
     * @param accessToken
     * @param requestBody
     * @return
     */
    @POST("rest/2.0/xpan/file?method=precreate")
    Call<PreCreateResponse> preCreate(@Query(value = "access_token", encoded = true) String accessToken, @Body RequestBody requestBody);

    @POST("rest/2.0/xpan/file?method=create")
    Call<CreateResponse> create(@Query(value = "access_token", encoded = true) String accessToken, @Body RequestBody requestBody);
}
