package com.dalimao.mytaxi;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class TestOkHttp3 {

    /**
     * 测试 OkHttp Get 方法
     */
    @Test
    public void testGet(){
         //创建okhttp对象
        OkHttpClient client = new OkHttpClient();
        //创建request对象
        Request request = new Request.Builder()
                .url("http://httpbin.org/get?id=id")
                .build();
        //OkHttpClient执行request
        try {
            Response response=client.newCall(request).execute();
            System.out.println("response"+response.body().string() );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 测试 OkHttp Post 方法
     */
    @Test
    public void testPost(){
        //创建okhttp对象
        OkHttpClient client = new OkHttpClient();
        //创建request对象
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, "{\"name\": \"dalimao\"}");
        Request request = new Request.Builder()
                .url("http://httpbin.org/post")//请求行
                //.header() //请求头
                .post(body)
                .build();
        //OkHttpClient执行request
        try {
            Response response=client.newCall(request).execute();
            System.out.println("response"+response.body().string() );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 测试拦截器
     */
    @Test
    public void testInterceptor(){
        //定义拦截器
        Interceptor interceptor =new Interceptor(){

            @Override
            public Response intercept(Chain chain) throws IOException {

                long start = System.currentTimeMillis();
                Request request = chain.request();
                Response response = chain.proceed(request);
                long end = System.currentTimeMillis();
                System.out.println("Interceptor: cost time = "+(end-start));
                return response;
            }
        };

        //创建okhttp对象
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();
        //创建request对象
        Request request = new Request.Builder()
                .url("http://httpbin.org/get?id=id")
                .build();
        //OkHttpClient执行request
        try {
            Response response=client.newCall(request).execute();
            System.out.println("response"+response.body().string() );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 测试缓存
     */
    @Test
    public void testCache(){
        //创建缓存对象
        Cache cache = new Cache(new File("cache.cache"), 1024 * 1024);
        //创建okhttp对象
        OkHttpClient client = new OkHttpClient.Builder()
                .cache(cache)
                .build();
        //创建request对象
        Request request = new Request.Builder()
                .url("http://httpbin.org/get?id=id")
                .cacheControl(CacheControl.FORCE_CACHE)//强制从缓存取或者强制从网络取
                .build();
        //OkHttpClient执行request
        try {
            Response response=client.newCall(request).execute();
            Response responseCache = response.cacheResponse();
            Response responseNet = response.networkResponse();
            if (responseCache != null) {
                System.out.println("reponse from cache");
            }

            if (responseNet != null) {
                System.out.println("reponse from net");
            }
            System.out.println("response"+response.body().string() );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
