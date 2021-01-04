package com.github.coerx.qarchiver.core.netdisk.baidupan.service;

/**
 * base url 是 http://d.pcs.baidu.com/
 */

import com.github.coerx.qarchiver.core.netdisk.baidupan.response.SliceUploadResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface BaiduNetDiskServiceOfPcs {
    @Multipart
    @POST("rest/2.0/pcs/superfile2?method=upload&type=tmpfile&app_id=250528")
    @Headers("accept: */*")
    Call<SliceUploadResponse> sliceUpload(@Query(value = "BDUSS", encoded = true) String budss,
                                          @Query(value = "uploadid", encoded = true) String uploadId,
                                          @Query(value = "path", encoded = true) String path,
                                          @Query(value = "partseq", encoded = true) int partSeq,
                                          @Part MultipartBody.Part requestBody);
}
