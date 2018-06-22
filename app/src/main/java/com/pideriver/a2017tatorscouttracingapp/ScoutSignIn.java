package com.pideriver.a2017tatorscouttracingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class ScoutSignIn extends AppCompatActivity {

    Context context = this;

    Button toSetup;

    AutoCompleteTextView scoutName;

    AutoCompleteTextView matchStart;

    Spinner groupSpinner;

    Toast toast;

    String[] spinnerAry;

    SharedPreferences preferences;

    /**
     * sets up all variables and views
     *
     * also clears all shared preferences
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scout_sign_in);
        preferences = this.getSharedPreferences("preferences",Context.MODE_PRIVATE);
        //kills all shared preferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();

        //seting up widgets
        toSetup = (Button)findViewById(R.id.btnToSetup);
        scoutName = (AutoCompleteTextView)findViewById(R.id.txtScoutName);
        matchStart = (AutoCompleteTextView)findViewById(R.id.txtMatchStart);
        groupSpinner = (Spinner)findViewById(R.id.spnGroupSpinner);

        //setting up spinner
        spinnerAry = new String[7];
        spinnerAry[0] = "Select Group";
        spinnerAry[1] = "Group 1";
        spinnerAry[2] = "Group 2";
        spinnerAry[3] = "Group 3";
        spinnerAry[4] = "Group 4";
        spinnerAry[5] = "Group 5";
        spinnerAry[6] = "Group 6";

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerAry);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(spinnerArrayAdapter);

        //Setting on click listeners
        toSetup.setOnClickListener(listen);
    }

    /**
     *  This method checks if an object has been clicked
     *
     *  btnToSetup:saves relevant variables and swiches activity ro
     *
     *  @param View v view that has been touched/selected/clicked
     */
    private View.OnClickListener listen = new View.OnClickListener(){
        public void onClick(View v){
            switch(v.getId()) {
                case R.id.btnToSetup:
                    if(checkEverything()){
                        //Log.d("CREATION",scoutName.getText().toString().length() + "");
                        //Log.d("CREATION",scoutName.getText().toString());
                        break;
                    }
                    else{
                        //Log.d("CREATION",scoutName.getText().toString().length() + "");
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("scoutName", scoutName.getText().toString().replaceAll("[-+.^:,]","").replace("\n", "").replace("\r", ""));
                        editor.putString("group",groupSpinner.getSelectedItem().toString());
                        editor.putInt("match",Integer.parseInt(matchStart.getText().toString()));
                        editor.putInt("match start",Integer.parseInt(matchStart.getText().toString()));
                        editor.putInt("match end",Integer.parseInt(matchStart.getText().toString()));
                        editor.putBoolean("nuke old file",false);
                        editor.commit();
                        Intent intent  = new Intent(context,MatchSetup.class);
                        startActivity(intent);
                    }
                    break;
            }
        }
    };
    //method to check if the user has put in a name and has selected a group

    /**
     * will check if everything that needed to be filled out was before activity is switched
     * @return fail will return true or false depending on checks below
     */
    private boolean checkEverything(){
        boolean fail = false;
        if(scoutName.getText().toString().length() ==0){
            toast = Toast.makeText(context, "Please enter your name to continue!", Toast.LENGTH_SHORT);
            toast.show();
            fail = true;
        }
        try {
            if(Integer.parseInt(matchStart.getText().toString())<1){
                toast = Toast.makeText(context, "The match number can't be less than 1!", Toast.LENGTH_SHORT);
                toast.show();
                fail = true;
            }
        }
        catch (Exception e){
            toast = Toast.makeText(context, "Please enter a valid match number!", Toast.LENGTH_SHORT);
            toast.show();
            fail = true;
        }
        if(matchStart.getText().toString() == ""){
            toast = Toast.makeText(context, "Please enter a valid match number!", Toast.LENGTH_SHORT);
            toast.show();
            fail = true;
        }
        if(groupSpinner.getSelectedItemPosition() == 0){
            toast = Toast.makeText(context, "Please select a group on the spinner!", Toast.LENGTH_SHORT);
            toast.show();
            fail = true;
        }
        return fail;
    }
}
