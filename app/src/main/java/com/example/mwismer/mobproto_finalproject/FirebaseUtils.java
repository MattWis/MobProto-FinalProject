package com.example.mwismer.mobproto_finalproject;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mwismer on 11/6/14.
 */
public class FirebaseUtils {
    private String TAG = "FirebaseUtils";
    private String url = "https://mobproto-final.firebaseio.com/";

    public void pushUUIDInfo(String deviceAddress, Map<String, byte[]> valueMap) {
        String timeStamp = new SimpleDateFormat("yyyy:MM:dd-HH:mm:ss").format(new Date());
        Firebase currentDevice = new Firebase(url).child("devices").child(deviceAddress).child(timeStamp);
        currentDevice.setValue(valueMap);
    }

    public void getWhiteList (final BLEScanner scanner) {
        Firebase whiteList = new Firebase(url).child("whiteList");
        whiteList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, dataSnapshot.getValue().toString());
                Object data = dataSnapshot.getValue();
                if (data.getClass().toString().equals("class java.util.HashMap")) {
                    scanner.whiteList = (HashMap<String, Object>) data;
                } else {
                    Log.d(TAG, data.getClass().toString());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}
