package com.dalimao.mytaxi.account.model;

import android.os.Handler;
import android.util.Log;

import com.dalimao.mytaxi.MyTaxiApplication;
import com.dalimao.mytaxi.R;
import com.dalimao.mytaxi.account.model.response.Account;
import com.dalimao.mytaxi.account.model.response.LoginResponse;
import com.dalimao.mytaxi.common.http.IHttpClient;
import com.dalimao.mytaxi.common.http.IRequest;
import com.dalimao.mytaxi.common.http.IResponse;
import com.dalimao.mytaxi.common.http.api.API;
import com.dalimao.mytaxi.common.http.biz.BaseBizResponse;
import com.dalimao.mytaxi.common.http.impl.BaseRequest;
import com.dalimao.mytaxi.common.http.impl.BaseResponse;
import com.dalimao.mytaxi.common.storage.SharedPreferencesDao;
import com.dalimao.mytaxi.common.util.DevUtil;
import com.dalimao.mytaxi.common.util.ToastUtil;
import com.dalimao.mytaxi.main.view.MainActivity;
import com.google.gson.Gson;

public class AccountManagerImpl implements IAccountManager {

    private static final String TAG = "AccountManagerImpl";


    // 网络请求库
    private IHttpClient httpClient;

    // 数据存储
    private SharedPreferencesDao sharedPreferencesDao;

    // 发送消息 handler

    private Handler handler;

    public AccountManagerImpl(IHttpClient httpClient,
                              SharedPreferencesDao sharedPreferencesDao) {
        this.httpClient = httpClient;
        this.sharedPreferencesDao = sharedPreferencesDao;
    }


    @Override
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    /**
     *  下发验证码
     */
    @Override
    public void fetchSMSCode(final String phone) {
        new Thread(){
            @Override
            public void run() {
                String url = API.Config.getDomain() + API.GET_SMS_CODE;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", phone);
                IResponse response = httpClient.get(request, false);
                Log.d(TAG, response.getData());
                if (response.getCode() == BaseResponse.STATE_OK) {
                    BaseBizResponse baseBizResponse = new Gson().fromJson(response.getData(), BaseBizResponse.class);
                    if (baseBizResponse.getCode() == BaseBizResponse.STATE_OK) {
                        handler.sendEmptyMessage(SMS_SEND_SUC);
                    } else {
                        handler.sendEmptyMessage(SMS_SEND_FAIL);
                    }
                } else {
                    handler.sendEmptyMessage(SMS_SEND_FAIL);
                }
            }
        }.start();
    }

    /**
     * 校验验证码
     */
    @Override
    public void checkSmsCode(final String phone, final String smsCode) {
        new Thread(){
            @Override
            public void run() {
                String url = API.Config.getDomain() + API.CHECK_SMS_CODE;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", phone);
                request.setBody("code", smsCode);
                IResponse response = httpClient.get(request, false);
                Log.d(TAG, response.getData());
                if (response.getCode() == BaseResponse.STATE_OK) {
                    BaseBizResponse baseBizResponse = new Gson().fromJson(response.getData(), BaseBizResponse.class);
                    if (baseBizResponse.getCode() == BaseBizResponse.STATE_OK) {
                        handler.sendEmptyMessage(SMS_CHECK_SUC);
                    } else {
                        handler.sendEmptyMessage(SMS_CHECK_FAIL);
                    }
                } else {
                    handler.sendEmptyMessage(SMS_CHECK_FAIL);
                }
            }
        }.start();

    }

    /**
     *  用户是否注册接口
     */
    @Override
    public void checkUserExist(final String phone) {
        new Thread(){
            @Override
            public void run() {
                String url = API.Config.getDomain() + API.CHECK_USER_EXIST;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", phone);
                IResponse response = httpClient.get(request, false);
                Log.d(TAG, response.getData());
                if (response.getCode() == BaseResponse.STATE_OK) {
                    BaseBizResponse baseBizResponse = new Gson().fromJson(response.getData(), BaseBizResponse.class);
                    if (baseBizResponse.getCode() == BaseBizResponse.STATE_USER_EXIST) {
                        handler.sendEmptyMessage(USER_EXIST);
                    } else if (baseBizResponse.getCode() == BaseBizResponse.STATE_USER_NOT_EXIST){
                        handler.sendEmptyMessage(USER_NOT_EXIST);
                    }
                } else {
                    handler.sendEmptyMessage(SERVER_FAIL);
                }
            }
        }.start();
    }

    /**
     *  注册
     */
    @Override
    public void register(final String phone, final String password) {
        //请求网络，提交注册
        new Thread(){
            @Override
            public void run() {
                String url = API.Config.getDomain() + API.REGISTER;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", phone);
                request.setBody("password",password);
                request.setBody("uid", DevUtil.UUID(MyTaxiApplication.getInstance()));
                IResponse response = httpClient.post(request, false);
                Log.d(TAG, response.getData());
                if (response.getCode() == BaseResponse.STATE_OK) {
                    BaseBizResponse baseBizResponse = new Gson().fromJson(response.getData(), BaseBizResponse.class);
                    if (baseBizResponse.getCode() == BaseBizResponse.STATE_OK) {
                        handler.sendEmptyMessage(REGISTER_SUC);
                    } else {
                        handler.sendEmptyMessage(SERVER_FAIL);
                    }
                } else {
                    handler.sendEmptyMessage(SERVER_FAIL);
                }
            }
        }.start();
    }

    /**
     *  登录
     */
    @Override
    public void login(final String phone, final String password) {
        new Thread(){
            @Override
            public void run() {
                String url = API.Config.getDomain() + API.LOGIN;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", phone);
                request.setBody("password",password);
                IResponse response = httpClient.post(request, false);
                Log.d(TAG, response.getData());
                if (response.getCode() == BaseResponse.STATE_OK) {
                    LoginResponse loginResponse = new Gson().fromJson(response.getData(), LoginResponse.class);
                    if (loginResponse.getCode() == BaseBizResponse.STATE_OK) {
                        //保存登录信息
                        Account account = loginResponse.getData();
                        //todo:加密存储
                        SharedPreferencesDao dao = new SharedPreferencesDao(MyTaxiApplication.getInstance(),
                                SharedPreferencesDao.FILE_ACCOUNT);
                        dao.save(SharedPreferencesDao.KEY_ACCOUNT, account);
                        //通知UI登录成功
                        handler.sendEmptyMessage(LOGIN_SUC);
                    } else if (loginResponse.getCode() == BaseBizResponse.STATE_PW_ERR) {
                        //登录密码错误
                        handler.sendEmptyMessage(PW_ERROR);
                    } else {
                        handler.sendEmptyMessage(SERVER_FAIL);
                    }
                } else {
                    handler.sendEmptyMessage(SERVER_FAIL);
                }
            }
        }.start();
    }

    /**
     * token 登录
     */
    @Override
    public void loginByToken() {
        //获取本地登录信息
        SharedPreferencesDao dao = new SharedPreferencesDao(MyTaxiApplication.getInstance(),
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
            handler.sendEmptyMessage(TOKEN_INVALID);
        } else {
            //请求网络，完成自动登录
            new Thread() {
                @Override
                public void run() {
                    String url = API.Config.getDomain() + API.LOGIN_BY_TOKEN;
                    IRequest request = new BaseRequest(url);
                    request.setBody("token", account.getToken());
                    IResponse response = httpClient.post(request, false);
                    Log.d(TAG, response.getData());
                    if (response.getCode() == BaseResponse.STATE_OK) {
                        LoginResponse loginResponse = new Gson().fromJson(response.getData(), LoginResponse.class);
                        if (loginResponse.getCode() == BaseBizResponse.STATE_OK) {
                            //保存登录信息
                            Account account = loginResponse.getData();
                            //todo:加密存储
                            SharedPreferencesDao dao = new SharedPreferencesDao(MyTaxiApplication.getInstance(),
                                    SharedPreferencesDao.FILE_ACCOUNT);
                            dao.save(SharedPreferencesDao.KEY_ACCOUNT, account);
                            //通知UI登录成功
                            handler.sendEmptyMessage(LOGIN_SUC);
                        } else if (loginResponse.getCode() == BaseBizResponse.STATE_TOKEN_INVALID) {
                            //token过期
                            handler.sendEmptyMessage(TOKEN_INVALID);
                        }
                    } else {
                        handler.sendEmptyMessage(SERVER_FAIL);
                    }
                }
            }.start();
        }
    }

    /**
     * 是否登录
     * @return
     */
        @Override
        public boolean isLogin() {
        // 获取本地登录信息
        Account account = (Account) sharedPreferencesDao.get(SharedPreferencesDao.KEY_ACCOUNT,
                        Account.class);


        // 登录是否过期
        boolean tokenValid = false;

        // 检查token是否过期
        if (account != null) {
            if (account.getExpired() > System.currentTimeMillis()) {
                // token 有效
                tokenValid = true;
            }
        }
        return tokenValid;

    }
}
