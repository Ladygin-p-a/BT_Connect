package com.landay.bt_connect;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements GetBTDevCallBack{

    String MY_LOG = "myLog";
    private String choise_device="";
    final String CHOISE_DEVICE = "choise_device";
    public BluetoothDevice BTDev;

    Button btnFindBTDevice, btnFindNewDevice, btnSend;
    TextView textView2;
    TextView tvInputMessage;
    ListView lvDevicesList;
    EditText etTextMessage;
    TextView tvFindBTDevice;
    ProgressBar pbConnected;
    private ArrayAdapter<String> newDevicesArrayAdapter;
    private StringBuilder sb = new StringBuilder();

    public BTEngineClass btEngineClass;

    private ArrayAdapter<String> lvMessageListArrayAdapter;

    final int REQUEST_ENABLE_BT = 1;
    final int REQUEST_SELECT_BT_DEVICE = 2;
    final static int MESSAGE_RETURN_MAIN_ACTIVITY = 4;
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private Handler handler;

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main_menu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSend = (Button) findViewById(R.id.btnSend);
        etTextMessage = (EditText) findViewById(R.id.etTextMessage);
        tvFindBTDevice = (TextView) findViewById(R.id.tvFindBTDevice);
        ListView lvMessageList = (ListView) findViewById(R.id.lvMessageList);
        pbConnected = (ProgressBar) findViewById(R.id.pbConnected);
        pbConnected.setVisibility(ProgressBar.INVISIBLE);

        lvMessageListArrayAdapter = new ArrayAdapter<String>(this, R.layout.my_list_item);
        lvMessageList.setAdapter(lvMessageListArrayAdapter);

        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MessageConstants.MESSAGE_READ:
                        lvMessageListArrayAdapter.add((String) msg.obj);
                        /*byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);
                        sb.append(strIncom);                                                // формируем строку
                        int endOfLineIndex = sb.indexOf("\r\n");                            // определяем символы конца строки
                        if (endOfLineIndex > 0) {                                            // если встречаем конец строки,
                            String sbprint = sb.substring(0, endOfLineIndex);               // то извлекаем строку
                            sb.delete(0, sb.length());                                      // и очищаем sb
                            lvMessageListArrayAdapter.add(sbprint);
                        }*/
                        break;
                    case MessageConstants.TOAST_ERROR_CONNECT:
                        pbConnected.setVisibility(ProgressBar.INVISIBLE);
                        //tvFindBTDevice.setText(R.string.tvFindBTDevice);
                        tvFindBTDevice.setText("Ошибка подключения к "+BTDev.getName()+" "+BTDev.getAddress());
                        Toast.makeText(MainActivity.this, R.string.error_connect, Toast.LENGTH_LONG).show();
                        break;
                    case MessageConstants.TOAST_VALID_CONNECT:
//                        tvFindBTDevice.setText("Подключено к "+BTEngineClass.selectedBluetoothDevice.getName()+" "+BTEngineClass.selectedBluetoothDevice.getAddress());
//                        tvFindBTDevice.setText("Подключено к "+BTDev.getName()+" "+BTDev.getAddress());
//                        pbConnected.setVisibility(ProgressBar.INVISIBLE);
//                        Toast.makeText(MainActivity.this, R.string.valid_connect, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            };
        };

        btEngineClass = new BTEngineClass(handler);

        if (!btEngineClass.BluetoothAdapter_isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, btEngineClass.REQUEST_ENABLE_BT);
        }

//        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
//        choise_device = "";//sPref.getString(CHOISE_DEVICE, "");

    }

    @Override
    protected void onStop() {
        super.onStop();
//        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
//        SharedPreferences.Editor ed = sPref.edit();
//        ed.putString(CHOISE_DEVICE, choise_device);
//        ed.commit();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
//        SharedPreferences.Editor ed = sPref.edit();
//        ed.putString(CHOISE_DEVICE, choise_device);
//        ed.commit();

        btEngineClass.CancelToDevice();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_SELECT_BT_DEVICE){
            //tvFindBTDevice.setText(R.string.connecting);
            //pbConnected.setVisibility(ProgressBar.VISIBLE);
        }

    }

    public void onclick(View v) {
       switch (v.getId()) {
           case (R.id.tvFindBTDevice):
               //Вызываем активити для выбора устройства (показывает сопряженные устройства и поиск новых)
               MainActivityCtx.setCtx(MainActivity.this);
               Intent intent = new Intent(this, SelectBT_DeviceActivity.class);
               startActivityForResult(intent, REQUEST_SELECT_BT_DEVICE);
               break;
           case (R.id.btnSend):
               lvMessageListArrayAdapter.add("<<"+etTextMessage.getText().toString());
               String _str = etTextMessage.getText()+"\r\n";
               byte [] _text = _str.getBytes();
               btEngineClass.WriteToDevice(_text);
               etTextMessage.setText("");
               break;

           default:
               break;
       }
    }

    @Override
    public void response(BTDevModel btDevModel) {

        BTDev = btDevModel.getBTDev();
        setText();
        btEngineClass.ConnectToDevice(BTDev);
    }

    @Override
    public void getInputMessage(String str, int len) {

        Message readMsg = handler.obtainMessage(
                MessageConstants.MESSAGE_READ, len, -1,
                str);
        readMsg.sendToTarget();

//        byte[] readBuf = (byte[]) str;
//        String strIncom = new String(readBuf, 0, len);//msg.arg1);
//        sb.append(strIncom);                                                // формируем строку
//        int endOfLineIndex = sb.indexOf("\r\n");                            // определяем символы конца строки
//        if (endOfLineIndex > 0) {                                            // если встречаем конец строки,
//            String sbprint = sb.substring(0, endOfLineIndex);               // то извлекаем строку
//            sb.delete(0, sb.length());                                      // и очищаем sb
//            setText1(sbprint);
//        }
    }

    public void setText1(String sbprint){
        lvMessageListArrayAdapter.add(sbprint.toString());
    }
    public void setText(){
        //tvFindBTDevice.setText("666");
        tvFindBTDevice.setText("Подключено к "+BTDev.getName()+" "+BTDev.getAddress());
        pbConnected.setVisibility(ProgressBar.VISIBLE);
        Toast.makeText(MainActivity.this, R.string.valid_connect, Toast.LENGTH_SHORT).show();

    }


}
