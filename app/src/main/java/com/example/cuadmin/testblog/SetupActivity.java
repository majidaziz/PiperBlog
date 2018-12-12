package com.example.cuadmin.testblog;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity
{

    private CircleImageView setupImage;
    private Uri mainImageURI = null;

    private EditText setupName;
    private Button setupBtn;
    private ProgressBar progress_bar;

    private String user_id;
    private boolean isChanged = false;



    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Toolbar setupToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Setup");


        firebaseAuth = FirebaseAuth.getInstance();

        user_id = firebaseAuth.getCurrentUser().getUid();

        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        setupImage = findViewById(R.id.circleImageView);
        setupName = findViewById(R.id.edit_name);
        setupBtn = findViewById(R.id.button);
        progress_bar = findViewById(R.id.progressBar);


        progress_bar.setVisibility(View.VISIBLE);
        setupBtn.setEnabled(false);

        firebaseFirestore.collection("User").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if(task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("images");

                        mainImageURI = Uri.parse(image);

                        setupName.setText(name);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.mipmap.default_icon);
                        Glide.with(SetupActivity.this).load(image).into(setupImage);
                    }
                }
                else
                {

                }
                progress_bar.setVisibility(View.INVISIBLE);
                setupBtn.setEnabled(true);
            }
        });

        setupBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                final String user_name = setupName.getText().toString();
                if (!TextUtils.isEmpty(user_name) && mainImageURI != null)
                {
                    progress_bar.setVisibility(View.VISIBLE);
                    if(isChanged)
                    {


                        user_id = firebaseAuth.getCurrentUser().getUid();

                        StorageReference image_path = storageReference.child("profile_images").child(user_id + ".jpg");
                        image_path.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                            {
                                if (task.isSuccessful())
                                {
                                    storeFirestore(task, user_name);
                                }
                                else
                                 {
                                    String errormsg = task.getException().getMessage();
                                    Context context = getApplicationContext();
                                    CharSequence msg = "ERROR: ";
                                    int duration = Toast.LENGTH_LONG;
                                    Toast toast = Toast.makeText(context,msg + errormsg,duration);
                                    toast.show();
                                    progress_bar.setVisibility(View.INVISIBLE);
                                }

                            }
                        });

                    }
                    else
                    {
                        storeFirestore(null, user_name);
                    }
                }
            }
        });

        setupImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    {
                        Context context = getApplicationContext();
                        CharSequence msg = "Permission Denied ";
                        int duration = Toast.LENGTH_LONG;
                        Toast toast = Toast.makeText(context,msg,duration);
                        toast.show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                    else
                    {

                        bringImagePicker();
                    }
                }
                else
                {

                    bringImagePicker();
                }
            }
        });
    }

    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot> task, String user_name)
    {
        Uri download_uri;
        if(task != null)
        {
             download_uri = task.getResult().getDownloadUrl();
        }
        else
        {
             download_uri = mainImageURI;
        }
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", user_name);
        userMap.put("images", download_uri.toString());

        firebaseFirestore.collection("User").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }
                else
                {
                    String errormsg = task.getException().getMessage();
                    Context context = getApplicationContext();
                    CharSequence msg = "Firestore ";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context,msg + errormsg,duration);
                    toast.show();
                }

            }
        });
    }

    private void bringImagePicker()
    {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SetupActivity.this);
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
                mainImageURI= result.getUri();
                setupImage.setImageURI(mainImageURI);
                isChanged = true;
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }
    }
}
