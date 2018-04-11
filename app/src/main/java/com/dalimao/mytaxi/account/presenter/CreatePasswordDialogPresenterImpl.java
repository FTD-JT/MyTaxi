package com.dalimao.mytaxi.account.presenter;

import android.os.Handler;
import android.os.Message;

import com.dalimao.mytaxi.account.model.IAccountManager;
import com.dalimao.mytaxi.account.model.response.LoginResponse;
import com.dalimao.mytaxi.account.model.response.RegisterResponse;
import com.dalimao.mytaxi.account.view.ICreatePasswordDialogView;
import com.dalimao.mytaxi.common.databus.RegisterBus;

import java.lang.ref.WeakReference;

public class CreatePasswordDialogPresenterImpl implements ICreatePasswordDialogPresenter {

    private ICreatePasswordDialogView view;
    private IAccountManager accountManager;


    public CreatePasswordDialogPresenterImpl(ICreatePasswordDialogView view, IAccountManager accountManager) {
        this.view = view;
        this.accountManager = accountManager;
//        accountManager.setHandler(new MyHandler(this));
    }

//    /**
//     * 接收子线程消息的 Handler
//     */
//    public static class MyHandler extends Handler {
//        // 软引用
//
//        WeakReference<CreatePasswordDialogPresenterImpl> codeDialogRef;
//
//        public MyHandler(CreatePasswordDialogPresenterImpl presenter) {
//            codeDialogRef =
//                    new WeakReference<CreatePasswordDialogPresenterImpl>(presenter);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            CreatePasswordDialogPresenterImpl presenter = codeDialogRef.get();
//            if (presenter == null) {
//                return;
//            }
//            // 处理UI 变化
//            switch (msg.what) {
//                case IAccountManager.REGISTER_SUC:
//                    // 注册成功
//                    presenter.view.showRegisterSuc();
//                    break;
//                case IAccountManager.LOGIN_SUC:
//                    // 登录成功
//                    presenter.view.showLoginSuc();
//                    break;
//                case IAccountManager.SERVER_FAIL:
//                    // 服务器错误
//                    presenter.view.showError(IAccountManager.SERVER_FAIL, "");
//                    break;
//            }
//
//        }
//    }


    @RegisterBus
    public void onRegisterResponse(RegisterResponse registerResponse) {
        // 处理UI 变化
        switch (registerResponse.getCode()) {
            case IAccountManager.REGISTER_SUC:
                // 注册成功
                view.showRegisterSuc();
                break;
            case IAccountManager.LOGIN_SUC:
                // 登录成功
                view.showLoginSuc();
                break;
            case IAccountManager.SERVER_FAIL:
                // 服务器错误
                view.showError(IAccountManager.SERVER_FAIL, "");
                break;
        }

    }

    @RegisterBus
    public void onLoginResponse(LoginResponse LoginResponse) {
        // 处理UI 变化
        switch (LoginResponse.getCode()) {
            case IAccountManager.LOGIN_SUC:
                // 登录成功
                view.showLoginSuc();
                break;
            case IAccountManager.SERVER_FAIL:
                // 服务器错误
                view.showError(IAccountManager.SERVER_FAIL, "");
                break;
        }

    }



    @Override
    public boolean checkPw(String pw, String pw1) {

        if (pw == null || pw.equals("")) {

            view.showPasswordNull();
            return false;
        }
        if (!pw.equals(pw1)) {

            view.showPasswordNotEqual();
            return false;
        }
        return true;
    }

    @Override
    public void requestRegister(String phone, String pw) {
        accountManager.register(phone,pw);
    }

    @Override
    public void requestLogin(String phone, String pw) {
        accountManager.login(phone,pw);
    }
}
