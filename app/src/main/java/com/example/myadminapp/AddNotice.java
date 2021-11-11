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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.security.PrivilegedExceptionAction;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddNotice extends AppCompatActivity {

    CardView addImage;
    ImageView preViewImage;
    EditText noticeTitle;
    Button uploadNoticeBtn;

    private DatabaseReference reference;
    private StorageReference storageReference;
    private String downloadUrl = "";

    private ProgressDialog pd;

    private final int REQUEST_CODE = 1;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notice);

        //set ids
        addImage = findViewById(R.id.addImage);
        preViewImage = findViewById(R.id.noticeImageView);
        noticeTitle = findViewById(R.id.noticeTitle);
        uploadNoticeBtn = findViewById(R.id.uploadNoticeBtn);

//        reference = FirebaseDatabase.getInstance("https://lnct-bhopal-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference(); // VVVVVIMP STEP
//        storageReference = FirebaseStorage.getInstance("gs://lnct-bhopal.appspot.com").getReference();

        reference = FirebaseDatabase.getInstance().getReference(); // VVVVVIMP STEP
        storageReference = FirebaseStorage.getInstance().getReference();

        pd = new ProgressDialog(AddNotice.this);


        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Intent for opening Gallery
                Intent pickImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickImage, REQUEST_CODE);
            }
        });

        uploadNoticeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(noticeTitle.getText().toString().isEmpty()) {
                    noticeTitle.setError("Empty!");
                    noticeTitle.requestFocus();
                }
                else if(bitmap == null){
                    uploadData();
                }
                else { // means title aur photo dono fill kri gyi hai
                    uploadImage();
                }
            }
        });

    }


    private void uploadData() {
        reference = reference.child("Notice");
        final  String uniqueKey = reference.push().getKey(); // Sara data ab notice folder k ander key me jaker store hoga

        String title = noticeTitle.getText().toString();

        //for current date and time of notice
        Calendar calDate = Calendar.getInstance();
        SimpleDateFormat currdate = new SimpleDateFormat("dd-MM-yy");
        String date = currdate.format(calDate.getTime());

        Calendar calTime = Calendar.getInstance();
        SimpleDateFormat currTime = new SimpleDateFormat("hh:mm:a");
        String time = currTime.format(calTime.getTime());

        //make obj of NoticeData class (custom class .. which we have created)
        NoticeData noticeData = new NoticeData(title, downloadUrl, date, time, uniqueKey);

        //now storing data to firebase

        //....is unique key me Notice ke ander Key me saara data stored hai islye child() k argument me unique key is pass kr di
        reference.child(uniqueKey).setValue(noticeData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pd.dismiss();
                Toast.makeText(AddNotice.this, "Notice Uploaded Successfully!!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddNotice.this, "Something went wrong11", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImage() {
        pd.setMessage("Uploading...");
        pd.show();

        //first compress image and then will upload it
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);

        byte[] finalImg = baos.toByteArray();// this will be the final image which we r going to store in db

        // now how to store....see below
        final StorageReference filePath;
        filePath = storageReference.child("Notice").child(finalImg + "jpg");

        //use UploadTask Class
        final UploadTask upTask = filePath.putBytes(finalImg);

        upTask.addOnCompleteListener(AddNotice.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
               if(task.isSuccessful()) {

                   //if task is successfull means ki image cloud storage pr upload hogyi and now we want its path so which
                   //we can upload it to database also

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
                   Toast.makeText(AddNotice.this, "Something went wrong in uploading image.", Toast.LENGTH_SHORT).show();  //yhn hai gdbd
               }
            }
        });

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

            preViewImage.setImageBitmap(bitmap);
            // manifest me jaker external storage ki prmsn bhi leni pdegi
        }
    }


}