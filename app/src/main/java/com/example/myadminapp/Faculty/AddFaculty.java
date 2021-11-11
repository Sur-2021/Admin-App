package com.example.myadminapp.Faculty;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.myadminapp.AddNotice;
import com.example.myadminapp.NoticeData;
import com.example.myadminapp.R;
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
import java.lang.ref.Reference;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddFaculty extends AppCompatActivity {

    ImageView addImage;
    EditText addName, addEmail, addPost;
    Button addTeacher;
    Spinner add_image_category;

    private final int REQUEST_CODE = 1;
    private Bitmap bitmap = null;

    private String category;
    private  String name, email, post, downloadUrl = "";

    private ProgressDialog pd;
    private StorageReference storageReference;
    private DatabaseReference reference, dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_faculty);

        //set ids
        addImage = findViewById(R.id.addImage);
        addName = findViewById(R.id.addTeacherName);
        addEmail = findViewById(R.id.addTeacherEmail);
        addPost = findViewById(R.id.addTeacherPost);
        addTeacher = findViewById(R.id.addTeacherBtn);

        add_image_category = findViewById(R.id.add_image_spinner);

        pd = new ProgressDialog(this);

        reference = FirebaseDatabase.getInstance().getReference().child("Teachers");
        storageReference = FirebaseStorage.getInstance().getReference();


        //list for items in Category
        String[] items = new String[] {"Select Category", "Computer Science", "Information Technology", "Electronics", "Electrical Engineering", "Chemical Engineering", "Mechanical Engineering", "Civil Engineering", "Agriculture Department", "MCA", "Pharmacy", "Other Departments"};
        add_image_category.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, items));

        add_image_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = add_image_category.getSelectedItem().toString(); // jo admin select krega wo value set hogi
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        //opening gallery
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        addTeacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkValidation();
            }
        });

    }

    private void checkValidation() {
        name = addName.getText().toString();
        email = addEmail.getText().toString();
        post = addPost.getText().toString();

        if(name.isEmpty()) {
            addTeacher.setError("Error");
            addTeacher.requestFocus();
        }
        else if(email.isEmpty()) {
            addEmail.setError("Error");
            addEmail.requestFocus();
        }
        else if(post.isEmpty()) {
            addPost.setError("Error");
            addPost.requestFocus();
        }
        else if(category.equals("Select Category")) {
            Toast.makeText(this, "Please select category!!", Toast.LENGTH_SHORT).show();
        }
        else if(bitmap == null) {
            uploadData();
        }
        else {
            pd.setMessage("Uploading...");
            pd.show();
            uploadImage();
        }

    }

    private void uploadData() {
        dbRef = reference.child(category);
        final  String uniqueKey = dbRef.push().getKey(); // Sara data ab notice folder k ander key me jaker store hoga

      //  String title = noticeTitle.getText().toString();


        //make obj of NoticeData
        TeacherData teacherData = new TeacherData(name, email, post, downloadUrl, uniqueKey);

        dbRef.child(uniqueKey).setValue(teacherData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pd.dismiss();
                Toast.makeText(AddFaculty.this, "Teacher added Successfully!!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddFaculty.this, "Something went wrong11", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImage() {


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);

        byte[] finalImg = baos.toByteArray();
        final StorageReference filePath;
        filePath = storageReference.child("Teachers").child(finalImg + "jpg");

        //use UploadTask Class
        final UploadTask upTask = filePath.putBytes(finalImg);

        upTask.addOnCompleteListener(AddFaculty.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
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
                    Toast.makeText(AddFaculty.this, "Something went wrong!22", Toast.LENGTH_SHORT).show();  //yhn hai gdbd
                }
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

            addImage.setImageBitmap(bitmap);
            // manifest me jaker external storage ki prmsn bhi leni pdegi
        }
    }
}