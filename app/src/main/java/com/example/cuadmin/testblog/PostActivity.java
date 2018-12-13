package com.example.cuadmin.testblog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class PostActivity extends AppCompatActivity
{

    private Toolbar toolbar;
    private EditText picText;
    private Button postBtn;
    private ImageView postImage;

    private Uri postImageUri;
    private ProgressBar progressbar;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String current_user_id;
    private Bitmap compressedImageFile;

    private ImageView image_test;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Main Page");

        picText = findViewById(R.id.descText);
        postBtn = findViewById(R.id.postBtn);
        postImage = findViewById(R.id.imageView);
        progressbar = findViewById(R.id.progressBar2);


        postImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1,1)
                        .start(PostActivity.this);
            }
        });

        postBtn.setOnClickListener(new View.OnClickListener()
        {
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
                                    compressedImageFile = new Compressor(PostActivity.this).compressToBitmap(newImageFile);
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
                                        Map<String, Object>postMap = new HashMap<>();
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
                                                    Context context = getApplicationContext();
                                                    CharSequence msg = "Post was added";
                                                    int duration = Toast.LENGTH_LONG;
                                                    Toast toast = Toast.makeText(context,msg,duration);
                                                    toast.show();
                                                    Intent intent = new Intent(PostActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                                else
                                                {
                                                    String error = task.getException().getMessage();
                                                    Context context = getApplicationContext();
                                                    CharSequence msg = "ERROR: ";
                                                    int duration = Toast.LENGTH_LONG;
                                                    Toast toast = Toast.makeText(context,msg + error,duration);
                                                    toast.show();
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
                                Context context = getApplicationContext();
                                CharSequence msg = "ERROR: ";
                                int duration = Toast.LENGTH_LONG;
                                Toast toast = Toast.makeText(context,msg + error,duration);
                                toast.show();
                            }
                        }
                    });
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                postImageUri = result.getUri();
                postImage.setImageURI(postImageUri);
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }
    }


}
