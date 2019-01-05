package com.sparrow.bundle.network.exception;

/**
 * @author zhangshaopeng
 * @date 2016/8/10 0010
 * @description
 */
public class ServerException extends IllegalAccessException {

    private String code;
    private String message;

    public ServerException(String message, String code) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
