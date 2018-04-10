package com.dalimao.mytaxi.account.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import com.dalimao.mytaxi.common.http.impl.OkHttpClientImpl;
import com.dalimao.mytaxi.common.storage.SharedPreferencesDao;
import com.dalimao.mytaxi.common.util.ToastUtil;
import com.google.gson.Gson;

import java.lang.ref.SoftReference;

public class LoginDialog extends Dialog{

    private static final String TAG = LoginDialog.class.getSimpleName();
    private static final int LOGIN_SUC = 1;
    private static final int SERVER_FAIL = 100;
    private static final int PW_ERROR = 4;


    private TextView mPhone;
    private EditText mPw;
    private Button mBtnConfirm;
    private View mLoading;
    private TextView mTips;
    private String mPhoneStr;
    private IHttpClient mHttpClient;
    private MyHandler myHandler;


    /**
     * 接收子线程消息的Handler
     */
    static class MyHandler extends Handler{
        SoftReference<LoginDialog> loginDialogSoftReference;

        public MyHandler(LoginDialog loginDialog) {
            loginDialogSoftReference = new SoftReference<LoginDialog>(loginDialog);
        }

        @Override
        public void handleMessage(Message msg) {
            LoginDialog loginDialog = loginDialogSoftReference.get();
            if (loginDialog == null) {
                return;
            }

            switch (msg.what) {
                case LoginDialog.LOGIN_SUC:
                    loginDialog.showLoginSuc();
                    break;
                case LoginDialog.PW_ERROR:
                    loginDialog.showPasswordError();
                    break;
                case LoginDialog.SERVER_FAIL:
                    loginDialog.showServerError();
                    break;
            }
        }
    }


    public LoginDialog(@NonNull Context context, String phone) {
        super(context, R.style.Dialog);
        mPhoneStr = phone;
        mHttpClient = new OkHttpClientImpl();
        myHandler = new MyHandler(this);
    }

    public LoginDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = layoutInflater.inflate(R.layout.dialog_login_input, null);
        setContentView(root);
        initViews();
    }


    private void initViews() {
        mPhone = (TextView) findViewById(R.id.phone);
        mPw = (EditText) findViewById(R.id.password);
        mBtnConfirm = (Button) findViewById(R.id.btn_confirm);
        mLoading = findViewById(R.id.loading);
        mTips = (TextView) findViewById(R.id.tips);
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        mPhone.setText(mPhoneStr);

    }

    /**
     * 提交登录
     */
    private void submit() {
        //网络请求
        new Thread(){
            @Override
            public void run() {
                String url = API.Config.getDomain() + API.LOGIN;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", mPhoneStr);
                String password = mPw.getText().toString();
                request.setBody("password",password);
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
                        myHandler.sendEmptyMessage(LOGIN_SUC);
                    } else if (loginResponse.getCode() == BaseBizResponse.STATE_PW_ERR) {
                        //登录密码错误
                        myHandler.sendEmptyMessage(PW_ERROR);
                    } else {
                        myHandler.sendEmptyMessage(SERVER_FAIL);
                    }
                } else {
                    myHandler.sendEmptyMessage(SERVER_FAIL);
                }
            }
        }.start();
    }



    /**
     * 显示或影藏Loading
     */
    private void showOrHideLoading(boolean show) {
        if (show) {
            mLoading.setVisibility(View.VISIBLE);
            mBtnConfirm.setVisibility(View.GONE);
        } else {
            mLoading.setVisibility(View.GONE);
            mBtnConfirm.setVisibility(View.VISIBLE);
        }
    }


    /**
     * 显示登录成功
     */
    private void showLoginSuc() {
        mLoading.setVisibility(View.GONE);
        mBtnConfirm.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.color_text_normal));
        mTips.setText(getContext().getString(R.string.login_suc));
        ToastUtil.show(getContext(),getContext().getString(R.string.login_suc));
        dismiss();
    }

    /**
     * 显示服务器异常
     */
    private void showServerError() {
           showOrHideLoading(false);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
        mTips.setText(getContext().getString(R.string.error_server));
    }

    /**
     * 显示登录密码错误
     */
    private void showPasswordError() {
        showOrHideLoading(false);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
        mTips.setText(getContext().getString(R.string.password_error));
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
