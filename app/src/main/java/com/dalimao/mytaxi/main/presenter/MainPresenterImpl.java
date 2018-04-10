package com.dalimao.mytaxi.main.presenter;

import android.os.Handler;
import android.os.Message;

import com.dalimao.mytaxi.account.model.IAccountManager;
import com.dalimao.mytaxi.main.view.IMainView;

import java.lang.ref.WeakReference;

public class MainPresenterImpl implements IMainPresenter {

    private IMainView view;
    private IAccountManager accountManager;

    public MainPresenterImpl(IMainView view, IAccountManager accountManager) {
        this.view = view;
        this.accountManager = accountManager;
        accountManager.setHandler(new MyHandler(this));
    }

    /**
     * 接收子线程消息的 Handler
     */
    static class MyHandler extends Handler {

        // 弱引用

        WeakReference<MainPresenterImpl> dialogRef;
        public MyHandler(MainPresenterImpl presenter)
        {
            dialogRef = new WeakReference<MainPresenterImpl>(presenter);
        }
        @Override
        public void handleMessage(Message msg) {
            MainPresenterImpl presenter = dialogRef.get();
            if (presenter == null) {
                return;
            }
            // 处理UI 变化
            switch (msg.what) {
                case IAccountManager.LOGIN_SUC:
                    // 登录成功
                    presenter.view.showLoginSuc();
                    break;
                case IAccountManager.TOKEN_INVALID:
                    // 登录过期
                    presenter.view.showError(IAccountManager.TOKEN_INVALID, "");
                    break;
                case IAccountManager.SERVER_FAIL:
                    // 服务器错误
                    presenter.view.showError(IAccountManager.SERVER_FAIL, "");
                    break;
            }

        }
    }


    @Override
    public void loginByToken() {
           accountManager.loginByToken();
    }
}
