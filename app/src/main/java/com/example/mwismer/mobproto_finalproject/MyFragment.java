package com.example.mwismer.mobproto_finalproject;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by mwismer on 10/23/14.
 */
public class MyFragment extends Fragment{
    private BluetoothDevice device = null;
    private String TAG = "MyFragment";
    private BluetoothAdapter mBLEAdapter;

    public MyFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my, container, false);
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBLEAdapter = bluetoothManager.getAdapter();
        if (mBLEAdapter == null || !mBLEAdapter.isEnabled()) {
            Log.d(TAG, "Enabling Bluetooth");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            getActivity().startActivityForResult(enableBtIntent, 20);
        }

        scanBLE();

        return rootView;
    }

    private boolean mScanning = false;
    private BluetoothAdapter.LeScanCallback mBLECallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            if (device == null || !bluetoothDevice.equals(device)) {
                Log.d(TAG, "Resetting device");
                device = bluetoothDevice;
            }
        }
    };

    private void scanBLE() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                mScanning = false;
                mBLEAdapter.stopLeScan(mBLECallback);
                Log.d(TAG, "BLE Scan finished");
                if (device == null) {
                    Log.d(TAG, "No devices");
                } else {
                    device.connectGatt(getActivity(), false, new BLEFinderCallback());
                }
            }
        };

        long SCAN_PERIOD = 10000; //Time to scan in ms
        timer.schedule(task, SCAN_PERIOD);

        mScanning = true;
        mBLEAdapter.startLeScan(mBLECallback);
        Log.d(TAG, "BLE Scan Started");
    }
}
