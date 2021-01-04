package com.github.coerx.qarchiver.core.netdisk.baidupan.response;

public class SliceUploadResponse {
    private int errno;
    private String md5;

    @Override
    public String toString() {
        return "SliceUploadResponse{" +
                "errno=" + errno +
                ", md5='" + md5 + '\'' +
                '}';
    }

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
