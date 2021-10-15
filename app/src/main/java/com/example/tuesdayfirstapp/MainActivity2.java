package com.example.tuesdayfirstapp;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.media.Rating;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class MainActivity2 extends AppCompatActivity {

    String[] items = new String[]{"Nausea", "Headache", "Diarrhea", "Soar Throat", "Fever", "Muscle Ache", "Loss of Smell or Taste", "Cough", "Shortness of Breath", "Feeling Tired"};
    Map<String, Float> symptomRatingMap = new HashMap<String, Float>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Dropdown menu for selecting the symptom.
        Spinner dropdown = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);


        // The stars rating bar widget
        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                int symptomInd = dropdown.getSelectedItemPosition();
                symptomRatingMap.put(items[symptomInd], v);
            }
        });

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                ratingBar.setRating(symptomRatingMap.get(items[position]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        // Initialize all the symptom ratings to zero
        for(int i = 0;i < items.length;i++){
            symptomRatingMap.put(items[i], (float) 0);
        }

        //Update the last 10 columns of the db
        Button btn5 = (Button) findViewById(R.id.button5);

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "pressed symptoms upload", Toast.LENGTH_SHORT).show();

                SQLiteDatabase db = MainActivity.db;

                db.beginTransaction();
                try {
                    //perform your database operations here ...
                    db.execSQL( "update "+MainActivity.lastNameValue+" set Nausea='"+symptomRatingMap.get(items[0])+"', Headache='"+symptomRatingMap.get(items[1])+"', Diarrhea='"+symptomRatingMap.get(items[2])+"', Soar_throat='"+symptomRatingMap.get(items[3])+"', Fever='"+symptomRatingMap.get(items[4])+"', Muscle_Ache='"+symptomRatingMap.get(items[5])+"', Loss_of_smell_or_taste='"+symptomRatingMap.get(items[6])+"', cough='"+symptomRatingMap.get(items[7])+"', shortness_of_breath='"+symptomRatingMap.get(items[8])+"', Feeling_tired='"+symptomRatingMap.get(items[9])+"' where recID = (SELECT MAX(recID) FROM "+MainActivity.lastNameValue+")" );
                    db.setTransactionSuccessful();
                }
                catch (SQLiteException e) {
                    Toast.makeText(getApplicationContext(), "error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("", "SQliteException");
                }
                finally {
                    db.endTransaction();
                }
                Toast.makeText(getApplicationContext(), "db entry added", Toast.LENGTH_SHORT).show();
                Log.d("", "db entry added");
            }
        });

    }
}
