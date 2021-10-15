package com.example.tuesdayfirstapp;

import static java.lang.Math.abs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static SQLiteDatabase db;
    static String lastNameValue;
    private Float respiratoryData = null, heartRateData = null;
    private Uri fileUri;
    private static final int VIDEO_CAPTURE = 101;
    private int scaler = 6;
    private boolean heartRateProcIsRunning = false;
    private boolean RespiratoryProcIsRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //button for recording video for measuring heart rate
        Button btn6 = (Button) findViewById(R.id.button6);

        if(!hasCamera()){
            btn6.setEnabled(false);
            Toast.makeText(MainActivity.this, "Camera not supported. Record button Disabled",
                    Toast.LENGTH_SHORT).show();
        }
        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(heartRateProcIsRunning == true) {
                    Toast.makeText(MainActivity.this, "Please wait for process to complete before recording a new video!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    startRecording();
                }
            }
        });

        //button for measuring heart rate
        Button btn1 = (Button) findViewById(R.id.button);
        TextView textView = (TextView) findViewById(R.id.textView3);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File mediaFile = new File(getExternalFilesDir(null), "myvideo.mp4");
                fileUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getApplicationContext().getPackageName() + ".provider", mediaFile);

                if(heartRateProcIsRunning == true) {
                    Toast.makeText(MainActivity.this, "Heart Rate Calculation Progress already in progress!",
                            Toast.LENGTH_SHORT).show();
                } else if (mediaFile.exists()) {
                    heartRateProcIsRunning = true;
                    textView.setText("Measuring");

                    System.gc();
                    Intent hInt = new Intent(MainActivity.this, HeartRateCalcService.class);
                    startService(hInt);

                } else {
                    Toast.makeText(MainActivity.this, "Please record a video first!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //button for measuring respiratory rate
        Button btn2 = (Button) findViewById(R.id.button2);
        TextView textView2 = (TextView) findViewById(R.id.textView2);

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(getApplicationContext(), "pressed respiratory", Toast.LENGTH_SHORT).show();
                // Checks if there is an existing respiratory rate detection process running
                if(RespiratoryProcIsRunning == true) {
                    Toast.makeText(MainActivity.this, "Respiratory Rate Calculation Progress already in progress!",
                            Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MainActivity.this, "Please lay down and place the phone on your chest \nfor 45s", Toast.LENGTH_LONG).show();
                    RespiratoryProcIsRunning = true;
                    textView2.setText("Measuring");

                    Intent respIntent = new Intent(MainActivity.this, AccelerometerService.class);
                    startService(respIntent);
                }
            }
        });

        //button for new activity page for symptoms
        Button btn3 = (Button) findViewById(R.id.button3);

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Rate your symptoms according to their severity", Toast.LENGTH_SHORT).show();
                Intent mA2Int = new Intent(view.getContext(), MainActivity2.class);
                startActivity(mA2Int);
            }
        });

        //Edit Text view for Lastname of the person (required for the database table name)
        EditText lastName = (EditText) findViewById(R.id.editTextTextPersonName2);

        //button for uploading results in to the db
        Button btn4 = (Button) findViewById(R.id.button4);

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastNameValue = lastName.getText().toString();
                Toast.makeText(getApplicationContext(), "pressed upload", Toast.LENGTH_SHORT).show();
                try{
                    File f = new File(getExternalFilesDir(null), "yash.db");

                    Log.d("Log", "Path of the db file: "+f.getPath());

                    db = SQLiteDatabase.openOrCreateDatabase(f.getPath(),null);

                    db.beginTransaction();
                    try {

                        db.execSQL("create table "+ lastNameValue +" ("
                                + " recID integer PRIMARY KEY autoincrement, "
                                + " heart_rate real, "
                                + " respiratory_rate real, "
                                + " Nausea real, "
                                + " Headache real, "
                                + " Diarrhea real, "
                                + " Soar_throat real, "
                                + " Fever real, "
                                + " Muscle_Ache real, "
                                + " Loss_of_smell_or_taste real, "
                                + " cough real, "
                                + " shortness_of_breath real, "
                                + " Feeling_tired real ); " );

                        db.setTransactionSuccessful();
                        Toast.makeText(MainActivity.this, "Database Table added with name: "+lastNameValue, Toast.LENGTH_LONG).show();
                    }
                    catch (SQLiteException e) {
//                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    finally {
                        db.endTransaction();
                    }
                }catch (SQLException e){
//                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

//                Toast.makeText(getApplicationContext(), "Adding db entry", Toast.LENGTH_SHORT).show();

                if(heartRateData != null && respiratoryData != null){
                    addRespHeartData(heartRateData, respiratoryData);
                    Log.d("Log", "db entry added");
                    Toast.makeText(getApplicationContext(), "Heart Rate and Respiratory Rate added to DB", Toast.LENGTH_SHORT).show();
                }
                else if (heartRateData == null || respiratoryData == null){
                    addRespHeartData(heartRateData, respiratoryData);
                    Log.d("Log", "db entry added");
                    Toast.makeText(getApplicationContext(), "Added to DB. (One of the values is null)", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Null values found. Please measure heart rate and respiratory rate again", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Listens for local broadcast containing Z values sent by AccelerometerService for calculation
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                float breathingRate;
                ArrayList<Integer> accelValuesZ;

                Bundle bundle = intent.getExtras();
                accelValuesZ = bundle.getIntegerArrayList("accelValuesZ");

                //Noise reduction from Accelerometer Z values
                ArrayList<Integer> accelValuesXDenoised = denoise(accelValuesZ, 10);

                //Peak detection algorithm running on denoised Accelerometer Z values
                int  zeroCrossings = peakFinding(accelValuesXDenoised);
                breathingRate = (zeroCrossings*60)/90;
                Log.i("log", "Respiratory rate" + breathingRate);

                textView2.setText(breathingRate + "");
                respiratoryData = breathingRate;

                Toast.makeText(MainActivity.this, "Respiratory rate calculated!", Toast.LENGTH_SHORT).show();
                RespiratoryProcIsRunning = false;
                bundle.clear();
                System.gc();

            }
        }, new IntentFilter("RespDataBroadcast"));

        //Listens for local broadcast containing average red values of extracted frames sent by Heart rate service for calculation
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle b = intent.getExtras();
                float heartRate = 0;
                int fail = 0;

                ArrayList<Integer> heartData = null;
                heartData = b.getIntegerArrayList("heartData");
                if(heartData == null){
                    Toast.makeText(MainActivity.this, "Null", Toast.LENGTH_SHORT).show();
                }
                Log.i("log", "heartData size: "+heartData.size());

                ArrayList<Integer> denoisedRedness = denoise(heartData, 2);

                float zeroCrossings = peakFinding(denoisedRedness);
                heartRate += zeroCrossings/2;
                Log.i("log", "heart rate: " + zeroCrossings/2);


                heartRate = (heartRate*scaler)*12/9;
                Log.i("log", "Final heart rate: " + heartRate);
                textView.setText(heartRate + "");
                heartRateData = heartRate;
                heartRateProcIsRunning = false;
                Toast.makeText(MainActivity.this, "Heart rate calculated!", Toast.LENGTH_SHORT).show();
                System.gc();
                b.clear();

            }
        }, new IntentFilter("broadcastingHeartData"));

    }

    public void startRecording()
    {
        File mediaFile = new File(getExternalFilesDir(null), "myvideo.mp4");

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,45);

        fileUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getApplicationContext().getPackageName() + ".provider", mediaFile);
        Log.d("", fileUri.getPath().toString());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, VIDEO_CAPTURE);
    }

    private boolean hasCamera() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<Integer> denoise(ArrayList<Integer> data, int filter){

        ArrayList<Integer> movingAvgArr = new ArrayList<>();
        int movingAvg = 0;

        for(int i=0; i< data.size(); i++){
            movingAvg += data.get(i);
            if(i+1 < filter) {
                continue;
            }
            movingAvgArr.add((movingAvg)/filter);
            movingAvg -= data.get(i+1 - filter);
        }

        return movingAvgArr;

    }

    public int peakFinding(ArrayList<Integer> data) {

        int diff, prev, slope = 0, zeroCrossings = 0;
        int j = 0;
        prev = data.get(0);

        //Get initial slope
        while(slope == 0 && j + 1 < data.size()){
            diff = data.get(j + 1) - data.get(j);
            if(diff != 0){
                slope = diff/abs(diff);
            }
            j++;
        }

        //Get total number of zero crossings in data curve
        for(int i = 1; i<data.size(); i++) {

            diff = data.get(i) - prev;
            prev = data.get(i);

            if(diff == 0) continue;

            int currSlope = diff/abs(diff);

            if(currSlope == -1* slope){
                slope *= -1;
                zeroCrossings++;
            }
        }

        return zeroCrossings;
    }

    public void addRespHeartData(Float heartData, Float respData){
        if(heartData == null) heartData = Float.parseFloat("0");
        if(respData == null) respData = Float.parseFloat("0");
        db.beginTransaction();
        try {
            db.execSQL( "insert into "+lastNameValue+" (heart_rate, respiratory_rate) values ('"+heartData+"', '"+respData+"' );" );
            db.setTransactionSuccessful();
        }
        catch (SQLiteException e) {
            Toast.makeText(getApplicationContext(), "error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("", "SQliteException" + e.getMessage());
        }
        finally {
            db.endTransaction();
        }
    }

}