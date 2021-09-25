package com.example.myadminapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class UploadPdf extends AppCompatActivity {

    CardView uploadPdf;
    EditText pdfTitle;
    TextView pdfTextView;
    Button uploadPdfBtn;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private String downloadUrl = "";
    private String pdfName ;
    private String title;

    private ProgressDialog pd;

    private final int REQUEST_CODE = 1;
    private Uri pdfData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_pdf);

        //set ids
        pdfTextView = findViewById(R.id.pdfTextView);
        uploadPdf = findViewById(R.id.addPdf);
        pdfTitle = findViewById(R.id.pdfTitle);
        uploadPdfBtn = findViewById(R.id.uploadPdfBtn);

        databaseReference = FirebaseDatabase.getInstance("https://lnct-bhopal-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference(); // VVVVVIMP STEP
        storageReference = FirebaseStorage.getInstance("gs://lnct-bhopal.appspot.com").getReference();

        pd = new ProgressDialog(UploadPdf.this);


        uploadPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Intent for opening Gallery
                Intent intent = new Intent();
                intent.setType("pdf/docs/ppt"); // we can use * for all files
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Pdf Title"), REQUEST_CODE);
            }
        });


        uploadPdfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = pdfTitle.getText().toString();
                if(title.isEmpty()) {
                    pdfTitle.setError("Empty!");
                    pdfTitle.requestFocus();
                }
                else if(pdfData == null){
                    Toast.makeText(UploadPdf.this, "Please Upload PDF", Toast.LENGTH_SHORT).show();
                }
                else {
                    uploadPDF();
                }
            }
        });

    }

    private void uploadPDF() {

        pd.setTitle("Please wait....");
        pd.setMessage("Uploading Pdf");
        pd.show();
        StorageReference reference = storageReference.child("Pdf" + pdfName + "-" + System.currentTimeMillis() + ".pdf"); // time islye jisse pdf uniquely identify ho jaye
        reference.putFile(pdfData)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //if everything goes fine then we will get url of pdf here
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                        // program ka execution tb tk rok kr rkhna hai jb tk uri mil nh jata

                        while( !uriTask.isComplete());

                        Uri uri = uriTask.getResult();
                        uploadData(String.valueOf(uri));
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(UploadPdf.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadData(String valueOf) {
        String uniqueKey = databaseReference.child("pdf").push().getKey(); // isme pdf get krenge

        HashMap data = new HashMap();
        data.put("PDFTitle", title);
        data.put("PDFUrl", downloadUrl);

        databaseReference.child("Pdf").child(uniqueKey).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                Toast.makeText(UploadPdf.this, "PDF uploaded successfully!", Toast.LENGTH_SHORT).show();
                pdfTitle.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(UploadPdf.this, "Fail to upload pdf", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // is method ko overRide krna pdega image ko gallery se pick krne k liye.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //gallery se image tb hi lenge jb ye condition true hogi

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
           pdfData = data.getData();

           if(pdfData.toString().startsWith("content://")) {
                Cursor cursor = null;
                cursor = UploadPdf.this.getContentResolver().query(pdfData, null, null, null, null);
               try {
                   if(cursor != null && cursor.moveToFirst()) {
                       pdfName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));  // here will get pdf name
                   }
               } catch (Exception e) {
                   e.printStackTrace();
               }

           } else if(pdfData.toString().startsWith("file://")) {
               pdfName = new File(pdfData.toString()).getName();   // here will get pdf name
           }
           pdfTextView.setText(pdfName);

        }
    }

}