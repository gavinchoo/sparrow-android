package com.sparrow.bundle.photo.matisse.upload;

import java.util.HashMap;

/**
 * Created by WEI on 2017/7/27.
 */

public class UploadParams {

    public HashMap<String, Object> params = new HashMap<>();

    public UploadType uploadType;
    public String fileKey;
    public String requestUrl;
    public int quality = 1;
    public boolean original = false;

    public enum UploadType {
        Multipart, Form
    }

    public UploadParams url(String requestUrl) {
        this.requestUrl = requestUrl;
        return this;
    }

    public UploadParams addParams(String key, Object value) {
        params.put(key, value);
        return this;
    }

    public UploadParams type(UploadType type) {
        uploadType = type;
        return this;
    }

    public UploadParams fileKey(String fileKey) {
        this.fileKey = fileKey;
        return this;
    }

    public UploadParams quality(int quality) {
        this.quality = quality;
        return this;
    }

    public UploadParams original(boolean original) {
        this.original = original;
        return this;
    }
}
