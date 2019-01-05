package com.sparrow.bundle.framework.utils;

import com.bumptech.glide.disklrucache.DiskLruCache;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.SafeKeyGenerator;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.signature.EmptySignature;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * zhujianwei134
 *
 * Glide缓存操作工具类
 */
public class GlideCacheUtils {

    public static String getCacheFilePath(String url) {
        File file = getCacheFile2(url);
        return null == file ?  null: file.getPath();
    }

    /**
     * Glide通过请求url获取
     * @param url
     * @return
     */
    public static File getCacheFile2(String url) {
        DataCacheKey dataCacheKey = new DataCacheKey(new GlideUrl(url), EmptySignature.obtain());
        SafeKeyGenerator safeKeyGenerator = new SafeKeyGenerator();
        String safeKey = safeKeyGenerator.getSafeKey(dataCacheKey);
        try {
            int cacheSize = 100 * 1000 * 1000;
            DiskLruCache diskLruCache = DiskLruCache.open(new File(Utils.getContext().getCacheDir(), DiskCache.Factory.DEFAULT_DISK_CACHE_DIR), 1, 1, cacheSize);
            DiskLruCache.Value value = diskLruCache.get(safeKey);
            if (value != null) {
                return value.getFile(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class DataCacheKey implements Key {

        private final Key sourceKey;
        private final Key signature;

        public DataCacheKey(Key sourceKey, Key signature) {
            this.sourceKey = sourceKey;
            this.signature = signature;
        }

        public Key getSourceKey() {
            return sourceKey;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof DataCacheKey) {
                DataCacheKey other = (DataCacheKey) o;
                return sourceKey.equals(other.sourceKey) && signature.equals(other.signature);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = sourceKey.hashCode();
            result = 31 * result + signature.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "DataCacheKey{"
                    + "sourceKey=" + sourceKey
                    + ", signature=" + signature
                    + '}';
        }

        @Override
        public void updateDiskCacheKey(MessageDigest messageDigest) {
            sourceKey.updateDiskCacheKey(messageDigest);
            signature.updateDiskCacheKey(messageDigest);
        }
    }
}
