package com.github.coerx.qarchiver.core.netdisk.baidupan.response;

import java.util.List;

public class PreCreateResponse {
    private int errno;
    private String path;
    private String uploadid;
    private int return_type;
    private List<String> block_list;

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUploadid() {
        return uploadid;
    }

    public void setUploadid(String uploadid) {
        this.uploadid = uploadid;
    }

    public int getReturn_type() {
        return return_type;
    }

    public void setReturn_type(int return_type) {
        this.return_type = return_type;
    }

    public List<String> getBlock_list() {
        return block_list;
    }

    public void setBlock_list(List<String> block_list) {
        this.block_list = block_list;
    }

    @Override
    public String toString() {
        return "PreCreateResponse{" +
                "errno=" + errno +
                ", path='" + path + '\'' +
                ", uploadid='" + uploadid + '\'' +
                ", return_type=" + return_type +
                ", block_list=" + block_list +
                '}';
    }
}
