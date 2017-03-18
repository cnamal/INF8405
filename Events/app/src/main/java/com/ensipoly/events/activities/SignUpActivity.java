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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ensipoly.events.FirebaseUtils;
import com.ensipoly.events.R;
import com.ensipoly.events.User;
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

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SignUpActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView mImageView;

    private FirebaseStorage mFirebaseStorage;
    private DatabaseReference mUserDBReference;
    private StorageReference mStorageReference;
    private byte[] mPhotoData;
    private String mFileName;
    private String mAbsolutePath;


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
                }else if(mFileName == null || mAbsolutePath == null || mPhotoData == null){
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
                try{
                File f = createImageFile();
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
                }catch (IOException e){
                    e.printStackTrace();
                    mFileName = null;
                    mAbsolutePath = null;
                }
            }
        });

        mFirebaseStorage = FirebaseStorage.getInstance();

        mUserDBReference = FirebaseUtils.getUserDBReference();
        mStorageReference = mFirebaseStorage.getReference("user_pp_photos");
    }

    private void createUser(final String username) {
        StorageReference photoRef = mStorageReference.child(mFileName);
        photoRef.putBytes(mPhotoData).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                final User user = new User(username,downloadUrl.toString(),0,0, DateFormat.getTimeInstance().format(new Date()));
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
                        Intent intent = new Intent(SignUpActivity.this,GroupsActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap imageBitmap = BitmapFactory.decodeFile(mAbsolutePath);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            mPhotoData = stream.toByteArray();
            mImageView.setImageBitmap(imageBitmap);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        mFileName = UUID.randomUUID().toString();
        File imageF = File.createTempFile(mFileName, ".jpg", storageDir);
        mAbsolutePath = imageF.getAbsolutePath();
        return imageF;
    }
}
