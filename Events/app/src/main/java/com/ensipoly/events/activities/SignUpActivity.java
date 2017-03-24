package com.ensipoly.events.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ensipoly.events.FirebaseUtils;
import com.ensipoly.events.R;
import com.ensipoly.events.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

public class SignUpActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView mImageView;

    private DatabaseReference mUserDBReference;
    private StorageReference mStorageReference;
    private byte[] mPhotoData;
    private String mFileName;
    private String mAbsolutePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up);

        // Fullscreen activity
        View contentView = findViewById(R.id.form);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        // View to show image when user will have taken a picture
        mImageView = (ImageView) findViewById(R.id.image_pic);

        Button signUpButton = (Button) findViewById(R.id.btn_signup);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = ((EditText) findViewById(R.id.input_username)).getText().toString();
                findViewById(R.id.input_username).clearFocus();
                TextInputLayout wrapper = (TextInputLayout) findViewById(R.id.input_username_wrapper);
                if (username.trim().equals(""))
                    wrapper.setError("Username field is required");
                else if (mFileName == null || mAbsolutePath == null || mPhotoData == null)
                    Toast.makeText(SignUpActivity.this, "Image required", Toast.LENGTH_SHORT).show();
                else
                    createUser(username);

            }
        });

        Button cameraButton = (Button) findViewById(R.id.btn_pic);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });


        mUserDBReference = FirebaseUtils.getUserDBReference();
        mStorageReference = FirebaseStorage.getInstance().getReference("user_pp_photos");
    }

    private void createUser(final String username) {
        // First upload the picture
        StorageReference photoRef = mStorageReference.child(mFileName);
        photoRef.putBytes(mPhotoData).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Create the user data
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                final User user = new User(username, downloadUrl.toString(), 0, 0, DateFormat.getTimeInstance().format(new Date()));
                DatabaseReference ref = mUserDBReference.push();
                Task<Void> task = ref.setValue(user);
                final String user_id = ref.getKey();
                task.addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // When the user is created, save it's ID in preference for future use.
                        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(User.USER_ID_KEY_PREFERENCE, user_id);
                        editor.commit();
                        Intent intent = new Intent(SignUpActivity.this, GroupsActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Get photo data and show it
            Bitmap imageBitmap = BitmapFactory.decodeFile(mAbsolutePath);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            mPhotoData = stream.toByteArray();
            mImageView.setImageBitmap(imageBitmap);
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        mFileName = UUID.randomUUID().toString();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                mFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mAbsolutePath = image.getAbsolutePath();
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                // Error occurred while creating the File
                e.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.ensipoly.events.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

}
