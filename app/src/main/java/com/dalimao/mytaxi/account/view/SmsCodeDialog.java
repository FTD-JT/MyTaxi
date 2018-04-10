package com.dalimao.mytaxi.account.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dalimao.corelibrary.VerificationCodeInput;
import com.dalimao.mytaxi.R;
import com.dalimao.mytaxi.common.http.IHttpClient;
import com.dalimao.mytaxi.common.http.IRequest;
import com.dalimao.mytaxi.common.http.IResponse;
import com.dalimao.mytaxi.common.http.api.API;
import com.dalimao.mytaxi.common.http.biz.BaseBizResponse;
import com.dalimao.mytaxi.common.http.impl.BaseRequest;
import com.dalimao.mytaxi.common.http.impl.BaseResponse;
import com.dalimao.mytaxi.common.http.impl.OkHttpClientImpl;
import com.dalimao.mytaxi.common.util.ToastUtil;
import com.dalimao.mytaxi.main.view.MainActivity;
import com.google.gson.Gson;

import java.lang.ref.SoftReference;

public class SmsCodeDialog extends Dialog implements  ISmsCodeDialogView {

    private static final String TAG = "SmsCodeDialog";

    private String mPhone;
    private Button mResentBtn;
    private VerificationCodeInput mVerificationInput;
    private View mLoading;
    private View mErrorView;
    private TextView mPhoneTv;
    //    private ISmsCodeDialogPresenter mPresenter;
    private MainActivity mainActivity;
    private IHttpClient mHttpClient;
    private MyHandler myHandler;
    public static final int SMS_SEND_SUC = 1;
    public static final int SMS_SEND_FAIL = -1;

    public static final int SMS_CHECK_SUC = 2;
    public static final int SMS_CHECK_FAIL = -2;

    private static final int USER_EXIST = 3;
    private static final int USER_NOT_EXIST = -3;
    //服务器异常
    private static final int SMS_SERVER_FAIL = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = inflater.inflate(R.layout.dialog_smscode_input, null);
        setContentView(root);
        mPhoneTv = (TextView) findViewById(R.id.phone);
        String template = getContext().getString(R.string.sending);
        mPhoneTv.setText(String.format(template, mPhone));
        mResentBtn = (Button) findViewById(R.id.btn_resend);
        mVerificationInput = (VerificationCodeInput) findViewById(R.id.verificationCodeInput);
        mLoading = findViewById(R.id.loading);
        mErrorView = findViewById(R.id.error);
        mErrorView.setVisibility(View.GONE);
        //设置监听器
        initListeners();
        requestSendSmsCode();
    }

    /**
     * 接收子线程消息的Handler
     */
    static class MyHandler extends Handler {
        //软引用
        SoftReference<SmsCodeDialog> smsSoftReference;

        public MyHandler(SmsCodeDialog smsCodeDialog) {
            smsSoftReference = new SoftReference<SmsCodeDialog>(smsCodeDialog);
        }

        @Override
        public void handleMessage(Message msg) {
            //通过软引用获得smsCodeDialog
            SmsCodeDialog smsCodeDialog = smsSoftReference.get();
            if (smsCodeDialog == null) {
                return;
            }
            //处理ui变化
            switch (msg.what) {
                case SmsCodeDialog.SMS_SEND_SUC:
                    //验证码倒计时启动
                    smsCodeDialog.mCountDownTimer.start();
                    break;
                case SmsCodeDialog.SMS_SEND_FAIL:
                    ToastUtil.show(smsCodeDialog.getContext(), smsCodeDialog.getContext().getString(R.string.sms_send_fail));
                    break;
                case SmsCodeDialog.SMS_CHECK_SUC:
                    //验证码验证成功
                    smsCodeDialog.showVerifyState(true);
                    break;
                case SmsCodeDialog.SMS_CHECK_FAIL:
                    //验证码验证失败
                    smsCodeDialog.showVerifyState(false);
                    break;
                case SmsCodeDialog.USER_EXIST:
                    smsCodeDialog.showUserIsExist(true);
                    break;
                case SmsCodeDialog.USER_NOT_EXIST:
                    smsCodeDialog.showUserIsExist(false);
                    break;
                case SmsCodeDialog.SMS_SERVER_FAIL:
                    ToastUtil.show(smsCodeDialog.getContext(),smsCodeDialog.getContext().getString(R.string.error_server));
                    break;
                default:
                    break;
            }
        }

    }



       /**
         * 检查验证码校验状态
         */
        private void showVerifyState(boolean suc) {

            if (!suc) {
                //提示验证码错误
                mErrorView.setVisibility(View.VISIBLE);
                mVerificationInput.setEnabled(true);
                mLoading.setVisibility(View.GONE);
            } else {
                mErrorView.setVisibility(View.GONE);
                mLoading.setVisibility(View.VISIBLE);
                //检查用户是否存在
                new Thread(){
                    @Override
                    public void run() {
                        String url = API.Config.getDomain() + API.CHECK_USER_EXIST;
                        IRequest request = new BaseRequest(url);
                        request.setBody("phone", mPhone);
                        IResponse response = mHttpClient.get(request, false);
                        Log.d(TAG, response.getData());
                        if (response.getCode() == BaseResponse.STATE_OK) {
                            BaseBizResponse baseBizResponse = new Gson().fromJson(response.getData(), BaseBizResponse.class);
                            if (baseBizResponse.getCode() == BaseBizResponse.STATE_USER_EXIST) {
                                myHandler.sendEmptyMessage(USER_EXIST);
                            } else if (baseBizResponse.getCode() == BaseBizResponse.STATE_USER_NOT_EXIST){
                                myHandler.sendEmptyMessage(USER_NOT_EXIST);
                            }
                        } else {
                            myHandler.sendEmptyMessage(SMS_SERVER_FAIL);
                        }
                    }
                }.start();
            }
        }

    /**
     * 检查用户是否存在
     */
    private void showUserIsExist(boolean exist) {
           mErrorView.setVisibility(View.GONE);
           mLoading.setVisibility(View.GONE);
           dismiss();
        if (!exist) {
          //用户不存在，进入注册
            CreatePasswordDialog createPasswordDialog = new CreatePasswordDialog(getContext(), mPhone);
            createPasswordDialog.show();
          } else {
          //用户存在，进入登录
            LoginDialog loginDialog = new LoginDialog(getContext(), mPhone);
            loginDialog.show();
          }
        }

    /**
     * 请求下发验证码
     */
    private void requestSendSmsCode() {
          new Thread(){
              @Override
              public void run() {
                  String url = API.Config.getDomain() + API.GET_SMS_CODE;
                  IRequest request = new BaseRequest(url);
                  request.setBody("phone", mPhone);
                  IResponse response = mHttpClient.get(request, false);
                  Log.d(TAG, response.getData());
                  if (response.getCode() == BaseResponse.STATE_OK) {
                      BaseBizResponse baseBizResponse = new Gson().fromJson(response.getData(), BaseBizResponse.class);
                      if (baseBizResponse.getCode() == BaseBizResponse.STATE_OK) {
                          myHandler.sendEmptyMessage(SMS_SEND_SUC);
                      } else {
                          myHandler.sendEmptyMessage(SMS_SEND_FAIL);
                      }
                  } else {
                      myHandler.sendEmptyMessage(SMS_SEND_FAIL);
                  }
              }
          }.start();
    }

    private void initListeners() {

        //  关闭按钮组册监听器
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // 重发验证码按钮注册监听器
        mResentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resend();
            }
        });

        // 验证码输入完成监听器
        mVerificationInput.setOnCompleteListener(new VerificationCodeInput.Listener() {
            @Override
            public void onComplete(String code) {
                commit(code);
            }
        });
    }

    /**
     * 重新发送验证码
     */
    private void resend() {
        String template = getContext().getString(R.string.sending);
        mPhoneTv.setText(String.format(template, mPhone));
    }


    /**
     * 验证码输入完成后提交进行验证
     * @param code
     */
    private void commit(final String code) {
        //显示进度条
        mLoading.setVisibility(View.VISIBLE);

        //网络请求校验验证码
        new Thread(){
            @Override
            public void run() {
                String url = API.Config.getDomain() + API.CHECK_SMS_CODE;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", mPhone);
                request.setBody("code", code);
                IResponse response = mHttpClient.get(request, false);
                Log.d(TAG, response.getData());
                if (response.getCode() == BaseResponse.STATE_OK) {
                    BaseBizResponse baseBizResponse = new Gson().fromJson(response.getData(), BaseBizResponse.class);
                    if (baseBizResponse.getCode() == BaseBizResponse.STATE_OK) {
                        myHandler.sendEmptyMessage(SMS_CHECK_SUC);
                    } else {
                        myHandler.sendEmptyMessage(SMS_CHECK_FAIL);
                    }
                } else {
                    myHandler.sendEmptyMessage(SMS_CHECK_FAIL);
                }
            }
        }.start();

    }

    public SmsCodeDialog(MainActivity context, String phone) {
        this(context, R.style.Dialog);
        this.mPhone = phone;
        mHttpClient = new OkHttpClientImpl();
        myHandler = new MyHandler(this);
    }


    public SmsCodeDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected SmsCodeDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    /**
     * 验证码倒计时
     */
    private CountDownTimer mCountDownTimer = new CountDownTimer(10000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            mResentBtn.setEnabled(false);
            mResentBtn.setText(String.format(getContext()
                    .getString(R.string.after_time_resend),millisUntilFinished/1000));

        }

        @Override
        public void onFinish() {
            mResentBtn.setEnabled(true);
            mResentBtn.setText(getContext().getString(R.string.resend));
            cancel();
        }
    };


    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mCountDownTimer.cancel();
    }

    @Override
    public void showCountDownTimer() {

    }

    @Override
    public void showSmsCodeCheckState(boolean b) {

    }

    @Override
    public void showUserExist(boolean b) {

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void showError(int Code, String msg) {

    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

}