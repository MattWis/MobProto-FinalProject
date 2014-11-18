package com.example.mwismer.mobproto_finalproject;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;

/**
 * Created by mwismer on 10/23/14.
 */
public class MyFragment extends Fragment {
    private String TAG = "MyFragment";
    BLEScanner scanner;

    public MyFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my, container, false);

        (new FirebaseUtils()).getNodeList(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, HashMap<String, Double>> nodes = (HashMap<String, HashMap<String, Double>>) dataSnapshot.getValue();

                for (String deviceName: nodes.keySet()) {
                    Log.d(TAG, deviceName);
                    Log.d(TAG, "" + nodes.get(deviceName).get("lat"));
                    Log.d(TAG, "" + nodes.get(deviceName).get("lon"));
                }

                runScanner();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        return rootView;
    }

    public void runScanner() {
        scanner = new BLEScanner(this);
        scanner.scanBLE("78:A5:04:8C:25:DF");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        scanner.onActivityResult(requestCode, resultCode, data);
    }
}