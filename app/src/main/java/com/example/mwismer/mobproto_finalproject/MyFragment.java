package com.example.mwismer.mobproto_finalproject;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by mwismer on 10/23/14.
 */
public class MyFragment extends Fragment{
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

        return rootView;
    }
}
