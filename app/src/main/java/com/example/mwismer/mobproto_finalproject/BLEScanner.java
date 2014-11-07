package com.example.mwismer.mobproto_finalproject;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by mwismer on 11/3/14.
 */
public final class BLEScanner {

    private BluetoothDevice device = null;
    private Activity activity;
    private static String TAG = "BLEScanner";
    private BluetoothAdapter.LeScanCallback mBLECallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
        if (device == null || !bluetoothDevice.equals(device)) {
            Log.d(TAG, "Resetting device");
            device = bluetoothDevice;
        }
        }
    };

    public BLEScanner(Activity context) { activity = context; }

    public void scanBLE() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter mBLEAdapter = bluetoothManager.getAdapter();
        if (mBLEAdapter == null || !mBLEAdapter.isEnabled()) {
            Log.d(TAG, "Enabling Bluetooth");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, 20);
        }

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                mBLEAdapter.stopLeScan(mBLECallback);
                Log.d(TAG, "BLE Scan finished");
                if (device == null) {
                    Log.d(TAG, "No devices");
                } else {
                    device.connectGatt(activity, false, new BLEFinderCallback(device));
                }
            }
        };

        long SCAN_PERIOD = 10000; //Time to scan in ms
        timer.schedule(task, SCAN_PERIOD);

        mBLEAdapter.startLeScan(mBLECallback);
        Log.d(TAG, "BLE Scan Started");
    }
}
