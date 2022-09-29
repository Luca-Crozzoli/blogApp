package com.example.pictureblog.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.pictureblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText userMail, userPassword;
    private Button btnLogin;
    private ProgressBar loginProgress;
    //Initialize mAuth variable
    private FirebaseAuth mAuth;
    //Initialize the intent used in the updateUI method
    private Intent homeActivity;
    private ImageView loginPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login );

        userMail = findViewById( R.id.login_mail );
        userPassword = findViewById( R.id.login_password );

        loginProgress = findViewById( R.id.login_progress );
        loginProgress.setVisibility( View.INVISIBLE );

        //Get the firebase instance
        mAuth = FirebaseAuth.getInstance();
        //initialize the Intent to move user at home after the login
        homeActivity = new Intent(this, Home.class);

        loginPhoto = findViewById( R.id.login_photo );

        //we redirect the user to register Activity if he/she clicks on the photo
        loginPhoto.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerActivity = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity( registerActivity );
                finish();
            }
        } );



        btnLogin=findViewById( R.id.logout_button );
        btnLogin.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginProgress.setVisibility( View.VISIBLE );
                btnLogin.setVisibility( View.INVISIBLE );

                final String mail = userMail.getText().toString().trim();
                final String password = userPassword.getText().toString().trim();

                //check the fields of the mail and password
                if (mail.isEmpty()) {
                    userMail.setError( "Mail is required" );
                    userMail.requestFocus();
                    btnLogin.setVisibility( View.VISIBLE );
                    loginProgress.setVisibility( View.INVISIBLE );
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher( mail ).matches()) {
                    userMail.setError( "Provide a valid Mail" );
                    userMail.requestFocus();
                    btnLogin.setVisibility( View.VISIBLE );
                    loginProgress.setVisibility( View.INVISIBLE );
                    return;
                }

                if (password.isEmpty()) {
                    userPassword.setError( "Password is required" );
                    userPassword.requestFocus();
                    btnLogin.setVisibility( View.VISIBLE );
                    loginProgress.setVisibility( View.INVISIBLE );
                    return;
                }

                if(password.length()<6){
                    userPassword.setError( "Password doesn't satisfy the length of 6 characters" );
                    userPassword.requestFocus();
                    btnLogin.setVisibility( View.VISIBLE );
                    loginProgress.setVisibility( View.INVISIBLE );
                    return;

                }

                signIn(mail,password);
            }
        } );
    }

    private void signIn(String mail, String password) {
        mAuth.signInWithEmailAndPassword( mail,password ).addOnCompleteListener( new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //if the sign in task is done the user is signed in
                if(task.isSuccessful()){
                    loginProgress.setVisibility( View.INVISIBLE );
                    btnLogin.setVisibility( View.VISIBLE );
                    updateUI();

                }else{
                    showMessage( task.getException().getMessage() );
                    btnLogin.setVisibility( View.VISIBLE );
                    loginProgress.setVisibility( View.INVISIBLE );
                }

            }
        } );

    }

    private void updateUI() {
        //move the user to the home activity
        startActivity( homeActivity );
        finish();
    }

    private void showMessage(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }


    //We do the override to let the user logged in if it is already logged in
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();

        if(user !=null){
            //user is already connected , need to redirect him to home page
            updateUI();
        }
    }
}