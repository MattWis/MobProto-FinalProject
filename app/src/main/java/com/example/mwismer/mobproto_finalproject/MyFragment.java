package com.example.mwismer.mobproto_finalproject;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        scanner = new BLEScanner(this);
        scanner.scanBLE("78:A5:04:8C:25:DF");

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        scanner.onActivityResult(requestCode, resultCode, data);
    }
}