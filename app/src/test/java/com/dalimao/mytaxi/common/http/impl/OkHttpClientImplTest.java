package com.dalimao.mytaxi.common.http.impl;

import com.dalimao.mytaxi.common.http.IHttpClient;
import com.dalimao.mytaxi.common.http.IRequest;
import com.dalimao.mytaxi.common.http.IResponse;
import com.dalimao.mytaxi.common.http.api.API;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class OkHttpClientImplTest {
    IHttpClient iHttpClient;

    @Before
    public void setUp() throws Exception {
        iHttpClient = new OkHttpClientImpl();
        API.Config.setDebug(false);
    }

    @Test
    public void get() {
        //Request参数
        String url = API.Config.getDomain() + API.TEST_GET;
        IRequest request = new BaseRequest(url);
        request.setBody("uid","12345");
        request.setHeader("testHeader", "test header");
        IResponse response = iHttpClient.get(request, false);
        System.out.println("stateCode = " + response.getCode());
        System.out.println("data = " + response.getData());
    }

    @Test
    public void post() {
        //Request参数
        String url = API.Config.getDomain() + API.TEST_POST;
        IRequest request = new BaseRequest(url);
        request.setBody("uid","12345");
        request.setHeader("testHeader", "test header");
        IResponse response = iHttpClient.post(request, false);
        System.out.println("stateCode = " + response.getCode());
        System.out.println("data = " + response.getData());
    }
}