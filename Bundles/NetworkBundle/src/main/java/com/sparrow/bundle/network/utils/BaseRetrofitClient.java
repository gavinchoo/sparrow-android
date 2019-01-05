package com.sparrow.bundle.network.utils;

import android.content.Context;
import android.util.Log;

import com.sparrow.bundle.network.BuildConfig;
import com.sparrow.bundle.network.cookie.CookieJarImpl;
import com.sparrow.bundle.network.cookie.store.PersistentCookieStore;
import com.sparrow.bundle.network.interceptor.BaseInterceptor;
import com.sparrow.bundle.network.interceptor.CacheInterceptor;
import com.sparrow.bundle.network.interceptor.logging.Level;
import com.sparrow.bundle.network.interceptor.logging.LoggingInterceptor;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.platform.Platform;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * RetrofitClient封装单例类, 实现网络请求
 */
public class BaseRetrofitClient {
    private static final String MAIN_TAG = "NetworkBundle";

    //超时时间
    private static final int DEFAULT_TIMEOUT = 45;
    //缓存时间
    private static final int CACHE_TIMEOUT = 10 * 1024 * 1024;

    private static OkHttpClient okHttpClient;
    private static Retrofit retrofit;

    public static boolean loggable = false;

    private Cache cache = null;
    private File httpCacheDirectory;

    private BaseInterceptor headersInterceptor = new BaseInterceptor();

    public BaseRetrofitClient(Context context, String baseUrl, Map<String, String> headers) {

        if (httpCacheDirectory == null) {
            httpCacheDirectory = new File(context.getCacheDir(), "goldze_cache");
        }

        try {
            if (cache == null) {
                cache = new Cache(httpCacheDirectory, CACHE_TIMEOUT);
            }
        } catch (Exception e) {
            Log.e("BaseRetrofitClient", e.getMessage());
        }
        headersInterceptor.setHeaders(headers);
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory();
        okHttpClient = new OkHttpClient.Builder()
                .cookieJar(new CookieJarImpl(new PersistentCookieStore(context)))
//                .cache(cache)
                .addInterceptor(urlInterceptor)
                .addInterceptor(headersInterceptor)
                .addInterceptor(new CacheInterceptor(context))
                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                .addInterceptor(new LoggingInterceptor.Builder()//构建者模式
                        .loggable(loggable) //是否开启日志打印
                        .setLevel(Level.BODY) //打印的等级
                        .log(Platform.INFO) // 打印类型
                        .request(MAIN_TAG + "_Request") // request的Tag
                        .response(MAIN_TAG + "_Response")// Response的Tag
                        .addHeader("version", BuildConfig.VERSION_NAME)//打印版本
                        .build()
                )
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(8, 15, TimeUnit.SECONDS))
                // 这里你可以根据自己的机型设置同时连接的个数和时间，我这里8个，和每个保持时间为10s
                .build();

        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl)
                .build();
    }

    private Interceptor urlInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            return urlInterceptor(chain);
        }
    };

    public Response urlInterceptor(Interceptor.Chain chain) throws IOException {
        return chain.proceed(chain.request());
    }

    public void setHeaders(Map<String, String> headers) {
        headersInterceptor.setHeaders(headers);
    }

    /**
     * create you ApiService
     * Create an implementation of the API endpoints defined by the {@code service} interface.
     */
    public <T> T create(final Class<T> service) {
        if (service == null) {
            throw new RuntimeException("Api service is null!");
        }
        return retrofit.create(service);
    }


    public void load(String url, Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(8, 15, TimeUnit.SECONDS))
                // 这里你可以根据自己的机型设置同时连接的个数和时间，我这里8个，和每个保持时间为10s
                .build();
        Request.Builder builder = new Request.Builder();
        Request request = builder.get().url(url).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    /**
     * /**
     * execute your customer API
     * For example:
     * MyApiService service =
     * BaseRetrofitClient.getInstance(MainActivity.this).create(MyApiService.class);
     * <p>
     * BaseRetrofitClient.getInstance(MainActivity.this)
     * .execute(service.lgon("name", "password"), subscriber)
     * * @param subscriber
     */

    public static <T> T execute(Observable<T> observable, Observer<T> subscriber) {
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
        return null;
    }
}
