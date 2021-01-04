package com.github.coerx.qarchiver.core.netdisk.baidupan;

import com.github.coerx.qarchiver.common.util.FileUtil;
import com.github.coerx.qarchiver.common.util.MapUtil;
import com.github.coerx.qarchiver.common.util.StringUtil;
import com.github.coerx.qarchiver.core.netdisk.baidupan.dispatch.BaiduWangPanUploadCallback;
import com.github.coerx.qarchiver.core.netdisk.baidupan.dispatch.BaiduWangPanUploadEvent;
import com.github.coerx.qarchiver.core.netdisk.baidupan.response.CreateResponse;
import com.github.coerx.qarchiver.core.netdisk.baidupan.response.PreCreateResponse;
import com.github.coerx.qarchiver.core.netdisk.baidupan.response.SliceUploadResponse;
import com.github.coerx.qarchiver.core.netdisk.baidupan.service.BaiduNetDiskServiceOfPan;
import com.github.coerx.qarchiver.core.netdisk.baidupan.service.BaiduNetDiskServiceOfPcs;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 访问百度网盘的客户端
 */
public class BaiduPanClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BaiduPanClient.class);

    private static final String URL = "https://pan.baidu.com/";
    private static final String FILE_UPLOAD_URL = "https://d.pcs.baidu.com/";
    private static final int SLICE_SIZE = 1024 * 1024 * 4;
    private static final MediaType urlencodedMediaType = MediaType.parse("application/x-www-form-urlencoded");
    private static final MediaType multipartFormDataMediaType = MediaType.parse("multipart/form-data");


    private String accessToken;
    private String bduss;
    /**
     * base url 是 pan.baidu.com
     */
    private BaiduNetDiskServiceOfPan baiduNetDiskServiceOfPan;
    /**
     * base url 是 d.pcs.baidu.com
     */
    private BaiduNetDiskServiceOfPcs baiduNetDiskServiceOfPcs;
    private MessageDigest md5;

    public BaiduPanClient(String accessToken, String bduss) {
        try {
            this.accessToken = accessToken;
            this.bduss = bduss;
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
            OkHttpClient client2 = new OkHttpClient.Builder().addInterceptor(interceptor).build();
            /*Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Object.class, new NaturalDeserializer())
                    .create();*/
            Retrofit retrofitOfPan = new Retrofit.Builder()
                    .baseUrl(URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
            Retrofit retrofitOfPcs = new Retrofit.Builder()
                    .baseUrl(FILE_UPLOAD_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client2)
                    .build();
            baiduNetDiskServiceOfPan = retrofitOfPan.create(BaiduNetDiskServiceOfPan.class);
            baiduNetDiskServiceOfPcs = retrofitOfPcs.create(BaiduNetDiskServiceOfPcs.class);
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }


    }

    public String getNetdiskName() throws IOException {
        return baiduNetDiskServiceOfPan.userInfo(accessToken).execute().body().get("netdisk_name");
    }

    /**
     * @param netDiskPath 存放在网盘的路径 必须在/apps/appName/文件夹下面，其中appName代表应用名称，例如/apps/qarchiver/filename.jp
     * @param file
     * @return
     */
    public CompletableFuture<CreateResponse> fileUpload(String netDiskPath, File file, BaiduWangPanUploadCallback callBack) throws IOException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new RuntimeException("文件不存在或者不是文件,file=" + file);
        }
        String[] uploadId = new String[1];
        List<String> md5List = FileUtil.md5sumStringListOfEachSlice(file, SLICE_SIZE);
        return precreate(netDiskPath, file, callBack)
                .thenComposeAsync((preCreateResponse -> {
                    uploadId[0] = preCreateResponse.getUploadid();
                    return sliceUploadSequentially(
                            netDiskPath,
                            file,
                            preCreateResponse.getUploadid(),
                            new LinkedList<>(preCreateResponse.getBlock_list()),
                            callBack);
                }))
                .thenComposeAsync(blockList -> createFile(
                        netDiskPath,
                        file,
                        uploadId[0],
                        blockList,
                        callBack));

    }

    public CompletableFuture<PreCreateResponse> precreate(String netDiskPath, File file, BaiduWangPanUploadCallback callBack) {
        CompletableFuture<PreCreateResponse> future = new CompletableFuture<>();
        try {
            Gson gson = new GsonBuilder().create();

            Map<String, Object> jsonMap = new HashMap<>();
            List<String> md5List = FileUtil.md5sumStringListOfEachSlice(file, SLICE_SIZE);
            String sliceMd5 = gson.toJson(md5List);
            String fileMd5 = FileUtil.md5sumString(file);
            log.info("本地计算出的文件分片md5{}", sliceMd5);
            log.info("本地计算出的文集md5{}", fileMd5);
            jsonMap.put("path", StringUtil.urlEncodeUTF8(netDiskPath));
            jsonMap.put("size", String.valueOf(file.length()));
            jsonMap.put("isdir", "0");
            jsonMap.put("autoinit", 1);
            jsonMap.put("rtype", 3);
            jsonMap.put("block_list", sliceMd5);
            jsonMap.put("content-md5", fileMd5);

            String sendData = MapUtil.mapToUrlQueryString(jsonMap);
            callBack.onStart(new BaiduWangPanUploadEvent(0, "开始预创建文件", null));
            baiduNetDiskServiceOfPan.preCreate(accessToken, RequestBody.create(urlencodedMediaType, sendData))
                    .enqueue(new Callback<PreCreateResponse>() {
                        @Override
                        public void onResponse(Call<PreCreateResponse> call, Response<PreCreateResponse> response) {
                            //System.out.println(response.body());
                            log.info("预创建文件请求的相应体 {}", response.body());
                            future.complete(response.body());
                        }

                        @Override
                        public void onFailure(Call<PreCreateResponse> call, Throwable t) {
                            future.completeExceptionally(t);
                            callBack.onError(new BaiduWangPanUploadEvent(0, "预创建文件失败", t));
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return future;
    }

    public CompletableFuture<List<String>> sliceUploadSequentially(String netDiskPath, File file, String uploadId, LinkedList<String> blockSeq, BaiduWangPanUploadCallback callback) {
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        try {
            if (blockSeq.isEmpty()) {
                blockSeq.add("0");
            }

            List<String> blockList = new LinkedList<>();
            InputStream in = new FileInputStream(file);
            byte[] buffer = new byte[SLICE_SIZE];
            int readLength = in.readNBytes(buffer, 0, SLICE_SIZE);
            callback.onProgress(new BaiduWangPanUploadEvent(readLength, "上传中", null));
            RequestBody requestBody = RequestBody.create(multipartFormDataMediaType, buffer, 0, readLength);
            MultipartBody.Part formData = MultipartBody.Part.createFormData("file", StringUtil.urlEncodeUTF8(netDiskPath), requestBody);
            Call<SliceUploadResponse> sliceUploadResponseCall = baiduNetDiskServiceOfPcs.sliceUpload(
                    bduss,
                    uploadId,
                    StringUtil.urlEncodeUTF8(netDiskPath),
                    Integer.parseInt(blockSeq.poll()),
                    formData);

            sliceUploadResponseCall.enqueue(new SliceUploadSequentiallyCallBack(netDiskPath, file, uploadId, blockSeq, in, buffer, blockList, future, callback));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return future;
    }

    public CompletableFuture<CreateResponse> createFile(String netDiskPath, File file, String uploadId, List<String> blockList, BaiduWangPanUploadCallback callback) {
        CompletableFuture<CreateResponse> future = new CompletableFuture<>();
        //System.out.println(blockList);

        Gson gson = new GsonBuilder().create();

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("path", StringUtil.urlEncodeUTF8(netDiskPath));
        jsonMap.put("size", String.valueOf(file.length()));
        jsonMap.put("isdir", "0");
        jsonMap.put("rtype", 3);
        jsonMap.put("uploadid", uploadId);
        jsonMap.put("block_list", gson.toJson(blockList));

        String sendData = MapUtil.mapToUrlQueryString(jsonMap);

        baiduNetDiskServiceOfPan.create(accessToken, RequestBody.create(urlencodedMediaType, sendData))
                .enqueue(new Callback<CreateResponse>() {
                    @Override
                    public void onResponse(Call<CreateResponse> call, Response<CreateResponse> response) {
                        log.info("create请求的响应体:{}", response.body());
                        future.complete(response.body());
                        callback.onDone(new BaiduWangPanUploadEvent(0, "上传完成", null));
                    }

                    @Override
                    public void onFailure(Call<CreateResponse> call, Throwable t) {
                        callback.onError(new BaiduWangPanUploadEvent(0, "创建文件出错", t));
                        future.completeExceptionally(t);
                    }
                });
        return future;
    }

    //todo 这样设计好奇怪
    private class SliceUploadSequentiallyCallBack implements Callback<SliceUploadResponse> {

        private String netDiskPath;
        private File file;
        private String uploadId;
        private LinkedList<String> blockSeq;
        private InputStream in;
        private byte[] buffer;
        private List<String> blockList;
        private CompletableFuture<List<String>> future;
        private BaiduWangPanUploadCallback callback;

        public SliceUploadSequentiallyCallBack(String netDiskPath,
                                               File file,
                                               String uploadId,
                                               LinkedList<String> blockSeq,
                                               InputStream in,
                                               byte[] buffer,
                                               List<String> blockList,
                                               CompletableFuture<List<String>> future,
                                               BaiduWangPanUploadCallback callback) {

            this.netDiskPath = netDiskPath;
            this.file = file;
            this.uploadId = uploadId;
            this.blockSeq = blockSeq;
            this.in = in;
            this.buffer = buffer;
            this.blockList = blockList;
            this.future = future;
            this.callback = callback;
        }

        @Override
        public void onResponse(Call<SliceUploadResponse> call, Response<SliceUploadResponse> response) {
            log.info("切片上传响应体 {}", response.body());
            try {
                blockList.add(response.body().getMd5());

                String nextSeq = blockSeq.poll();
                if (nextSeq == null) {
                    in.close();
                    log.info("superfile返回的分片md5 {}", blockList);
                    future.complete(blockList);
                    return;
                }

                int readLength = in.readNBytes(buffer, 0, SLICE_SIZE);
                if (readLength <= 0) {
                    in.close();
                    log.info("superfile返回的分片md5 {}", blockList);
                    future.complete(blockList);
                    return;
                }
                callback.onProgress(new BaiduWangPanUploadEvent(readLength, "上传中", null));
                RequestBody requestBody = RequestBody.create(multipartFormDataMediaType, buffer, 0, readLength);
                MultipartBody.Part formData = MultipartBody.Part.createFormData("file", StringUtil.urlEncodeUTF8(netDiskPath), requestBody);

                baiduNetDiskServiceOfPcs.sliceUpload(
                        bduss,
                        uploadId,
                        StringUtil.urlEncodeUTF8(netDiskPath),
                        Integer.parseInt(nextSeq),
                        formData)
                        .enqueue(new SliceUploadSequentiallyCallBack(netDiskPath, file, uploadId, blockSeq, in, buffer, blockList, future, callback));
            } catch (Exception e) {
                callback.onError(new BaiduWangPanUploadEvent(0, "分片上传出错", e));
                future.completeExceptionally(e);
            }
        }

        @Override
        public void onFailure(Call<SliceUploadResponse> call, Throwable t) {
            future.completeExceptionally(t);
            callback.onError(new BaiduWangPanUploadEvent(0, "分片上传出错", t));
        }
    }

}
