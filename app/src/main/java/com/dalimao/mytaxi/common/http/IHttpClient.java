package com.dalimao.mytaxi.common.http;

public interface IHttpClient {

    IResponse get(IRequest request, boolean forceCache);
    IResponse post(IRequest request, boolean forceCache);
}
