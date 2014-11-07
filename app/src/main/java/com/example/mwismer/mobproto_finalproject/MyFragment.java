package com.example.mwismer.mobproto_finalproject;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by mwismer on 10/23/14.
 */
public class MyFragment extends Fragment {
    private String TAG = "MyFragment";

    public MyFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my, container, false);

        new BLEScanner(getActivity()).scanBLE();

        return rootView;
    }
}