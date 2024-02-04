package com.landay.bt_connect;

import android.bluetooth.BluetoothDevice;

public interface GetBTDevCallBack {
    void response(BTDevModel btDevModel);
    //void getInputMessage(byte[] str, int len);
    void getInputMessage(String str, int len);

}
