package com.pideriver.a2017tatorscouttracingapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

public class MatchSetup extends AppCompatActivity {

    //Declaring Views
    private RadioButton radStartPos1, radStartPos2, radStartPos3, radStartPos4, radRed, radBlue;

    private RadioGroup rgpRedOrBlue;

    private Button btnToAuto;

    private Button btnDataSent;

    private Spinner spnTeamSpinner;

    private ImageView imgFieldSetup;

    private SharedPreferences preferences;

    private String[] spinnerAry;

    private Context context;

    private Intent itToMatch;

    private DLocationManager start, redOrBlue;

    private EditText matchNum;

    private EditText scoutName;

    private EditText playBackSpeed;

    private String groupStr;

    private int group;

    private Button setTeam;

    private EditText teamName;

    /**
     *sets all variables and views
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_setup);
        verifyStoragePermissions(this);
        preferences = this.getSharedPreferences("preferences", MODE_PRIVATE);
        context = this;
        //Initializing Views
        radStartPos1 = (RadioButton) findViewById(R.id.radStartPos1);
        radStartPos2 = (RadioButton) findViewById(R.id.radStartPos2);
        radStartPos3 = (RadioButton) findViewById(R.id.radStartPos3);
        radStartPos4 = (RadioButton) findViewById(R.id.radStartPos4);
        radRed = (RadioButton) findViewById(R.id.radRed);
        radBlue = (RadioButton) findViewById(R.id.radBlue);
        btnToAuto = (Button) findViewById(R.id.btnToAuto);
        btnDataSent = (Button) findViewById(R.id.btnDataSent);
        spnTeamSpinner = (Spinner) findViewById(R.id.spnTeamSpinner);
        imgFieldSetup = (ImageView) findViewById(R.id.imgFieldSetup);
        matchNum = (EditText)findViewById(R.id.matchNum);
        scoutName = (EditText)findViewById(R.id.ETscoutName);
        playBackSpeed = (EditText) findViewById(R.id.speedNum);
        //Setting Up Spinner
        spinnerAry = new String[7];
        //DummyData

        preferences = this.getSharedPreferences("preferences", MODE_PRIVATE);
        context = this;
        matchNum.setText(preferences.getInt("match", 1)+"");
        scoutName.setText(preferences.getString("scoutName",""));
        //Initializing Views
        radStartPos1 = (RadioButton) findViewById(R.id.radStartPos1);
        radStartPos2 = (RadioButton) findViewById(R.id.radStartPos2);
        radStartPos3 = (RadioButton) findViewById(R.id.radStartPos3);
        radStartPos4 = (RadioButton) findViewById(R.id.radStartPos4);
        radRed = (RadioButton) findViewById(R.id.radRed);
        radBlue = (RadioButton) findViewById(R.id.radBlue);
        btnToAuto = (Button) findViewById(R.id.btnToAuto);
        spnTeamSpinner = (Spinner) findViewById(R.id.spnTeamSpinner);
        rgpRedOrBlue = (RadioGroup) findViewById(R.id.rgpRedOrBlue);
        imgFieldSetup = (ImageView) findViewById(R.id.imgFieldSetup);
        setTeam = (Button)findViewById(R.id.setTeam);
        teamName = (EditText)findViewById(R.id.teamName);

        //Setting Up Spinner
        spinnerAry = new String[7];
        //DummyData
        spinnerAry[0] = "Pick A Team";
        spinnerAry[1] = "254";
        spinnerAry[2] = "976";
        spinnerAry[3] = "987";
        spinnerAry[4] = "1114";
        spinnerAry[5] = "2056";
        spinnerAry[6] = "2122";
        //getting group num
        groupStr = preferences.getString("group", "GROUP");
        switch(groupStr){
            case "Group 1":group =1;
                break;
            case "Group 2":group =2;
                break;
            case "Group 3":group =3;
                break;
            case "Group 4":group =4;
                break;
            case "Group 5":group =5;
                break;
            case "Group 6":group =6;
                break;
            default:group = 0;//Should only happen if sign in catch breaks
        }
        //ActualData
        //System.out.println("Hello FakeData");
        //readFile();

        ArrayAdapter<String> teamSpnAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerAry);
        teamSpnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTeamSpinner.setAdapter(teamSpnAdapter);

        //Setting OnClickListeners
        btnToAuto.setOnClickListener(listener);
        btnDataSent.setOnClickListener(listener);
        radRed.setOnClickListener(listener);
        radBlue.setOnClickListener(listener);
        setTeam.setOnClickListener(listener);
        //rgpRedOrBlue.setOnCheckedChangeListener(checkListener);

        setupRadioButtons(context);
    }

    /**
     *used daryls location manager for a radiogroup
     *
     * @param context context of the activity
     */
    private void setupRadioButtons(Context context) {
        start = new DLocationManager(context);
        start.add(radStartPos1, "Inside Key");
        start.add(radStartPos2, "Next to Key");
        start.add(radStartPos3, "Middle of Airship");
        start.add(radStartPos4, "Next to Loading Zone");


        //DLocationManager redOrBlue
        redOrBlue = new DLocationManager(context);
        redOrBlue.add(radRed, "Red");
        redOrBlue.add(radBlue, "Blue");
    }

    /**
     * This method checks if an object has been clicked
     *
     * btnToAuto: calls checkEverything and if it returns true then calls saveData and switches activities to Match.java and activity_match.xml
     * btnDataSent: prompts user if they want to split the file
     * setTeam:(Button) calls getTeam and uses the file writer, match number and group number to determine what team will be selected
     *
     * @param View v view that has been touched/selected/clicked
     */
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //System.out.println("Hello ReachedOCL");
            switch (v.getId()) {

                case R.id.btnToAuto:
                    if (checkEverything()) {
                        if(preferences.getInt("matchesWithoutSplit",0) >=5){
                            new AlertDialog.Builder(context)
                                    .setTitle("Has Data Been Sent?")
                                    .setMessage("If it has and you have pressed the \"Sent Data To Stuart Button\" press Continue.\n If not sent press Continue \n Otherwise press no and go do that")
                                    .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            SharedPreferences.Editor editor2 = preferences.edit();
                                            editor2.putInt("matchesWithoutSplit", preferences.getInt("matchesWithoutSplit",0)+1);
                                            editor2.commit();
                                            saveData();
                                            itToMatch = new Intent(context, Match.class);
                                            startActivity(itToMatch);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                        else {
                            SharedPreferences.Editor editor2 = preferences.edit();
                            editor2.putInt("matchesWithoutSplit", preferences.getInt("matchesWithoutSplit",0)+1);
                            editor2.commit();
                            saveData();
                            itToMatch = new Intent(context, Match.class);
                            startActivity(itToMatch);
                        }

                    }
                    break;
                case R.id.btnDataSent:
                    new AlertDialog.Builder(context)
                            .setTitle("Data Sent")
                            .setMessage("Are you sure Stuwort has received the data?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putInt("match start",preferences.getInt("match end",0));
                                    editor.putInt("match end",preferences.getInt("match end",0)+1);
                                    editor.putBoolean("nuke old file",true);
                                    editor.putInt("matchesWithoutSplit", 0);
                                    editor.commit();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    break;
                //for new way of reading team csv
                case R.id.setTeam:
                    teamName.setText(getTeam(Integer.parseInt(matchNum.getText().toString()), group));
                    break;
            }
        }
    };

    /**
     * will return true if everything os selected and ok, false otherwise
     *
     * @return boolean will return true if everything os selected and ok, false otherwise
     */
    private boolean checkEverything() {
        /**if (start.getCheckedButtonName().equals("none")) {
            Toast t = Toast.makeText(context, "Please select a starting position", Toast.LENGTH_SHORT);
            t.show();
            return false;
        }*/
        /**if (redOrBlue.getCheckedButtonName().equals("none")) {
            Toast t = Toast.makeText(context, "Please select red or blue", Toast.LENGTH_SHORT);
            t.show();
            return false;
        }*/
        if (teamName.getText().toString().equals("")||teamName.getText().toString().equals("File not found. Please enter number manually")) {
            Toast t = Toast.makeText(context, "Please select a Team", Toast.LENGTH_SHORT);
            t.show();
            return false;
        }
        if(playBackSpeed.getText().toString().equals("") && (!playBackSpeed.getText().toString().equals("1") || !playBackSpeed.getText().toString().equals("1.5") || !playBackSpeed.getText().toString().equals("2"))){
            Toast t = Toast.makeText(context, "Please input a playback speed", Toast.LENGTH_SHORT);
            t.show();
            return false;
        }
        return true;
    }

    /**
     * saves data to shared preferences
     */
    private void saveData() {
        SharedPreferences.Editor editor = preferences.edit();
        //editor.putString("startPos", start.getCheckedButtonName());
        editor.putString("allianceColor", redOrBlue.getCheckedButtonName());
        editor.putInt("match", Integer.parseInt(matchNum.getText().toString()));
        editor.putInt("match end", Integer.parseInt(matchNum.getText().toString()));
        editor.putString("scoutName", scoutName.getText().toString().replaceAll("[-+.^:,]","").replace("\n", "").replace("\r", ""));
        editor.putString("playbackSpeed",playBackSpeed.getText().toString());///
        //editor.putString("team", spnTeamSpinner.getSelectedItem().toString().replaceAll(",.*", ""));
        editor.putString("team", teamName.getText().toString());
        editor.putBoolean("didAppend",false);
        editor.commit();
    }

    /**
     * used for requesting permission to write to storage
     */
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * requests permission if needed for writing to storage
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        //Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //System.out.println("Hello ReachedVSP");
        if (permission != PackageManager.PERMISSION_GRANTED){
            //System.out.println("Hello No Permission");
            //We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    //Additions for new way to read team csv

    /**
     * reads a schedule.csv file and then creates an array of teams
     *
     * @return teams array list of teams
     */
    private ArrayList<String> readFile() {
        try {
            FileReader fr = new FileReader(Environment.getExternalStorageDirectory() + "/schedule.csv");
            Scanner scanner = new Scanner(fr);
            scanner.useDelimiter("\n");
            ArrayList<String> matches = new ArrayList<String>();
            ArrayList<String> teams = new ArrayList<String>();
            while (scanner.hasNext()){
                String team = scanner.next().trim();
                matches.add(team);
            }
            scanner.close();
            for (String string:matches) {
                String[] sAry = string.split(",");
                for (String sstring:sAry) {
                    teams.add(sstring.trim());
                }
            }
            return teams;
        }
        catch (FileNotFoundException e){
            Toast t = Toast.makeText(this,
                    "File not found!", Toast.LENGTH_LONG);
            t.show();
            return new ArrayList<String>();
        }
    }

    /**
     * selects team based on match number and group number. calls readFile
     *
     * @param matchNumber current match number
     * @param group current group number
     * @return String of team selected
     */
    private String getTeam(int matchNumber, int group) {
        ArrayList<String> teams = readFile();
        if(!teams.isEmpty()) {
            return teams.get((matchNumber-1)*6+group-1);
        }
        else{
            return "File not found. Please enter number manually";
        }
    }


}
