package com.example.myadminapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    CardView addNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set ids
        addNotice = findViewById(R.id.addNotice);

        // action of buttons
//        addNotice.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(MainActivity.this, AddNotice.class);
//                startActivity(i);
//            }
//        });
    }

    public void add_the_notice(View view) {
        Intent i = new Intent(MainActivity.this, AddNotice.class);
        startActivity(i);
    }
}