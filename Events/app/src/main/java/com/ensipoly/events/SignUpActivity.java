package com.ensipoly.events;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SignUpActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView mImageView;

    FirebaseDatabase mFirebaseDatabase;
    FirebaseStorage mFirebaseStorage;
    DatabaseReference mUserDBReference;
    StorageReference mStorageReference;
    private byte[] mPhotoData;
    private Uri mUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up);

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

        mImageView = (ImageView) findViewById(R.id.image_pic);

        Button signUpButton = (Button) findViewById(R.id.btn_signup);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = ((EditText)findViewById(R.id.input_username)).getText().toString();
                findViewById(R.id.input_username).clearFocus();
                TextInputLayout wrapper = (TextInputLayout)findViewById(R.id.input_username_wrapper);
                if(username.equals("")){
                    wrapper.setError("Username field is required"); // TODO R.string
                }else if(!usernameAvailable(username)){
                    wrapper.setError("Username not available"); // TODO R.string
                }else if(mUri == null){
                    Toast.makeText(SignUpActivity.this,"Image required",Toast.LENGTH_SHORT).show(); //TODO R.string
                } else {
                    createUser(username);
                }
            }
        });

        Button cameraButton = (Button) findViewById(R.id.btn_pic);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mUserDBReference = mFirebaseDatabase.getReference("users");
        mStorageReference = mFirebaseStorage.getReference("user_pp_photos");
    }

    private void createUser(final String username) {
        StorageReference photoRef = mStorageReference.child(mUri.getLastPathSegment());
        photoRef.putBytes(mPhotoData).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                final User user = new User(username,downloadUrl.toString());
                DatabaseReference ref = mUserDBReference.push();
                Task<Void> task = ref.setValue(user);
                final String user_id = ref.getKey();
                task.addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(User.USER_ID_KEY_PREFERENCE,user_id);
                        editor.commit();
                        UserDbHelper.getHelper(SignUpActivity.this).addUser(user_id, user);
                        Intent intent = new Intent(SignUpActivity.this,MainActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });

    }

    private boolean usernameAvailable(String username) {
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            mUri = data.getData();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            mPhotoData = stream.toByteArray();
            mImageView.setImageBitmap(imageBitmap);
        }
    }
}
