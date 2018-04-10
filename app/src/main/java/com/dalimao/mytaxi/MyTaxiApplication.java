package com.dalimao.mytaxi;

import android.app.Application;

public class MyTaxiApplication extends Application {

    private static MyTaxiApplication INSTANCE;


    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }


    public static MyTaxiApplication getInstance() {
        return INSTANCE;
    }
}
