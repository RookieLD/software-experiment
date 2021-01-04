package com.github.coerx.qarchiver.core.netdisk.baidupan.dispatch;

public class BaiduWangPanUploadEvent {
    private int uploadBytes;
    private String message;
    private Throwable exception;

    public BaiduWangPanUploadEvent() {
    }

    public BaiduWangPanUploadEvent(int uploadBytes, String message, Throwable exception) {
        this.uploadBytes = uploadBytes;
        this.message = message;
        this.exception = exception;
    }

    public int getUploadBytes() {
        return uploadBytes;
    }

    public BaiduWangPanUploadEvent setUploadBytes(int uploadBytes) {
        this.uploadBytes = uploadBytes;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public BaiduWangPanUploadEvent setMessage(String message) {
        this.message = message;
        return this;
    }

    public Throwable getException() {
        return exception;
    }

    public BaiduWangPanUploadEvent setException(Throwable exception) {
        this.exception = exception;
        return this;
    }
}
