package com.example.mwismer.mobproto_finalproject;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import com.firebase.client.Firebase;

/**
 * Created by mwismer on 11/3/14.
 */
public class BLEFinderCallback extends BluetoothGattCallback {
    private ArrayList<UUID> mUUIDs = new ArrayList<UUID>();
    private ArrayBlockingQueue<Runnable> infoToGet = new ArrayBlockingQueue<Runnable>(128);
    private boolean success = true;
    private String TAG = "BLEFinderCallback";
    private String deviceAddress;

    private HashMap<String, byte[]> characteristicMap = new HashMap<String, byte[]>();
    private HashMap<String, byte[]> descriptorMap = new HashMap<String, byte[]>();

    public BLEFinderCallback(BluetoothDevice device) {
        deviceAddress = device.getAddress();
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.i(TAG, "Connected to GATT server.");
            Log.i(TAG, "Attempting to start service discovery:" + gatt.discoverServices());

        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.i(TAG, "Disconnected from GATT server.");
        }
    }

    public void updateSuccess(boolean successful) { success = successful; }

    @Override
    public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        for (BluetoothGattService service: gatt.getServices()) {
            List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
            for (final BluetoothGattCharacteristic characteristic: characteristicList) {
                if (mUUIDs.indexOf(characteristic.getUuid()) == -1) {
                    infoToGet.add(new Runnable() {
                        @Override
                        public void run() {
                            updateSuccess(gatt.readCharacteristic(characteristic));
                        }
                    });
                    mUUIDs.add(characteristic.getUuid());
                }

                List<BluetoothGattDescriptor> descriptorList = characteristic.getDescriptors();
                for (final BluetoothGattDescriptor descriptor: descriptorList) {
                    if (mUUIDs.indexOf(descriptor.getUuid()) == -1) {
                        infoToGet.add(new Runnable() {
                            @Override
                            public void run() {
                                updateSuccess(gatt.readDescriptor(descriptor));
                            }
                        });
                        mUUIDs.add(descriptor.getUuid());
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
        while (!infoToGet.isEmpty()) {
            infoToGet.poll().run();
            if (success) { break; }
        }
        if (infoToGet.isEmpty()) {
            pushInfoToFirebase();
            bulkLog();
        }
    }

    private void pushInfoToFirebase() {
        Firebase currentDevice = new Firebase("https://mobproto-final.firebaseio.com/").child("devices").child(deviceAddress);
        //TODO: Put data from characteristicMap and deviceMap into the firebase at this child node
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
            Log.d(TAG, Arrays.toString(val));
        } else {
            Log.d(TAG, "Null value");
        }
    }
}

