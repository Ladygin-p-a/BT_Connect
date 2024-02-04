package com.landay.bt_connect;

import android.bluetooth.BluetoothDevice;

public class BTDevModel {
   private String name;
   private String MACAdr;
   private BluetoothDevice BTDev;

    public BTDevModel(String name, String MACAdr, BluetoothDevice BTDev) {
        this.name = name;
        this.MACAdr = MACAdr;
        this.BTDev = BTDev;
    }


    public String getName() {
        return name;
    }

    public String getMACAddr() {
        return MACAdr;
    }

    public BluetoothDevice getBTDev() {
        return BTDev;
    }
}
