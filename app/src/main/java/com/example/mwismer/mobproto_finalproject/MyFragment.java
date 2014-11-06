package com.example.mwismer.mobproto_finalproject;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Timer;
import java.util.TimerTask;

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
        // To detect hardware BLE:
        // if(getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {}
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBLEAdapter = bluetoothManager.getAdapter(); // Get device's "Bluetooth radio"
        if (mBLEAdapter == null || !mBLEAdapter.isEnabled()) { // If Bluetooth doesn't exist or is disabled
            Log.d(TAG, "Enabling Bluetooth");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            getActivity().startActivityForResult(enableBtIntent, 20); // REQUEST_ENABLE_BT is 20
            // REQUEST_ENABLE_BT will be the request code passed to onActivityResult callback in main activity
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
                mBLEAdapter.stopLeScan(mBLECallback); // Stop scanning
                Log.d(TAG, "BLE Scan finished");
                if (device == null) { // Didn't find a device in alotted time
                    Log.d(TAG, "No devices");
                } else {
                    // Connect to GATT server hosted by found device (autoConnect is off)
                    // Assign GATT callback handler to receive asynchronous callbacks
                    device.connectGatt(getActivity(), false, new BLEFinderCallback());
                }
            }
        };

        long SCAN_PERIOD = 10000; //Time to scan in ms
        timer.schedule(task, SCAN_PERIOD); // Schedule scan to stop after SCAN_PERIOD

        mScanning = true;
        mBLEAdapter.startLeScan(mBLECallback); // Start scanning
        Log.d(TAG, "BLE Scan Started");
    }
}
