package com.example.myadminapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class UploadImage extends AppCompatActivity {

    private Spinner image_category;
    private CardView select_img_cv;
    private Button upload_img_btn;
    private ImageView gallery_image_view;

    private  String category;
    private final int REQUEST_CODE = 1;

    private Bitmap bitmap;
    ProgressDialog pd;

    String downloadUrl;

    private DatabaseReference reference;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        //set ids
        select_img_cv = findViewById(R.id.addGalleryImage);
        gallery_image_view = findViewById(R.id.galleryImageView);
        upload_img_btn = findViewById(R.id.uploadImageBtn);
        image_category = findViewById(R.id.image_category);

        pd = new ProgressDialog(this);

        reference = FirebaseDatabase.getInstance("https://lnct-bhopal-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference(); // VVVVVIMP STEP
        storageReference = FirebaseStorage.getInstance("gs://lnct-bhopal.appspot.com").getReference();



        //list for items in Category
        String[] items = new String[] {"Select Category", "Convocation", "Independence Day", "Tech Fest 2K21", "Other Events"};
        image_category.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, items));

        image_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = image_category.getSelectedItem().toString(); // jo admin select krega wo value set hogi
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //opening gallery
        select_img_cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });



        upload_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bitmap == null) {
                    Toast.makeText(UploadImage.this, "Please Select a Image!", Toast.LENGTH_SHORT).show();
                }
                else if(category == "Select Category") {
                    Toast.makeText(UploadImage.this, "Please select any Category!", Toast.LENGTH_SHORT).show();
                }
                else {
                    pd.setMessage("Uploading....");
                    pd.show();
                    uploadImage();
                }
            }
        });




    }

    private void uploadImage() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);

        byte[] finalImg = baos.toByteArray();
        final StorageReference filePath;
        filePath = storageReference.child(finalImg + "jpg");

        //use UploadTask Class
        final UploadTask upTask = filePath.putBytes(finalImg);

        upTask.addOnCompleteListener(UploadImage.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()) {
                    upTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    downloadUrl = String.valueOf(uri);
                                    uploadData();
                                }
                            });
                        }
                    });
                }
                else {
                    pd.dismiss();
                    Toast.makeText(UploadImage.this, "Something went wrong!22", Toast.LENGTH_SHORT).show();  //yhn hai gdbd
                }
            }
        });

    }

    private void uploadData() {
        reference = reference.child(category); // user jo category select krega
        final  String uniqueKey = reference.push().getKey();

        reference.child(uniqueKey).setValue(downloadUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pd.dismiss();
                Toast.makeText(UploadImage.this, "Image Uploaded Successfully!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(UploadImage.this, "Something went wrong in uploading image!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void openGallery() {
        //Intent for opening Gallery
        Intent pickImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickImage, REQUEST_CODE);
    }

    // is method ko overRide krna pdega image ko gallery se pick krne k liye.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //gallery se image tb hi lenge jb ye condition true hogi

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData(); // photo mil gyi but in form of uri thus use Bitmap here
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            gallery_image_view.setImageBitmap(bitmap);
            // manifest me jaker external storage ki prmsn bhi leni pdegi
        }
    }
}