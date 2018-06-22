package com.pideriver.a2017tatorscouttracingapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static android.support.v7.appcompat.R.styleable.View;
import static android.view.MotionEvent.ACTION_MOVE;

public class Match extends AppCompatActivity {

    private SharedPreferences preferences;

    private Context context;

    //private ArrayList<String> points;
    private String[] times = new String[160];
    private String[] xPoints = new String[160];
    private String[] yPoints = new String[160];
    private ArrayList<String> picks = new ArrayList<String>(60);

    private Button toComments;
    private Button restart;
    private Button viewData;
    private TextView recording;
    private RadioButton robot;
    private Button pick;
    private Button pause;
    private CheckBox dead;
    private CheckBox climb;
    private CheckBox intermittent;
    private AutoCompleteTextView comments;

    private int touchCalled = 6;
    private float maxX;
    private float maxY;
    private float minX;
    private float minY;
    private boolean firstTimeRun = true;
    private boolean firstTimeRunTwo = true;
    private int pointsRecorded = 0;
    private long time;
    private long startTime;
    private long lastTime;
    private NumberFormat formatter = NumberFormat.getInstance(Locale.US);
    private float playBackSpeed;
    private boolean isPaused = false;

    /**
     * sets up all variables and views
     *
     * remember to comment out and uncomment the correct min and max x and y depending which tablet the apk is (sections needing change will contain the word "tablets").
     * both the min and max in this method and the robot calibration in the printSamples method need to be changed depending on the tablet.
     * if they need to be recalibrated change printSamples to output pixel instead of feet and use trial and error on the tablet (DO NOT USE THE VIRTUAL MACHINE FOR CALIBRATING)
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        preferences = this.getSharedPreferences("preferences", MODE_PRIVATE);
        context = this;

        playBackSpeed = Float.parseFloat(preferences.getString("playbackSpeed","1"));

        //points = new ArrayList<String>(10);
        //points.add("Start of Points: Time>X>Y");

        recording = (TextView)findViewById(R.id.recordingText);

        robot = (RadioButton) findViewById(R.id.robot);

        toComments = (Button)findViewById(R.id.btnToComments);
        restart = (Button)findViewById(R.id.restartBtn);
        viewData = (Button)findViewById(R.id.viewDataBtn);
        pick = (Button)findViewById(R.id.pickBtn);
        pause = (Button)findViewById(R.id.btnPause);
        dead = (CheckBox)findViewById(R.id.ckBxDead);
        intermittent = (CheckBox)findViewById(R.id.ckBxIntermittent);
        climb = (CheckBox)findViewById(R.id.ckBxClimb);
        comments = (AutoCompleteTextView)findViewById(R.id.aCTVcomments) ;

        toComments.setOnClickListener(listener);
        restart.setOnClickListener(listener);
        viewData.setOnClickListener(listener);
        pick.setOnClickListener(listener);
        pause.setOnClickListener(listener);

        //large tablets
        //**
        //oldmaxX = 936.985f;
        //oldmaxY = 616.5472f;
        //oldminX = 84.23441f;
        //oldminY = 187.5558f;
        maxX = 938.1f;
        maxY = 565.7f;
        minX = 76.3f;
        minY = 127.2f;
        //*/

        //red tablets
        /**
        //oldmaxX = 1138.8f;
        maxX = 1158.748f;
        //oldmaxY = 684.5f;
        maxY = 658.0741f;
        //oldminX = 139.6f;
        minX = 119.2301f;
        //oldminY = 186.1f;
        minY = 134f;
        //*/
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);
        formatter.setRoundingMode(RoundingMode.HALF_UP);

        toComments.setVisibility(android.view.View.INVISIBLE);
        dead.setVisibility(android.view.View.INVISIBLE);
        intermittent.setVisibility(android.view.View.INVISIBLE);
        climb.setVisibility(android.view.View.INVISIBLE);
        comments.setVisibility(android.view.View.INVISIBLE);
    }

    /**
     * this is to help people from accidentally going back a screen and losing data
     */
    //https://stackoverflow.com/questions/2257963/how-to-show-a-dialog-to-confirm-that-the-user-wishes-to-exit-an-android-activity
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Screen")
                .setMessage("Are you sure you want to go back a screen?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    //https://stackoverflow.com/questions/21927467/how-to-get-all-points-in-motion-event?noredirect=1&lq=1

    /**
     * This method takes a motion event on the screen, records the
     * position on the screen after converting to feet,
     * and will move the robot box on the xml to those coordinates
     * will stop recording when comments appear
     *
     * Make sure to change the robot calibration depending on the tablet.
     * If it is a new tablet it has to be calibrated on the tablet by trial and error and NOT IN THE VIRTUAL MACHINE
     *
     * @param ev a Motion event that occured on the screen
     */
    void printSamples(MotionEvent ev) {
        //System.out.println("printSamples Called");
        if(!isPaused) {

            if (!firstTimeRun) {
                time = System.currentTimeMillis() - startTime;
            }
            if (firstTimeRun) {
                startTime = System.currentTimeMillis();
                //System.out.println("7::" + startTime);
                time = 0;
                firstTimeRunTwo = false;
            }

            //if(touchCalled%10 == 0) {
            recording.setText("Points recorded = " + pointsRecorded);
            final int historySize = ev.getHistorySize();
            final int pointerCount = ev.getPointerCount();
            for (int h = 0; h < historySize; h++) {

                //time = Float.toString(ev.getHistoricalEventTime(h));
                for (int p = 0; p < pointerCount; p++) {
                    //because the pick button will occasionally add unintended data points, a decision was made to ignore all data outide the boundary near the pick button
                    if (ev.getHistoricalY(p, h) < minY) {
                        return;
                    }
                    //System.out.println("8::" + (lastTime-time));
                    if ((firstTimeRun || time - lastTime >= (1000 / playBackSpeed)) && pointsRecorded < 160) {
                        times[pointsRecorded] = Long.toString(time);
                        //xPoints[pointsRecorded] = (Float.toString( ((ev.getHistoricalX(p, h) -minX) / (maxX - minX) ) * 54) );
                        xPoints[pointsRecorded] = (formatter.format(((ev.getHistoricalX(p, h) - minX) / (maxX - minX)) * 54));
                        //yPoints[pointsRecorded] = (formatter.format( ( (maxY - ev.getHistoricalY(p, h) )  / ( maxY - minY)) * 27) );
                        if (ev.getHistoricalY(p, h) > maxY) {
                            yPoints[pointsRecorded] = (0 + "");
                        } else if (ev.getHistoricalY(p, h) < minY) {
                            yPoints[pointsRecorded] = (27 + "");
                        } else {
                            //yPoints[pointsRecorded] = (Float.toString( ( (maxY - ev.getHistoricalY(p, h) )  / ( maxY - minY)) * 27) );
                            yPoints[pointsRecorded] = (formatter.format(((maxY - ev.getHistoricalY(p, h)) / (maxY - minY)) * 27));
                        }
                        pointsRecorded++;
                        //System.out.println("X = " + ev.getHistoricalX(p, h));
                        //System.out.println("Y = " + ( ( (maxY - ev.getHistoricalY(p, h) )  / ( maxY - minY)) * 27));
                        lastTime = time;
                    }
                    //robot.setX(ev.getHistoricalX(p, h) -50);
                    //robot.setY(ev.getHistoricalY(p, h) - 120);
                    //System.out.println("Robot y = " + robot.getY());
                }

            }

            recording.setText("Points recorded = " + pointsRecorded);
            //time = Float.toString(ev.getEventTime());
            for (int p = 0; p < pointerCount; p++) {
                //because the pick button will occasionally add unintended data points, a decision was made to ignore all data outide the boundary near the pick button
                if (ev.getY(p) < minY) {
                    return;
                }
                //System.out.println("9::" + (time-lastTime));
                //System.out.println("9::" + (time-lastTime >=1000));
                if ((firstTimeRun || time - lastTime >= (1000 / playBackSpeed)) && pointsRecorded < 160) {
                    times[pointsRecorded] = Long.toString(time);
                    //xPoints[pointsRecorded] = (Float.toString( ((ev.getX(p) - minX) / (maxX - minX)) * 54) );
                    xPoints[pointsRecorded] = (formatter.format(((ev.getX(p) - minX) / (maxX - minX)) * 54));
                    //yPoints[pointsRecorded] = (formatter.format( ( (maxY - ev.getY(p)) / ( maxY - minY) ) * 27) );
                    if (ev.getY(p) > maxY) {
                        yPoints[pointsRecorded] = (0 + "");
                    } else if (ev.getY(p) < minY) {
                        yPoints[pointsRecorded] = (27 + "");
                    } else {
                        //yPoints[pointsRecorded] = (Float.toString( ( (maxY - ev.getY(p)) / ( maxY - minY) ) * 27) );
                        yPoints[pointsRecorded] = (formatter.format(((maxY - ev.getY(p)) / (maxY - minY)) * 27));
                    }
                    pointsRecorded++;
                    //System.out.println("X = " + ev.getX(p));
                    //System.out.println("Y = " + ( ( (maxY - ev.getY(p)) / ( maxY - minY) ) * 27));
                    lastTime = time;
                }
                if (touchCalled % 10 == 0) {
                    //red tablets
                    /**
                     robot.setX(ev.getX(p) - 18);//-18
                     robot.setY(ev.getY(p) - 50);//-50
                     //*/

                    //Large Tablets
                    //**
                    robot.setX(ev.getX(p) - 18);//-18
                    robot.setY(ev.getY(p) - 35);//-50
                    //*/
                    //System.out.println("Robot Y = " + robot.getY());
                }

            }

            //}

            if (touchCalled >= 40) {
                touchCalled = -1;
            }
            touchCalled++;
            if (!firstTimeRunTwo) {
                firstTimeRun = false;
            }
        }
    }

    /**
     * This method checks if an object has been clicked
     *
     * restartBtn: will prompt the user if they want to clear data. If yes it resets all data variables and arrays
     * viewDataBtn: prompts the user if the user wants to go to a screen to view what they drew but that screen is not currently implemented
     * pickBtn: add a pick from the last known robot position and time
     * btnToComments: changes the activity to MatchSetup.java and activity_match_setup.xml after it writes all data to a file.
     *
     * @param View v view that has been touched/selected/clicked
     * @exception IOException thrown if something happens with the file writer from btnToComments
     */
    private android.view.View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //System.out.println("Hello ReachedOCL");
            switch (v.getId()) {

                case R.id.restartBtn:
                    new AlertDialog.Builder(context)
                            .setTitle("Restart")
                            .setMessage("Are you sure you want to reset the data?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    firstTimeRun = true;
                                    firstTimeRunTwo = true;
                                    pointsRecorded = 0;
                                    time = 0;
                                    startTime = 0;
                                    lastTime = 0;
                                    picks = new ArrayList<String>(60);
                                    //points.add("Start of Points: Time>X>Y");
                                    recording.setText("Points recorded = 0");
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    break;
                case R.id.viewDataBtn:
                    new AlertDialog.Builder(context)
                            .setTitle("View data")
                            .setMessage("Do you want to review the data? Except it doesn't matter because this is a placeholder atm")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ////
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    break;
                case R.id.pickBtn:
                    picks.add(Long.toString(time));
                    picks.add(xPoints[pointsRecorded-1]);
                    picks.add(yPoints[pointsRecorded-1]);
                    break;
                case R.id.btnPause:
                    new AlertDialog.Builder(context)
                            .setTitle("To Comments")
                            .setMessage("Do you want to go to comments? \n WARNING: You can not go back after you click yes!")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    isPaused = true;
                                    restart.setVisibility(android.view.View.INVISIBLE);
                                    robot.setVisibility(android.view.View.INVISIBLE);
                                    recording.setVisibility(android.view.View.INVISIBLE);
                                    pause.setVisibility(android.view.View.INVISIBLE);

                                    toComments.setVisibility(android.view.View.VISIBLE);
                                    comments.setVisibility(android.view.View.VISIBLE);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    break;
                case R.id.btnToComments:

                    if(preferences.getBoolean("didAppend",false)){
                        //System.out.println("had appended");
                        Intent intent  = new Intent(context,MatchSetup.class);
                        startActivity(intent);
                        break;
                    }
                    //System.out.println("appending");

                    SharedPreferences.Editor editorThree = preferences.edit();
                    String oldFileName = "";

                    if(preferences.getInt("match end",1) >1 && !preferences.getBoolean("nuke old file",false)) {
                        oldFileName = preferences.getString("filename", "ERROR");
                    }
                    editorThree.putString("filename", preferences.getString("scoutName","SCOUT") + "TracingApp-"+preferences.getString("group", "GROUP") + " Matches " + preferences.getInt("match start", 0) + " - " + preferences.getInt("match end", 0) + ".csv");
                    editorThree.commit();


                    File file = new File(Environment.getExternalStorageDirectory() + "/" + preferences.getString("filename", "BROKEN"));

                    if(preferences.getInt("match end",0) >1 && !preferences.getBoolean("nuke old file",false)) {
                        try {
                            File oldfile = new File(Environment.getExternalStorageDirectory() + "/" + oldFileName);
                            oldfile.renameTo(file);
                            file = new File(Environment.getExternalStorageDirectory() + "/" + preferences.getString("filename", "BROKEN"));
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("CREATION", "no old file");
                        }
                    }
                    try{
                        FileWriter writer;
                        writer = new FileWriter(file,true);
                        writer.append(preferences.getString("team","TEAM"));
                        writer.append(",");
                        writer.append(preferences.getInt("match",0)+"");
                        writer.append(",");
                        writer.append(preferences.getString("scoutName","SCOUT"));
                        writer.append(",");
                        writer.append(preferences.getString("group","GROUP"));
                        writer.append(",");
                        //writer.append(preferences.getString("startPos","STARTING POSITION"));
                        //writer.append(",");
                        writer.append(preferences.getString("allianceColor","COLOR"));
                        writer.append(",");
                        writer.append("Start of Points: Time > X > Y");
                        writer.append(",");
                        for(int x = 0;x<pointsRecorded;x++){
                            if(!xPoints[x].equals("") && !yPoints[x].equals("") && !times[x].equals("")){
                                writer.append(times[x] + ",");
                                writer.append(xPoints[x] + ",");
                                writer.append(yPoints[x] + ",");
                            }
                        }
                        if(pointsRecorded<160){
                            for(int c = 1; c<=160-pointsRecorded;c++){
                                writer.append(times[pointsRecorded-1] + ",");
                                writer.append(xPoints[pointsRecorded-1] + ",");
                                writer.append(yPoints[pointsRecorded-1] + ",");
                            }
                        }
                        if(dead.isChecked()){
                            writer.append("1");
                        }
                        else {
                            writer.append("0");
                        }
                        writer.append(",");
                        if(intermittent.isChecked()){
                            writer.append("1");
                        }
                        else {
                            writer.append("0");
                        }
                        writer.append(",");
                        if(climb.isChecked()){
                            writer.append("1");
                        }
                        else {
                            writer.append("0");
                        }
                        writer.append(",");
                        writer.append(comments.getText().toString().replaceAll("\n", "").replaceAll(",", ""));
                        writer.append(",");
                        writer.append("Picks: Time > X > Y,");
                        for(String s: picks){
                            writer.append(s);
                            writer.append(",");
                        }
                        //Array ary = preferences.getStringSet("pointSet",new HashSet<String>());
                        writer.append("\n");
                        writer.flush();
                        writer.close();
                    }
                    catch(IOException e){
                        e.printStackTrace();
                        String stack = "";
                        for(StackTraceElement x:e.getStackTrace()){
                            stack = stack + x.toString() + "\n";
                        }
                        Log.d("CREATION", stack);
                        Toast.makeText(context, "File Writer Failed", Toast.LENGTH_SHORT).show();
                    }
                    SharedPreferences.Editor editorTwo = preferences.edit();
                    editorTwo.putBoolean("nuke old file",false);
                    editorTwo.putInt("match end",preferences.getInt("match end",1)+1);
                    editorTwo.putInt("match", preferences.getInt("match", 0) + 1);
                    editorTwo.putBoolean("didAppend",true);
                    editorTwo.commit();
                    Intent intent  = new Intent(context,MatchSetup.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    /**
     * takes a motion event that occured and passes it to the printSamples method
     * @param event Motion event
     * @return  super.onTouchEvent(event)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //System.out.println("MotionEvent Occured");
        printSamples(event);
        return super.onTouchEvent(event);
    }



}
