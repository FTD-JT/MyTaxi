package com.dalimao.mytaxi.main.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.dalimao.mytaxi.MyTaxiApplication;
import com.dalimao.mytaxi.R;
import com.dalimao.mytaxi.account.model.response.Account;
import com.dalimao.mytaxi.account.model.response.LoginResponse;
import com.dalimao.mytaxi.account.view.PhoneInputDialog;
import com.dalimao.mytaxi.common.http.IHttpClient;
import com.dalimao.mytaxi.common.http.IRequest;
import com.dalimao.mytaxi.common.http.IResponse;
import com.dalimao.mytaxi.common.http.api.API;
import com.dalimao.mytaxi.common.http.biz.BaseBizResponse;
import com.dalimao.mytaxi.common.http.impl.BaseRequest;
import com.dalimao.mytaxi.common.http.impl.BaseResponse;
import com.dalimao.mytaxi.common.http.impl.OkHttpClientImpl;
import com.dalimao.mytaxi.common.storage.SharedPreferencesDao;
import com.dalimao.mytaxi.common.util.ToastUtil;
import com.google.gson.Gson;


/**
 * －－－ 登录逻辑－－－
 * 1 检查本地纪录(登录态检查)
 * 2 若用户没登录则登录
 * 3 登录之前先校验手机号码
 * 4 token 有效使用 token 自动登录
 * －－－－ 地图初始化－－－
 * 1 地图接入
 * 2 定位自己的位置，显示蓝点
 * 3 使用 Marker 标记当前位置和方向
 * 4 地图封装
 * ------获取附近司机---
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private IHttpClient mHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHttpClient = new OkHttpClientImpl();
        checkLoginState();
    }

    /**
     * 检查用户是否登录
     */
    private void checkLoginState() {
        //获取本地登录信息
        SharedPreferencesDao dao = new SharedPreferencesDao(MyTaxiApplication.getINSTANCE(),
                SharedPreferencesDao.FILE_ACCOUNT);
        final Account account = (Account) dao.get(SharedPreferencesDao.KEY_ACCOUNT, Account.class);

        //登录是否过期
        boolean tokenValid = false;

        //检查登录是否过期
        if (account != null) {
            if (account.getExpired() > System.currentTimeMillis()) {
                //token有效
                tokenValid = true;
            }
        }


        if (!tokenValid) {
            showPhoneInputDialog();
        } else {
            //请求网络，完成自动登录
            new Thread(){
                @Override
                public void run() {
                    String url = API.Config.getDomain() + API.LOGIN_BY_TOKEN;
                    IRequest request = new BaseRequest(url);
                    request.setBody("token",account.getToken());
                    IResponse response = mHttpClient.post(request, false);
                    Log.d(TAG, response.getData());
                    if (response.getCode() == BaseResponse.STATE_OK) {
                        LoginResponse loginResponse = new Gson().fromJson(response.getData(), LoginResponse.class);
                        if (loginResponse.getCode() == BaseBizResponse.STATE_OK) {
                            //保存登录信息
                            Account account = loginResponse.getData();
                            //todo:加密存储
                            SharedPreferencesDao dao = new SharedPreferencesDao(MyTaxiApplication.getINSTANCE(),
                                    SharedPreferencesDao.FILE_ACCOUNT);
                            dao.save(SharedPreferencesDao.KEY_ACCOUNT, account);
                            //通知UI登录成功
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtil.show(MainActivity.this,getString(R.string.login_suc));
                                }
                            });
                        } else if (loginResponse.getCode() == BaseBizResponse.STATE_TOKEN_INVALID) {
                            //token过期
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showPhoneInputDialog();
                                }
                            });
                        }
                    } else {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.show(MainActivity.this,getString(R.string.error_server));
                            }
                        });
                    }
                }
            }.start();
        }
    }


    /**
     * 显示手机输入框
     */
    private void showPhoneInputDialog() {
        PhoneInputDialog phoneInputDialog = new PhoneInputDialog(this);
        phoneInputDialog.show();
    }
}
