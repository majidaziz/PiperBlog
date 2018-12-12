package com.example.cuadmin.testblog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class PostTextActivity extends AppCompatActivity
{
    private Toolbar toolbar;
    private Button cancel;
    private Button postBtn;
    private EditText titlePost;
    private EditText textPost;

    private Uri postImageUri;
    private ProgressBar progressbar;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String current_user_id;
    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_text);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cancel = findViewById(R.id.cancel);
        postBtn = findViewById(R.id.post);

        progressbar = findViewById(R.id.progressBar2);
        titlePost = findViewById(R.id.titlePost);
        textPost = findViewById(R.id.txtPost);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Main Page");

        cancel.setOnClickListener(backHome);
        //postBtn.setOnClickListener(postText);

    }

    private View.OnClickListener backHome = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent backHome = new Intent(PostTextActivity.this, MainActivity.class);
            startActivity(backHome);
        }
    };

    /*private View.OnClickListener postText = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            final String desc = picText.getText().toString();
            if(!TextUtils.isEmpty(desc) && postImageUri != null)
            {
                progressbar.setVisibility(View.VISIBLE);
                final String randomName = UUID.randomUUID().toString();
                StorageReference filePath = storageReference.child("post_images").child(randomName + ".jpg");
                filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        final String downloadUri = task.getResult().getDownloadUrl().toString();
                        if(task.isSuccessful()){
                            File newImageFile = new File(postImageUri.getPath());
                            try
                            {
                                compressedImageFile = new Compressor(PostTextActivity.this).compressToBitmap(newImageFile);
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] thumbData = baos.toByteArray();
                            UploadTask uploadTask = storageReference.child("post_images/thumbs").child(randomName + ".jpg").putBytes(thumbData);
                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                            {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    String downloadthumbUri = taskSnapshot.getDownloadUrl().toString();
                                    Map<String, Object> postMap = new HashMap<>();
                                    postMap.put("image_url", downloadUri);
                                    postMap.put("image_thumb", downloadthumbUri);
                                    postMap.put("desc", desc);
                                    postMap.put("user_id", current_user_id);
                                    postMap.put("timestamp", FieldValue.serverTimestamp());
                                    firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(getApplicationContext(), "Post was added.", Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(PostTextActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                            else
                                            {
                                                String error = task.getException().getMessage();
                                                Toast.makeText(getApplicationContext(), "Error : " + error, Toast.LENGTH_LONG).show();
                                            }
                                            progressbar.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {

                                }
                            });


                        }
                        else
                        {
                            progressbar.setVisibility(View.INVISIBLE);
                            String error = task.getException().getMessage();
                            Toast.makeText(getApplicationContext(), "Error : " + error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
        });
    };*/
}
