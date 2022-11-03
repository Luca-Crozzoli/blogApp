package com.example.pictureblog.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.pictureblog.Helpers.ToastShort;
import com.example.pictureblog.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
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

    private ImageView imgUserPhoto;
    private Uri pickedImgUri;
    private EditText userName, userEmail, userPassword, userPassword2;
    private ProgressBar progressBar;
    private Button regButton;
    private TextView regLoginText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_register );

        imgUserPhoto = findViewById( R.id.regUserPhoto );
        userName = findViewById( R.id.regName );
        userEmail = findViewById( R.id.regMail );
        userPassword = findViewById( R.id.regPassword );
        userPassword2 = findViewById( R.id.regPassword2 );
        progressBar = findViewById( R.id.regProgressBar );
        progressBar.setVisibility( View.INVISIBLE );
        regButton = findViewById( R.id.regButton );
        regLoginText = findViewById( R.id.regLoginTextView );

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
                    progressBar.setVisibility( View.GONE );
                    return;
                }

                if (mail.isEmpty()) {
                    userEmail.setError( "Mail is required" );
                    userEmail.requestFocus();
                    regButton.setVisibility( View.VISIBLE );
                    progressBar.setVisibility( View.GONE );
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher( mail ).matches()) {
                    userEmail.setError( "Provide a valid Mail" );
                    userEmail.requestFocus();
                    regButton.setVisibility( View.VISIBLE );
                    progressBar.setVisibility( View.GONE );
                    return;
                }

                if (password.isEmpty()) {
                    userPassword.setError( "Password is required" );
                    userPassword.requestFocus();
                    regButton.setVisibility( View.VISIBLE );
                    progressBar.setVisibility( View.GONE );
                    return;
                }
                //for firebase the password must be at least 6 chars long
                if (password.length() < 6) {
                    userPassword.setError( "Min length 6 characters" );
                    userPassword.requestFocus();
                    regButton.setVisibility( View.VISIBLE );
                    progressBar.setVisibility( View.GONE );
                    return;
                }

                if (!password.equals( password2 )) {
                    userPassword2.setError( "The passwords do not correspond" );
                    userPassword2.requestFocus();
                    regButton.setVisibility( View.VISIBLE );
                    progressBar.setVisibility( View.GONE );
                    return;
                }
                CreateUserAccount( name, mail, password );
            }
        } );


        imgUserPhoto.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.Companion.with( RegisterActivity.this )
                        .cropSquare()	    			/*//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)*/
                        .start();
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

    @Override
    public void onBackPressed() {

        Intent loginActivity = new Intent( getApplicationContext(), LoginActivity.class );
        startActivity( loginActivity );
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        pickedImgUri = data.getData();
        imgUserPhoto.setImageURI( pickedImgUri );
    }

    private void CreateUserAccount(String name, String mail, String password) {
        mAuth.createUserWithEmailAndPassword( mail, password )
                .addOnCompleteListener( this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //user account created successfully
                            ToastShort ToastS = new ToastShort( "New User account registration done!", getApplicationContext() );
                            ToastS.showMessage();
                            //after created the user account we need to update his picture and name
                            updateUserInfo( name, pickedImgUri, mAuth.getCurrentUser() );
                        } else {
                            //user account creation failed
                            ToastShort ToastS = new ToastShort( "New user registration failed" + task.getException().getMessage(), getApplicationContext() );
                            ToastS.showMessage();
                            regButton.setVisibility( View.VISIBLE );
                            progressBar.setVisibility( View.GONE );
                        }
                    }
                } );
    }

    private void updateUserInfo(String name, Uri pickedImgUri, FirebaseUser currentUser) {
        //first upload user image to firebase storage and get url
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child( "users_photos" );
        //if no image is picked set the uri with the default image we have in drawable
        if (pickedImgUri == null) {
            Resources resources = getResources();
            pickedImgUri = Uri.parse( ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName( R.drawable.userphoto ) + '/' +
                    resources.getResourceTypeName( R.drawable.userphoto ) + '/' +
                    resources.getResourceEntryName( R.drawable.userphoto ) );
        }
        StorageReference imageFilePath = mStorage.child( pickedImgUri.getLastPathSegment() );
        imageFilePath.putFile( pickedImgUri ).addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // image uploaded successfully, we can get image url
                imageFilePath.getDownloadUrl().addOnSuccessListener( new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //Uri contain user image url, request for update the profile
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName( name )
                                .setPhotoUri( uri )
                                .build();
                        //take the previous request to update the profile information
                        currentUser.updateProfile( profileUpdate )
                                .addOnCompleteListener( new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //user info updated successfully
                                            ToastShort ToastS = new ToastShort( "Registration Completed", getApplicationContext() );
                                            ToastS.showMessage();
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
        Intent homeActivity = new Intent( getApplicationContext(), HomeActivity.class );
        startActivity( homeActivity );
        finish();
    }
}