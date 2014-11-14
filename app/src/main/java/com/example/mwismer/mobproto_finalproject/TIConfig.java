package com.example.mwismer.mobproto_finalproject;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by mwismer on 11/13/14.
 */
public class TIConfig {
    private HashMap<UUID, Byte> configs = new HashMap<UUID, Byte>(8);
    private ArrayBlockingQueue<Runnable> confSet = new ArrayBlockingQueue<Runnable>(128);

    public TIConfig() {
        configs.put(UUID.fromString("f000aa02-0451-4000-b000-000000000000"), (byte) 1); //IRT
        configs.put(UUID.fromString("f000aa12-0451-4000-b000-000000000000"), (byte) 3); //ACC: weird enable
        configs.put(UUID.fromString("f000aa22-0451-4000-b000-000000000000"), (byte) 1); //HUM
        configs.put(UUID.fromString("f000aa32-0451-4000-b000-000000000000"), (byte) 1); //MAG
        configs.put(UUID.fromString("f000aa42-0451-4000-b000-000000000000"), (byte) 1); //BAR
        configs.put(UUID.fromString("f000aa52-0451-4000-b000-000000000000"), (byte) 7); //GYRO: weird enable
    }

    //This is a horrible function (and poorly named) and I apologize for it... (mwismer, Nov 13, 2014)
    public ArrayBlockingQueue<Runnable> getServiceWriters (
            final BLEFinderCallback callback,
            final BluetoothGatt gatt,
            List<BluetoothGattCharacteristic> characteristicList,
            List<BluetoothGattDescriptor> descriptorList,
            ArrayBlockingQueue<Runnable> infoToGet) throws InterruptedException {

        for (final UUID confUUID: configs.keySet()) {
            for (final BluetoothGattCharacteristic characteristic: characteristicList) {
                if (characteristic.getUuid().equals(confUUID)) {
                    confSet.put(new Runnable() {
                        @Override
                        public void run() {
                            final byte[] array = { configs.get(confUUID) };
                            characteristic.setValue(array);
                            callback.updateSuccess(gatt.writeCharacteristic(characteristic));
                        }
                    });
                }
            }
            for (final BluetoothGattDescriptor descriptor: descriptorList) {
                if (descriptor.getUuid().equals(confUUID)) {
                    confSet.put(new Runnable() {
                        @Override
                        public void run() {
                            byte[] array = { configs.get(confUUID) };
                            descriptor.setValue(array);
                            gatt.writeDescriptor(descriptor);
                        }
                    });
                }
            }
        }

        for (Runnable runner: infoToGet) {
            confSet.put(runner);
        }

        return confSet;
    }
}
