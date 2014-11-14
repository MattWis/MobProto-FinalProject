package com.example.mwismer.mobproto_finalproject;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.shaded.fasterxml.jackson.core.ObjectCodec;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by mwismer on 11/3/14.
 */
public final class BLEScanner implements PreferenceManager.OnActivityResultListener{

    private static int ENABLE_BLE = 21305;
    private HashMap<String, Object> whiteList;
    private BluetoothDevice device = null;
    private BluetoothAdapter mBLEAdapter = null;
    private Activity activity;
    private Fragment fragment;
    private static String TAG = "BLEScanner";
    private BluetoothAdapter.LeScanCallback mBLECallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            if (device == null || !bluetoothDevice.equals(device)) {
                if (whiteList == null || whiteList.containsKey(bluetoothDevice.getAddress())) {
                    Log.d(TAG, "Resetting device");
                    device = bluetoothDevice;
                }
            }
        }
    };

    public BLEScanner(Fragment currentFragment) {
        activity = currentFragment.getActivity();
        fragment = currentFragment;
    }

    public boolean checkBLEEnabled() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBLEAdapter = bluetoothManager.getAdapter();
        return (mBLEAdapter != null && mBLEAdapter.isEnabled());
    }

    public void enableBLE() {
        Log.d(TAG, "Enabling Bluetooth");
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        fragment.startActivityForResult(enableBtIntent, ENABLE_BLE);
    }

    public void scanBLE() {
        if (checkBLEEnabled()) {
            scanBLEUnsafe();
        } else {
            enableBLE();
        }
    }

    public void scanBLEUnsafe() {
        new FirebaseUtils().getWhiteList(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "WhiteList: " + dataSnapshot.getValue().toString());
                Object data = dataSnapshot.getValue();
                if (data.getClass().toString().equals("class java.util.HashMap")) {
                    whiteList = (HashMap<String, Object>) data;
                    startScan();
                } else {
                    Log.d(TAG, data.getClass().toString());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }


    public void startScan() {
        if (whiteList == null) {
            Log.d(TAG, "This function doesn't do anything when called with null argument");
        } else {
            final Timer timer = new Timer();
            final TimerTask endScan = new TimerTask() {
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
            timer.schedule(endScan, SCAN_PERIOD);

            mBLEAdapter.startLeScan(mBLECallback);
            Log.d(TAG, "BLE Scan Started");
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ENABLE_BLE) {
            Log.d(TAG, "BLE Enabled. Trying to scan again");
            scanBLE();
            return true;
        }
        return false;
    }
}
