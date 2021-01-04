package com.github.coerx.qarchiver.core.dispatch;

/**
 * 各种任务的进度回调接口
 *
 * @param <T>
 */
public class BaseCallback<T> {

    public void onStart(T event) {
    }

    public void onProgress(T event) {
    }

    public void onError(T event) {
    }

    public void onDone(T event) {
    }
}


