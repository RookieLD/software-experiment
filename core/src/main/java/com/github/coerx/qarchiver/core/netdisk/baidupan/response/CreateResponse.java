package com.github.coerx.qarchiver.core.netdisk.baidupan.response;

public class CreateResponse {
    /**
     * 0代表创建成功
     */
    private int errno;
    private String fs_id;
    private String md5;
    private String server_filename;
    private int category;
    private String path;
    private String size;
    private String ctime;
    private String mtime;
    private int isdir;

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public String getFs_id() {
        return fs_id;
    }

    public void setFs_id(String fs_id) {
        this.fs_id = fs_id;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getServer_filename() {
        return server_filename;
    }

    public void setServer_filename(String server_filename) {
        this.server_filename = server_filename;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public String getMtime() {
        return mtime;
    }

    public void setMtime(String mtime) {
        this.mtime = mtime;
    }

    public int getIsdir() {
        return isdir;
    }

    public void setIsdir(int isdir) {
        this.isdir = isdir;
    }

    @Override
    public String toString() {
        return "CreateResponse{" +
                "errno=" + errno +
                ", fs_id='" + fs_id + '\'' +
                ", md5='" + md5 + '\'' +
                ", server_filename='" + server_filename + '\'' +
                ", category=" + category +
                ", path='" + path + '\'' +
                ", size='" + size + '\'' +
                ", ctime='" + ctime + '\'' +
                ", mtime='" + mtime + '\'' +
                ", isdir=" + isdir +
                '}';
    }
}
