package com.example.myadminapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.myadminapp.Faculty.UpdateFaculty;

public class MainActivity extends AppCompatActivity {

    CardView addNotice, uploadImage, uploadEbook, updateFaculty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set ids
        addNotice = findViewById(R.id.addNotice);
        uploadImage = findViewById(R.id.uploadImage);
        uploadEbook = findViewById(R.id.uploadEbook);
        updateFaculty = findViewById(R.id.updateFaculty);
    }

    // action of buttons
    public void add_the_notice(View view) {
        Intent i = new Intent(MainActivity.this, AddNotice.class);
        startActivity(i);
    }

    public void upload_img_btn(View view) {
        Intent i = new Intent(MainActivity.this, UploadImage.class);
        startActivity(i);
    }

    public void upload_pdf(View view) {
        Intent i = new Intent(MainActivity.this, UploadPdf.class);
        startActivity(i);
    }

    public void faculty_btn(View view) {
        Intent i = new Intent(MainActivity.this, UpdateFaculty.class);
        startActivity(i);
    }
}