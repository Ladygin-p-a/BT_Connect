package com.landay.bt_connect;

import android.content.Context;

public class MainActivityCtx {
    private static Context Ctx;

    public static Context getCtx() {
        return Ctx;
    }

    public static void setCtx(Context ctx) {
        Ctx = ctx;
    }
}
