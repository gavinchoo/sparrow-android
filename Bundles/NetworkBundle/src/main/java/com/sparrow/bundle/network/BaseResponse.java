package com.sparrow.bundle.network;

import java.io.Serializable;

/**
 * 该类仅供参考，实际业务返回的固定字段, 根据需求来定义，
 */
public class BaseResponse<T> implements Serializable {

    private String code;
    private boolean success;
    private String message;
    private T data;

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getResult() {
        return data;
    }

    public void setResult(T result) {
        this.data = result;
    }

    public boolean isOk() {
        return "200".equals(code) || success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
