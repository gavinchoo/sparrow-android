package com.sparrow.bundle.framework.base.entity;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class MultipartBodyEntity {
    public MultipartBody.Part[] parts;
    public Map<String, RequestBody> multipartBody;

}