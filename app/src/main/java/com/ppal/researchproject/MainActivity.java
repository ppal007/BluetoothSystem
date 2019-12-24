package com.ppal.researchproject;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.switch_bluetooth)
    Switch switchBluetooth;
    @BindView(R.id.button_discoverable)
    Button buttonDiscoverable;
    @BindView(R.id.listview_pair)
    ListView listviewPair;
    @BindView(R.id.listview_scan)
    ListView listviewScan;
    @BindView(R.id.tv_discovereble_title)
    TextView tvDiscoverebleTitle;


    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_CODE_FOR_ENABLE = 1;
    private Intent bluetooth_enabling_intent;

    ArrayList<String> arrayList = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;

    IntentFilter scanIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //bluetooth adapter initialize
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetooth_enabling_intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        //switch ON/OFF
        switchBluetooth.setOnClickListener(v -> {
            if (switchBluetooth.isChecked()) {
                bluetoothOnMethod();

            } else {
                bluetoothOffMethod();
            }
        });

        //discoverable method
        discoverable();


    }

    //discoverable method
    private void discoverable() {
        buttonDiscoverable.setOnClickListener(v -> {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 10);
            startActivity(intent);
        });

        registerReceiver(scanBrodcastReceiver, scanIntentFilter);
    }

    //receiver for discoverable
   private final BroadcastReceiver scanBrodcastReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int modeValue = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                if (modeValue == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
                    tvDiscoverebleTitle.setText("The device is not in discoverable mode but receive connections");
                } else if (modeValue == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    tvDiscoverebleTitle.setText("The device is in discoverable");
                } else if (modeValue == BluetoothAdapter.SCAN_MODE_NONE) {
                    tvDiscoverebleTitle.setText("The device is not in discoverable mode and not receive connections");
                } else {
                    tvDiscoverebleTitle.setText("Error");
                }
            }
        }
    };

    //scan device method
    private void scanDevice() {

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(myReceiver, intentFilter);

        bluetoothAdapter.startDiscovery();
        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.listview_sample,R.id.tv_text_sample, arrayList);
        listviewScan.setAdapter(arrayAdapter);
    }
    //receiver for scan device
   private final BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                assert device != null;
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                arrayList.add(deviceName + " " + deviceHardwareAddress);
                arrayAdapter.notifyDataSetChanged();
            }
        }
    };

    //switch OFF method
    private void bluetoothOffMethod() {
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();

        }
    }
    //switch ON method
    private void bluetoothOnMethod() {
        if (bluetoothAdapter == null) {
            Toast.makeText(MainActivity.this, "Bluetooth is not support in your device", Toast.LENGTH_SHORT).show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                startActivityForResult(bluetooth_enabling_intent, REQUEST_CODE_FOR_ENABLE);

            }
        }
    }
    //for switch ON method
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FOR_ENABLE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth is enable", Toast.LENGTH_SHORT).show();
                //show pair device list
                Set<BluetoothDevice> bluetoothDevices = bluetoothAdapter.getBondedDevices();
                String[] strings = new String[bluetoothDevices.size()];
                int index = 0;
                if (bluetoothDevices.size() > 0) {
                    for (BluetoothDevice device : bluetoothDevices) {
                        strings[index] = device.getName();
                        index++;
                    }

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.listview_sample, R.id.tv_text_sample, strings);
                    listviewPair.setAdapter(arrayAdapter);
                    //scan device
                    scanDevice();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth enabling cancel", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
