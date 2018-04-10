package com.dalimao.mytaxi.account.presenter;

import android.os.Message;

import com.dalimao.mytaxi.account.model.IAccountManager;
import com.dalimao.mytaxi.account.view.ISmsCodeDialogView;

import java.lang.ref.WeakReference;
import java.util.logging.Handler;

public class SmsCodeDialogPresenterImpl implements ISmsCodeDialogPresenter {

    private ISmsCodeDialogView view;
    private IAccountManager accountManager;

    /**
     * 接受消息并处理
     *
     */
    private static class MyHandler extends android.os.Handler {
        WeakReference<SmsCodeDialogPresenterImpl> refContext;

        public MyHandler(SmsCodeDialogPresenterImpl smsCodeDialogPresenter) {
            refContext = new WeakReference<>(smsCodeDialogPresenter);
        }

        @Override
        public void handleMessage(Message msg) {
            SmsCodeDialogPresenterImpl presenter = refContext.get();
            switch (msg.what) {
                case IAccountManager.SMS_SEND_SUC:
                    presenter.view.showCountDownTimer();
                    break;
                case IAccountManager.SMS_SEND_FAIL:
                    presenter.view.showError(IAccountManager.SMS_SEND_FAIL,"");
                    break;
                case IAccountManager.SMS_CHECK_SUC:
                    presenter.view.showSmsCodeCheckState(true);
                    break;
                case IAccountManager.SMS_CHECK_FAIL:
                    presenter.view.showError(IAccountManager.SMS_CHECK_FAIL, "");
                    break;
                case IAccountManager.USER_EXIST:
                    presenter.view.showUserExist(true);
                    break;
                case IAccountManager.USER_NOT_EXIST:
                    presenter.view.showUserExist(false);
                    break;
                case IAccountManager.SERVER_FAIL:
                    presenter.view.showError(IAccountManager.SERVER_FAIL, "");
                    break;
            }
        }
    }


    public SmsCodeDialogPresenterImpl(ISmsCodeDialogView view,
                                      IAccountManager accountManager) {
        this.view = view;
        this.accountManager = accountManager;
        accountManager.setHandler(new MyHandler(this));
    }

    /**
     *  请求下发验证码
     */
    @Override
    public void requestSendSmsCode(String phone) {
        accountManager.fetchSMSCode(phone);
    }

    /**
     * 请求校验验证码
     */
    @Override
    public void requestCheckSmsCode(String phone, String smsCode) {
        accountManager.checkSmsCode(phone,smsCode);
    }

    /**
     * 用户是否存在
     */
    @Override
    public void requestCheckUserExist(String phone) {
       accountManager.checkUserExist(phone);
    }
}
