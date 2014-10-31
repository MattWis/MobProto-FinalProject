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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

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
                    device.connectGatt(getActivity(), false, new BluetoothGattCallback() {
                        private ArrayList<BluetoothGattCharacteristic> mCharacteristics = new ArrayList<BluetoothGattCharacteristic>();
                        private ArrayList<UUID> mCharacteristicUUIDs = new ArrayList<UUID>();
                        private ArrayList<BluetoothGattDescriptor> mDescriptors = new ArrayList<BluetoothGattDescriptor>();
                        private ArrayList<UUID> mDescriptorUUIDs = new ArrayList<UUID>();
                        private int indexToRead = 0;
                        private int characteristicFlag = 0;

                        private HashMap<String, byte[]> characteristicMap = new HashMap<String, byte[]>();
                        private HashMap<String, byte[]> descriptorMap = new HashMap<String, byte[]>();

                        @Override
                        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                            super.onConnectionStateChange(gatt, status, newState);
                            String intentAction;
                            if (newState == BluetoothProfile.STATE_CONNECTED) {
                                Log.i(TAG, "Connected to GATT server.");
                                Log.i(TAG, "Attempting to start service discovery:" + gatt.discoverServices());

                            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                Log.i(TAG, "Disconnected from GATT server.");
                            }
                        }

                        @Override
                        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                            super.onServicesDiscovered(gatt, status);
                            for (BluetoothGattService service: gatt.getServices()) {
                                List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
                                for (BluetoothGattCharacteristic characteristic: characteristicList) {
                                    if (mCharacteristicUUIDs.indexOf(characteristic.getUuid()) == -1) {
                                        mCharacteristics.add(characteristic);
                                        mCharacteristicUUIDs.add(characteristic.getUuid());
                                    }

                                    List<BluetoothGattDescriptor> descriptorList = characteristic.getDescriptors();
                                    for (BluetoothGattDescriptor descriptor: descriptorList) {
                                        Log.d(TAG, descriptor.toString());
                                        if (mDescriptorUUIDs.indexOf(descriptor.getUuid()) == -1) {
                                            mDescriptors.add(descriptor);
                                            mDescriptorUUIDs.add(descriptor.getUuid());
                                        }
                                    }
                                }
                                readNextBLE(gatt);
                            }
                        }

                        @Override
                        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                            super.onCharacteristicRead(gatt, characteristic, status);
                            characteristicMap.put(characteristic.getUuid().toString(), characteristic.getValue());

                            readNextBLE(gatt);
                        }

                        @Override
                        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                            super.onDescriptorRead(gatt, descriptor, status);
                            descriptorMap.put(descriptor.getUuid().toString(), descriptor.getValue());

                            readNextBLE(gatt);
                        }

                        private void readNextBLE(BluetoothGatt gatt) {
                            if (characteristicFlag == 0) {
                                indexToRead = readNextCharacteristic(gatt, mCharacteristics, indexToRead);
                                if (indexToRead == -1) {
                                    characteristicFlag = 1;
                                    indexToRead = readNextDescriptor(gatt, mDescriptors, 0);
                                }
                            } else if (characteristicFlag == 1) {
                                indexToRead = readNextDescriptor(gatt, mDescriptors, indexToRead);
                                if (indexToRead == -1) {
                                    characteristicFlag = 2;
                                    bulkLog();
                                }

                            }
                        }

                        private int readNextCharacteristic(BluetoothGatt gatt, List<BluetoothGattCharacteristic> list, int index) {
                            while (list.size() > index && !gatt.readCharacteristic(list.get(index))) {
                                index += 1;
                                Log.d(TAG, "Failed Read");
                            }

                            return (list.size() > index) ? index + 1 : -1;
                        }

                        private int readNextDescriptor(BluetoothGatt gatt, List<BluetoothGattDescriptor> list, int index) {
                            while (list.size() > index && !gatt.readDescriptor(list.get(index))) {
                                index += 1;
                                Log.d(TAG, "Failed Read");
                            }

                            return (list.size() > index) ? index + 1 : -1;
                        }

                        private void bulkLog() {
                            Log.d(TAG, "Characteristics: ");
                            for (String uuid: characteristicMap.keySet()) {
                                Log.d(TAG, uuid);
                                logValue(characteristicMap.get(uuid));
                            }
                            Log.d(TAG, "Descriptors: ");
                            for (String uuid: descriptorMap.keySet()) {
                                Log.d(TAG, uuid);
                                logValue(descriptorMap.get(uuid));
                            }
                        }

                        private void logValue(byte[] val) {
                            if (val != null) {
                                String message = "";
                                for (byte item: val) {
                                    message += ", " + item;
                                }
                                Log.d(TAG, message);
                            } else {
                                Log.d(TAG, "Null value");
                            }
                        }
                    });
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
