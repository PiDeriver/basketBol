package com.pideriver.a2017tatorscouttracingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class CalibrationScreen extends AppCompatActivity {

    Context context = this;
    Button toMatches;
    SharedPreferences preferences;
    private int touchCalled = 6;
    private ArrayList<Float> pointX;
    private ArrayList<Float> pointY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration_screen);

        preferences = this.getSharedPreferences("preferences", Context.MODE_PRIVATE);

        pointX = new ArrayList<Float>(10);
        pointY = new ArrayList<Float>(10);

        toMatches = (Button)findViewById(R.id.btnToMatches);
        toMatches.setOnClickListener(listen);
    }

    private View.OnClickListener listen = new View.OnClickListener(){
        public void onClick(View v){
            switch(v.getId()) {
                case R.id.btnToMatches:
                    if(!pointX.isEmpty() && !pointY.isEmpty()){
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putFloat("maxX", Collections.max(pointX));
                        editor.putFloat("maxY", Collections.max(pointY));
                        editor.putFloat("minX", Collections.min(pointX));
                        editor.putFloat("minY", Collections.min(pointY));
                        editor.commit();
                        Intent intent  = new Intent(context,MatchSetup.class);
                        startActivity(intent);
                    }
                    break;
            }
        }
    };


    void printSamples(MotionEvent ev) {

        //if(touchCalled >= 0) {
            final int historySize = ev.getHistorySize();
            final int pointerCount = ev.getPointerCount();
            for (int h = 0; h < historySize; h++) {

                for (int p = 0; p < pointerCount; p++) {
                        pointX.add(ev.getHistoricalX(p, h));
                        pointY.add(ev.getHistoricalY(p, h));
                }
            }

            for (int p = 0; p < pointerCount; p++) {
                pointX.add(ev.getX(p));
                pointY.add(ev.getY(p));
            }
        /**}

        if (touchCalled >=40){
            touchCalled = -1;
        }
        touchCalled++;*/
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //System.out.println("MotionEvent Occured");
        printSamples(event);
        return super.onTouchEvent(event);
    }

}
