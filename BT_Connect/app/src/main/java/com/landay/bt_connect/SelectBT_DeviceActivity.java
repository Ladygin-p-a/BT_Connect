package com.landay.bt_connect;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.sql.RowSetListener;

public class SelectBT_DeviceActivity extends AppCompatActivity {

    private  GetBTDevCallBack getBTDevCallBack;
    String MY_LOG = "myLog";
    private BluetoothAdapter bluetoothAdapter;
    //private ArrayAdapter<BluetoothDevice> newDevicesArrayAdapter;
    private SimpleAdapter paired_devices_adapter, newDevicesArrayAdapter;
    final int REQUEST_ENABLE_BT = 1;
    private ListView lvNewDevices, lvPairedDevices;
    //private static final List<ArrayBTDevices> BTDevices = new ArrayList<ArrayBTDevices>();
    //ArrayAdapter<ArrayBTDevices> adapter;

    ArrayList<Map<String, Object>> dataToPaired_devices_adapter = new ArrayList<Map<String, Object>>();
    ArrayList<Map<String, Object>> dataToNew_devices_adapter = new ArrayList<Map<String, Object>>();

    //Map<String, Object> m;
    private Handler handler;

//    private class ArrayBTDevicesAdapter extends ArrayAdapter<ArrayBTDevices> {
//
//        public ArrayBTDevicesAdapter(Context context) {
//            super(context, R.layout.lay_new_devices, BTDevices);
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ArrayBTDevices arrayBTDevices = getItem(position);
//
//            if (convertView == null) {
//                convertView = LayoutInflater.from(getContext())
//                        .inflate(R.layout.lay_new_devices, null);
//            }
//            ((TextView) convertView.findViewById(R.id.textView1))
//                    .setText(arrayBTDevices.name);
//            ((TextView) convertView.findViewById(R.id.textView2))
//                    .setText(arrayBTDevices.bluetoothDevice.toString());
//            return convertView;
//        }
//    }

//    private static class ArrayBTDevices {
//        public final String name;
//        public final BluetoothDevice bluetoothDevice;
//
//        public ArrayBTDevices(String name, BluetoothDevice bluetoothDevice) {
//            this.name = name;
//            this.bluetoothDevice = bluetoothDevice;
//        }
//    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {

                    HashMap<String, Object>m = new HashMap<String, Object>();
                    m.put(BTEngineClass.ATTRIBUTE_NAME_TEXT, device.getName().toString()+"  "+device.getAddress().toString());
                    //m.put(BTEngineClass.ATTRIBUTE_BluetoothDevice, device);
                    m.put(BTEngineClass.ATTRIBUTE_BluetoothDevice, new BTDevModel(device.getName().toString(), device.getAddress().toString(), device));
                    dataToNew_devices_adapter.add(m);
                    newDevicesArrayAdapter.notifyDataSetChanged();
                    //BTDevices.add(new ArrayBTDevices(device.getName().toString(), device));
                    //adapter.notifyDataSetChanged();
                    //newDevicesArrayAdapter.add(device);
//                    HashMap<String, Object>m = new HashMap<String, Object>();
//                    m.put(BTEngineClass.ATTRIBUTE_NAME_TEXT, device.getName().toString());
//                    m.put(BTEngineClass.ATTRIBUTE_BluetoothDevice, device);
//                    dataToNew_devices_adapter.add(m);

//                    String deviceName = device.getName();
//                    String deviceMAC = device.getAddress();
//                    UUID uuid = null;
//                    ParcelUuid[] uuids = device.getUuids();
//                    if (uuids != null) {
//                        uuid = uuids[0].getUuid();
//                    }
//                    String uuid_str = "";
//                    if (uuid != null) {
//                        uuid_str = uuid.toString();
//                    }
//                    String _device = deviceName + " (" + deviceMAC + ")" + " (" + uuid_str + ")";
//                    newDevicesArrayAdapter.add(_device);
                }
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                handler.sendEmptyMessage(MessageConstants.MESSAGE_PB_INVISIBLE);
            }

        }
    };

    private AdapterView.OnItemClickListener lvDevicesList_ItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//            //BluetoothDevice _device = newDevicesArrayAdapter.getItem(position);
//            ArrayBTDevices _device = adapter.getItem(position);
//            BTEngineClass.selectedBluetoothDevice = _device.bluetoothDevice;

            Object __device = newDevicesArrayAdapter.getItem(position);
            //BluetoothDevice _device = (BluetoothDevice)((HashMap) __device).get(BTEngineClass.ATTRIBUTE_BluetoothDevice);
            //BTEngineClass.selectedBluetoothDevice = _device;
            BTDevModel _device = (BTDevModel)((HashMap) __device).get(BTEngineClass.ATTRIBUTE_BluetoothDevice);

            getBTDevCallBack.response(_device);

            Intent intent = new Intent();
            //intent.putExtra("device", (String) msg.obj);
            setResult(RESULT_OK, intent);
            finish();



        }
    };

    private AdapterView.OnItemClickListener lvPairedDevicesList_ItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            //BluetoothDevice __device = PairedDevicesArrayAdapter.getItem(position);

            Object __device = paired_devices_adapter.getItem(position);
            //BluetoothDevice _device = (BluetoothDevice)((HashMap) __device).get(BTEngineClass.ATTRIBUTE_BluetoothDevice);
            //BTEngineClass.selectedBluetoothDevice = _device;
            BTDevModel _device = (BTDevModel)((HashMap) __device).get(BTEngineClass.ATTRIBUTE_BluetoothDevice);

//            String deviceName = _device.getName();
//            String deviceMAC = _device.getAddress();
//
//            String str_device = deviceName + " (" + deviceMAC + ")";

            Intent intent = new Intent();
            //intent.putExtra("device", (String) msg.obj);
            setResult(RESULT_OK, intent);
            finish();

            getBTDevCallBack.response(_device);

            //Message msg = handler.obtainMessage(MessageConstants.MESSAGE_RETURN_MAIN_ACTIVITY, str_device);
            //handler.sendMessage(msg);

            //connectThread = new ConnectThread(_device);
            //connectThread.start();

        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothAdapter.cancelDiscovery();
        this.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_b_t__device);

        this.getBTDevCallBack = (GetBTDevCallBack) MainActivityCtx.getCtx();

        bluetoothAdapter = BTEngineClass.bluetoothAdapter;
        //adapter = new ArrayBTDevicesAdapter(this);

        //Оперделяем элементы формы
        final ProgressBar pbFindNewDevice = (ProgressBar) findViewById(R.id.pbFindNewDevice);
        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        lvPairedDevices = (ListView) findViewById(R.id.lvPairedDevices);
        pbFindNewDevice.setVisibility(ProgressBar.VISIBLE);

        //Прячем анимацию прогрессбар когда закончился поиск новых устройств
        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MessageConstants.MESSAGE_PB_INVISIBLE:
                        pbFindNewDevice.setVisibility(ProgressBar.INVISIBLE);
                        break;
//                    case MessageConstants.MESSAGE_RETURN_MAIN_ACTIVITY:
//                        //connectThread.cancel();
//                        bluetoothAdapter.cancelDiscovery();
//                        getPairDevices();
//
//                        Intent intent = new Intent();
//                        intent.putExtra("device", (String) msg.obj);
//                        setResult(RESULT_OK, intent);
//                        finish();
//                        break;
//                    case MessageConstants.TOAST_ERROR_CONNECT:
//                        Toast.makeText(SelectBT_DeviceActivity.this, R.string.error_connect, Toast.LENGTH_LONG).show();
//                        break;
                    default:
                        break;
                }
            };
        };

        if (bluetoothAdapter != null){
            if (BTEngineClass.BluetoothAdapter_isEnabled()){

                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                this.registerReceiver(mReceiver, filter);

                filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                this.registerReceiver(mReceiver, filter);

                //newDevicesArrayAdapter = new ArrayAdapter<BluetoothDevice>(this,  R.layout.lay_new_devices);
                String[] from = { BTEngineClass.ATTRIBUTE_NAME_TEXT};//, BTEngineClass.ATTRIBUTE_BluetoothDevice};
                // массив ID View-компонентов, в которые будут вставлять данные
                int[] to = { R.id.textView1};//,R.id.textView2 };

                // создаем адаптер
                newDevicesArrayAdapter = new SimpleAdapter(this, dataToNew_devices_adapter, R.layout.lay_new_devices,
                        from, to);

                lvNewDevices.setAdapter(newDevicesArrayAdapter);
                //lvNewDevices.setAdapter(adapter);
                lvNewDevices.setOnItemClickListener(lvDevicesList_ItemClickListener);

                //PairedDevicesArrayAdapter = new ArrayAdapter<BluetoothDevice>(this, R.layout.my_list_item);
                //lvPairedDevices.setAdapter(PairedDevicesArrayAdapter);
                //lvPairedDevices.setOnItemClickListener(lvPairedDevicesList_ItemClickListener);

                // массив имен атрибутов, из которых будут читаться данные

                // создаем адаптер
                paired_devices_adapter = new SimpleAdapter(this, dataToPaired_devices_adapter, R.layout.lay_paired_devices,
                        from, to);

                lvPairedDevices.setAdapter(paired_devices_adapter);
                lvPairedDevices.setOnItemClickListener(lvPairedDevicesList_ItemClickListener);

                BTEngineClass.getPairDevices(dataToPaired_devices_adapter);

                //newDevicesArrayAdapter.clear();
                if (bluetoothAdapter.isDiscovering()){
                    bluetoothAdapter.cancelDiscovery();
                }
                bluetoothAdapter.startDiscovery();
            }

        } else {
            Toast.makeText(this, "Bluetooth на устройстве не установлен", Toast.LENGTH_LONG).show();
        }

    }

}
