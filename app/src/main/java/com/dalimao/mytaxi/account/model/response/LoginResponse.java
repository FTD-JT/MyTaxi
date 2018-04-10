package com.dalimao.mytaxi.account.model.response;

import com.dalimao.mytaxi.common.http.biz.BaseBizResponse;

public class LoginResponse extends BaseBizResponse {

    Account data;

    public Account getData() {
        return data;
    }

    public void setData(Account data) {
        this.data = data;
    }
}
