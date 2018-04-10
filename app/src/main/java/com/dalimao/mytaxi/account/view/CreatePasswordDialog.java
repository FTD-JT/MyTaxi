package com.dalimao.mytaxi.account.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
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
import com.dalimao.mytaxi.common.util.DevUtil;
import com.dalimao.mytaxi.common.util.ToastUtil;
import com.google.gson.Gson;

import java.lang.ref.SoftReference;

import static com.dalimao.mytaxi.account.view.SmsCodeDialog.SMS_SEND_FAIL;

public class CreatePasswordDialog extends Dialog {
    private static final String TAG = CreatePasswordDialog.class.getSimpleName();
    private static final int REGISTER_SUC = 1;
    private static final int SERVER_FAIL = 100;
    private static final int LOGIN_SUC = 2;

    private TextView mPhone;
    private Button mBtnConfirm;
    private View mLoading;
    private TextView mPw;
    private TextView mPw1;
    private TextView mTips;
    private String mPhoneStr;
    private Object mContext;
    private MyHandler myHandler;
    private IHttpClient mHttpClient;


    public CreatePasswordDialog(@NonNull Context context,String phone) {
        super(context, R.style.Dialog);
        mPhoneStr = phone;
         mHttpClient = new OkHttpClientImpl();
        myHandler = new MyHandler(this);

    }

    public CreatePasswordDialog(Context context, int theme) {
        super(context, theme);

    }


    /**
     * 接收子线程消息的Handler
     */
    static class MyHandler extends Handler {
        //软引用
        SoftReference<CreatePasswordDialog> createSoftReference;

        public MyHandler(CreatePasswordDialog createPasswordDialog) {
            createSoftReference = new SoftReference<CreatePasswordDialog>(createPasswordDialog);
        }

        @Override
        public void handleMessage(Message msg) {
            CreatePasswordDialog createPasswordDialog = createSoftReference.get();
            if (createPasswordDialog == null) {
                return;
            }
            switch (msg.what) {
                case CreatePasswordDialog.REGISTER_SUC:
                   createPasswordDialog.showRegisterSuc();
                    break;
                case CreatePasswordDialog.SERVER_FAIL:
                    createPasswordDialog.showServerError();
                    break;
                case CreatePasswordDialog.LOGIN_SUC:
                    createPasswordDialog.showLoginSuc();
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = inflater.inflate(R.layout.dialog_create_pw, null);
        setContentView(root);
        initViews();
    }

    private void initViews() {
        mPhone = (TextView) findViewById(R.id.phone);
        mPw = (EditText) findViewById(R.id.pw);
        mPw1 = (EditText) findViewById(R.id.pw1);
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
     * 提交注册
     */
    private void submit() {
        if (checkPassword()) {
            final String password = mPw.getText().toString();
            final String phonePhone = mPhoneStr;
            //请求网络，提交注册
            new Thread(){
                @Override
                public void run() {
                    String url = API.Config.getDomain() + API.REGISTER;
                    IRequest request = new BaseRequest(url);
                    request.setBody("phone", phonePhone);
                    request.setBody("password",password);
                    request.setBody("uid",DevUtil.UUID(getContext()));
                    IResponse response = mHttpClient.post(request, false);
                    Log.d(TAG, response.getData());
                    if (response.getCode() == BaseResponse.STATE_OK) {
                        BaseBizResponse baseBizResponse = new Gson().fromJson(response.getData(), BaseBizResponse.class);
                        if (baseBizResponse.getCode() == BaseBizResponse.STATE_OK) {
                            myHandler.sendEmptyMessage(REGISTER_SUC);
                        } else {
                            myHandler.sendEmptyMessage(SERVER_FAIL);
                        }
                    } else {
                        myHandler.sendEmptyMessage(SERVER_FAIL);
                    }
                }
            }.start();
        }
    }

    /**
     * 检查密码输入
     * @return
     */
    private boolean checkPassword() {
        String password = mPw.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mTips.setVisibility(View.VISIBLE);
            mTips.setText(getContext().getString(R.string.password_is_null));
            mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
            return false;
        } else if (!password.equals(mPw1.getText().toString())){
            mTips.setVisibility(View.VISIBLE);
            mTips.setText(getContext().getString(R.string.password_is_not_equal));
            mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
            return false;
        }
        return true;
    }

    /**
     * 处理注册成功
     */
    private void showRegisterSuc() {
        mLoading.setVisibility(View.VISIBLE);
        mBtnConfirm.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.color_text_normal));
        mTips.setText(getContext().getString(R.string.register_suc_and_loging));
        //注册成功后,，完成自动登录
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
                        SharedPreferencesDao dao = new SharedPreferencesDao(MyTaxiApplication.getINSTANCE(),
                                SharedPreferencesDao.FILE_ACCOUNT);
                        dao.save(SharedPreferencesDao.KEY_ACCOUNT,account);
                        //通知UI登录成功
                        myHandler.sendEmptyMessage(LOGIN_SUC);
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
     * 显示服务器响应异常
     */
    private void showServerError(){
        mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
        mTips.setText(getContext().getResources().getString(R.string.error_server));
    }

    /**
     * 注册登录成功
     */
    private void showLoginSuc(){
        dismiss();
        ToastUtil.show(getContext(),getContext().getString(R.string.login_suc));
    }

}
