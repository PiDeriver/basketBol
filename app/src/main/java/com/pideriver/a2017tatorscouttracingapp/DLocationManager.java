package com.pideriver.a2017tatorscouttracingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RadioButton;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Daryl on 6/25/2015.
 */
public class DLocationManager {
    private ArrayList<RadioButton> locations;
    private ArrayList<String> places;
    private View.OnClickListener listen, toCall;
    private View toPass;
    private String checkedButtonName = "";

    public DLocationManager(final Context context) {
        locations = new ArrayList<>();
        places = new ArrayList<>();
        listen = new View.OnClickListener() {
            public void onClick(View v) {
                for (int i = 0; i < locations.size(); i++) {
                    if (locations.get(i) != v) {
                        locations.get(i).setChecked(false);
                    } else {
                        checkedButtonName = (places.get(i));
                    }
                    final Context mContext = context;
                    SharedPreferences preferences;
                    preferences = mContext.getSharedPreferences("preferences", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("startLoc", places.get(i));
                    editor.apply();
                }
            }
        };
        if (toCall != null) {
            toCall.onClick(toPass);
        }
    }


    public void add(RadioButton newLoc, String place) {
        locations.add(newLoc);
        places.add(place);
        newLoc.setOnClickListener(listen);
    }

    public void setOnClickListener(View.OnClickListener listen) {
        toCall = listen;
    }

    public void setOnClickListener(View.OnClickListener listen, View Pass) {
        toCall = listen;
        toPass = Pass;
    }

    public String getCheckedButtonName() {
        if (checkedButtonName.matches("")) {
            return "none";
        } else {
            return checkedButtonName;
        }

    }
}