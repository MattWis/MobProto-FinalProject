package com.example.mwismer.mobproto_finalproject;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import com.firebase.client.Firebase;

/**
 * Created by mwismer on 11/3/14.
 */
 // Callback used to get us the results from the server (device)
public class BLEFinderCallback extends BluetoothGattCallback {
    private ArrayList<UUID> mUUIDs = new ArrayList<UUID>();
    private ArrayBlockingQueue<Runnable> infoToGet = new ArrayBlockingQueue<Runnable>(128);
    private boolean success = true;
    private String TAG = "BLEFinderCallback";
    private String deviceAddress;

    private HashMap<String, byte[]> valueMap = new HashMap<String, byte[]>();

    public BLEFinderCallback(BluetoothDevice device) { deviceAddress = device.getAddress(); }

    // Client connection state has changed
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

    // Server's list of (remote services, characteristics and descriptors) for the remote device (server) has been updated
    @Override
    public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        
        // Check each service within device...
        for (BluetoothGattService service: gatt.getServices()) {
            // List of characteristics in service
            List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
            
            // Check each characteristic within each service...
            for (final BluetoothGattCharacteristic characteristic: characteristicList) {
                if (mUUIDs.indexOf(characteristic.getUuid()) == -1) { // New characteristic! (not already in our list)
                    infoToGet.add(new Runnable() {
                        @Override
                        public void run() {
                            // Request specific characteristic relating to newly discovered or updated data
                            updateSuccess(gatt.readCharacteristic(characteristic));
                        }
                    });
                    // Add characteristic ID to our list
                    mUUIDs.add(characteristic.getUuid());
                }

                // List of descriptors in characteristic
                List<BluetoothGattDescriptor> descriptorList = characteristic.getDescriptors();
                // Check each descriptor within each characteristic...
                for (final BluetoothGattDescriptor descriptor: descriptorList) {
                    if (mUUIDs.indexOf(descriptor.getUuid()) == -1) { // New descriptor! (not already in our list)
                        infoToGet.add(new Runnable() {
                            @Override
                            public void run() {
                                // Request specific descriptor relating to newly discovered or updated data
                                updateSuccess(gatt.readDescriptor(descriptor));
                            }
                        });
                        // Add descriptor ID to our list
                        mUUIDs.add(descriptor.getUuid());
                    }
                }
            }
            
            // Execute read requests before moving on to next service
            readNextBLE(gatt);
        }
    }

    // Returns response to characteristic read request
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        valueMap.put(characteristic.getUuid().toString(), characteristic.getValue());

        // Only read next bit of data after current request is completed
        readNextBLE(gatt);
    }

    // Returns response to descriptor read request
    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);
        valueMap.put(descriptor.getUuid().toString(), descriptor.getValue());

        // Only read next bit of data after current request is completed
        readNextBLE(gatt);
    }

    // Execute read requests stored in infoToGet, and log the result
    private void readNextBLE(BluetoothGatt gatt) {
        while (!infoToGet.isEmpty()) {
            infoToGet.poll().run();
            if (success) { break; }
        }
        if (infoToGet.isEmpty()) {
            new FirebaseUtils().pushUUIDInfo(deviceAddress, valueMap);
            bulkLog();
        }
    }

    // Log all of the new information
    private void bulkLog() {
        Log.d(TAG, "Logging BLE Data: ");
        for (String uuid: valueMap.keySet()) {
            Log.d(TAG, uuid);
            logValue(valueMap.get(uuid));
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

