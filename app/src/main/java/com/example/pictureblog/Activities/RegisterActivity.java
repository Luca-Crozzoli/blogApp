package com.example.pictureblog.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pictureblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {

    ImageView imgUserPhoto;
    static int PreqCode = 1; //Permission request code
    static int REQUESCODE = 1;
    Uri pickedImgUri;

    private EditText userName, userEmail, userPassword, userPassword2;
    private ProgressBar progressBar;
    private Button regButton;
    private TextView regLoginText;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_register );

        //initialization of views
        imgUserPhoto = findViewById( R.id.regUserPhoto );
        userName = findViewById( R.id.regName );
        userEmail = findViewById( R.id.regMail );
        userPassword = findViewById( R.id.regPassword );
        userPassword2 = findViewById( R.id.regPassword2 );
        progressBar = findViewById( R.id.regProgressBar );
        progressBar.setVisibility( View.INVISIBLE );
        regButton = findViewById( R.id.regButton );
        regLoginText = findViewById( R.id.regLoginTextView );

        //get firebase instance
        mAuth = FirebaseAuth.getInstance();

        regButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regButton.setVisibility( View.INVISIBLE );
                progressBar.setVisibility( View.VISIBLE );
                final String name = userName.getText().toString().trim();
                final String mail = userEmail.getText().toString().trim();
                final String password = userPassword.getText().toString().trim();
                final String password2 = userPassword2.getText().toString().trim();

                if (name.isEmpty()) {
                    userName.setError( "Full Name is required" );
                    userName.requestFocus();
                    regButton.setVisibility( View.VISIBLE );
                    progressBar.setVisibility( View.INVISIBLE );
                    return;
                }

                if (mail.isEmpty()) {
                    userEmail.setError( "Mail is required" );
                    userEmail.requestFocus();
                    regButton.setVisibility( View.VISIBLE );
                    progressBar.setVisibility( View.INVISIBLE );
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher( mail ).matches()) {
                    userEmail.setError( "Provide a valid Mail" );
                    userEmail.requestFocus();
                    regButton.setVisibility( View.VISIBLE );
                    progressBar.setVisibility( View.INVISIBLE );
                    return;
                }

                if (password.isEmpty()) {
                    userPassword.setError( "Password is required" );
                    userPassword.requestFocus();
                    regButton.setVisibility( View.VISIBLE );
                    progressBar.setVisibility( View.INVISIBLE );
                    return;
                }
                //for firebase the password must be at least 6 chars long
                if (password.length() < 6) {
                    userPassword.setError( "Min length 6 characters" );
                    userPassword.requestFocus();
                    regButton.setVisibility( View.VISIBLE );
                    progressBar.setVisibility( View.INVISIBLE );
                    return;
                }

                if (!password.equals( password2 )) {
                    userPassword2.setError( "The 2 password do not correspond" );
                    userPassword2.requestFocus();
                    regButton.setVisibility( View.VISIBLE );
                    progressBar.setVisibility( View.INVISIBLE );
                    return;
                }

                //TODO this is a commented if statement because we are implementi anothr method to allow the user registration without
                //uploading the user photo
                /*if(imgUserPhoto.getDrawable().getConstantState()==getResources().getDrawable(R.drawable.userphoto,getTheme() ).getConstantState()){
                    showMessage( "You need to upload your image!" );
                    imgUserPhoto.requestFocus();
                    regButton.setVisibility( View.VISIBLE );
                    progressBar.setVisibility( View.INVISIBLE );
                    return;
                }*/

                //Everything is okay then we can start to create a new user
                //create user account method will try to create the user if the email is valid

                CreateUserAccount( name, mail, password );
            }
        } );


        imgUserPhoto.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 22) {
                    checkAndRequestForPermission();
                } else {
                    openGallery();
                }
            }
        } );

        regLoginText.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginActivity = new Intent( getApplicationContext(), LoginActivity.class );
                startActivity( loginActivity );
                finish();
            }
        } );
    }

    private void CreateUserAccount(String name, String mail, String password) {
        //this method create user account with specific email and password
        mAuth.createUserWithEmailAndPassword( mail, password )
                .addOnCompleteListener( this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //user account created successfully
                            showMessage( "New User account registration done!" );
                            //after created the user account we need to update his picture and name
                            updateUserInfo( name, pickedImgUri, mAuth.getCurrentUser() );
                        } else {
                            //user account creation failed
                            showMessage( "New user registration failed" + task.getException().getMessage() );
                            regButton.setVisibility( View.VISIBLE );
                            progressBar.setVisibility( View.INVISIBLE );
                        }
                    }
                } );
    }

    // update user name and image
    private void updateUserInfo(String name, Uri pickedImgUri, FirebaseUser currentUser) {
        //first upload user image to firebase storage and get url
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child( "users_photos" );
        //if no image is picked set the uri with the default image we have in drawable
        if(pickedImgUri == null){
            Resources resources = getResources();
            pickedImgUri = Uri.parse( ContentResolver.SCHEME_ANDROID_RESOURCE+"://"+ resources.getResourcePackageName( R.drawable.userphoto)+'/'+
                    resources.getResourceTypeName( R.drawable.userphoto )+'/'+
                    resources.getResourceEntryName( R.drawable.userphoto ));
        }
        StorageReference imageFilePath = mStorage.child( pickedImgUri.getLastPathSegment() );
        imageFilePath.putFile( pickedImgUri ).addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // image uploaded successfully
                //now we can get our image url

                imageFilePath.getDownloadUrl().addOnSuccessListener( new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //Uri contain user image url
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName( name )
                                .setPhotoUri( uri )
                                .build();

                        currentUser.updateProfile( profileUpdate )
                                .addOnCompleteListener( new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //user info updated successfully
                                            showMessage( "Register Complete" );
                                            updateUI();
                                        }

                                    }
                                } );
                    }
                } );
            }
        } );

    }


    private void updateUI() {
        Intent loginActivity = new Intent( getApplicationContext(), LoginActivity.class ); //TODO in the video tutorial Home.class
        startActivity( loginActivity );
        finish();
    }

    //simple method to show toast message
    private void showMessage(String message) {
        Toast.makeText( getApplicationContext(), message, Toast.LENGTH_LONG ).show();
    }


    private void openGallery() {
        //TODO: openGallery intent and wait for user to pick a photo
        Intent galleryIntent = new Intent( Intent.ACTION_GET_CONTENT );
        galleryIntent.setType( "image/*" );
        startActivityForResult( galleryIntent, REQUESCODE ); //TODO deprecated method find an alternative!!

    }

    private void checkAndRequestForPermission() {
        //If the permission are not allowed!!
        if (ContextCompat.checkSelfPermission( RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale( RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE )) {
                Toast.makeText( RegisterActivity.this, "Please accept for required permission", Toast.LENGTH_LONG ).show();
            } else {
                ActivityCompat.requestPermissions( RegisterActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PreqCode );
            }
        } else {
            openGallery();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null) {
            //the user has successfully picked the image
            // we need to save its reference to a URI variable
            pickedImgUri = data.getData();
            imgUserPhoto.setImageURI( pickedImgUri );
        }
    }
}