package com.dalimao.mytaxi.common.http;

public interface IResponse {

    // 状态码
    int getCode();
    // 数据体
    String getData();
}
