package com.landay.bt_connect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BTEngineClass{

    ConnectThread connectThread;
    public ReadWriteToBTDataThread readWriteToBTDataThread;
    Handler handler;
    String MY_LOG = "myLog";
    private static final String TAG = "MY_APP_DEBUG_TAG";
    public final int REQUEST_ENABLE_BT = 1;

    public static BluetoothAdapter bluetoothAdapter;
    public static BluetoothDevice selectedBluetoothDevice;
    public final static String ATTRIBUTE_NAME_TEXT = "text";
    public final static String ATTRIBUTE_BluetoothDevice = "BluetoothDevice";

    public BTEngineClass(Handler handler) {
        this.handler = handler;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    public static boolean BluetoothAdapter_isEnabled(){
        if (!bluetoothAdapter.isEnabled()) {
            return false;
        }
        return true;
    }

    public void ConnectToDevice(BluetoothDevice device){

        connectThread = new ConnectThread(device);
        connectThread.start();
    }

    public void CancelToDevice(){
        connectThread.cancel();
        readWriteToBTDataThread.cancel();
    }

    public void WriteToDevice(byte[] bytes){
        readWriteToBTDataThread.write(bytes);
    }


    public static void getPairDevices(ArrayList<Map<String, Object>> data){

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0){
            String[] names = new String[pairedDevices.size()];

            for (BluetoothDevice device: pairedDevices){
                String deviceName =  device.getName();
//                String deviceMAC  =  device.getAddress();
//                String _device = deviceName+" ("+deviceMAC+")";

                HashMap<String, Object> m = new HashMap<String, Object>();
                m.put(ATTRIBUTE_NAME_TEXT, deviceName.toString()+"  "+device.getAddress().toString());
                m.put(ATTRIBUTE_BluetoothDevice, new BTDevModel(device.getName().toString(), device.getAddress().toString(), device));
                data.add(m);

            }

        }

    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;


        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            //Оставим для пример
           /* try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
                Log.e(MY_LOG, "Socket's create() method failed", e);
            }

            mmSocket = tmp;*/


            Method m = null;
            try {
                m = mmDevice.getClass().getMethod("createRfcommSocket",new Class[] { int.class });
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            try {
                tmp = (BluetoothSocket)m.invoke(mmDevice, Integer.valueOf(1));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;

        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(MY_LOG, "Could not close the client socket", e);
            }
        }
        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                handler.sendEmptyMessage(MessageConstants.TOAST_ERROR_CONNECT);
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(MY_LOG, "Could not close the client socket", closeException);
                }
                return;
            }

            handler.sendEmptyMessage(MessageConstants.TOAST_VALID_CONNECT);

            readWriteToBTDataThread = new ReadWriteToBTDataThread(mmSocket);
            readWriteToBTDataThread.start();

        }

    }

    private class ReadWriteToBTDataThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream
        GetBTDevCallBack getBTDevCallBack;
        private boolean isConnected = false;

        public ReadWriteToBTDataThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            this.getBTDevCallBack = (GetBTDevCallBack) MainActivityCtx.getCtx();

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            isConnected = true;
        }

        public void run() {
            BufferedInputStream bis = new BufferedInputStream(mmInStream);
            StringBuffer buffer = new StringBuffer();
            final StringBuffer sbConsole = new StringBuffer();

            while (isConnected) {
                try {
                    int bytes = bis.read();
                    buffer.append((char) bytes);
                    int eof = buffer.indexOf("\r\n");

                    if (eof > 0) {
                        sbConsole.append(buffer.toString());
                        getBTDevCallBack.getInputMessage(buffer.toString(), buffer.length());
                        buffer.delete(0, buffer.length());

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                bis.close();
                cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = handler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                isConnected = false;
                mmSocket.close();
                mmInStream.close();
                mmOutStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}
