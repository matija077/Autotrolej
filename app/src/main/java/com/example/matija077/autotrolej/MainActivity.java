package com.example.matija077.autotrolej;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnTimeTable = (Button) findViewById(R.id.btnTime_table);
        Button btnMap = (Button) findViewById(R.id.btnMap);
        Button btnSettings = (Button) findViewById(R.id.btnSettings);
        Button btnExit = (Button) findViewById(R.id.btnExit);

        btnTimeTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent btnIntent = new Intent(MainActivity.this, TimeTableActivity.class);
                startActivity(btnIntent);
            }
        });

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent btnIntent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(btnIntent);
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent btnIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(btnIntent);
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: srediti exit da se ugasi app skroz
                finish();
                System.exit(0);
            }
        });
    }
}
