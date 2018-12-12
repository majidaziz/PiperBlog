package com.example.cuadmin.testblog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class UploadPost extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_post);

        AlertDialog.Builder alertTwoButtons = new AlertDialog.Builder(UploadPost.this);
        alertTwoButtons.setTitle("Uploading Content")
                .setMessage("What do you want to upload?")
                .setPositiveButton("Photo",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent photoUpload = new Intent(UploadPost.this, PostActivity.class);
                                startActivity(photoUpload);
                            }
                        })
                .setNegativeButton("Text", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent textUpload = new Intent(UploadPost.this, PostTextActivity.class);
                        startActivity(textUpload);
                    }
                });
        AlertDialog alertDialog = alertTwoButtons.create();
        alertDialog.show();
    }
}
