package com.dalimao.mytaxi.main.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.dalimao.mytaxi.MyTaxiApplication;
import com.dalimao.mytaxi.R;
import com.dalimao.mytaxi.account.model.AccountManagerImpl;
import com.dalimao.mytaxi.account.model.IAccountManager;
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
import com.dalimao.mytaxi.main.presenter.IMainPresenter;
import com.dalimao.mytaxi.main.presenter.MainPresenterImpl;
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

public class MainActivity extends AppCompatActivity implements IMainView{

    private static final String TAG = MainActivity.class.getSimpleName();
    private IMainPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IHttpClient httpClient = new OkHttpClientImpl();
        SharedPreferencesDao dao =
                new SharedPreferencesDao(MyTaxiApplication.getInstance(),
                        SharedPreferencesDao.FILE_ACCOUNT);
        IAccountManager accountManager = new AccountManagerImpl(httpClient, dao);
        mPresenter = new MainPresenterImpl(this, accountManager);
        //检查用户是否登录
        mPresenter.loginByToken();
    }


    /**
     * 显示手机输入框
     */
    private void showPhoneInputDialog() {
        PhoneInputDialog phoneInputDialog = new PhoneInputDialog(this);
        phoneInputDialog.show();
    }

    @Override
    public void showLoginSuc() {
        ToastUtil.show(this, getString(R.string.login_suc));
    }


    /**
     * 显示 loading
     */
    @Override
    public void showLoading() {
        // TODO: 17/5/14   显示加载框
    }

    /**
     * 错误处理
     *
     * @param code
     * @param msg
     */

    @Override
    public void showError(int code, String msg) {
        switch (code) {
            case IAccountManager.TOKEN_INVALID:
                // 登录过期
                ToastUtil.show(this, getString(R.string.token_invalid));
                showPhoneInputDialog();
                break;
            case IAccountManager.SERVER_FAIL:
                // 服务器错误
                showPhoneInputDialog();
                break;

        }
    }
}
