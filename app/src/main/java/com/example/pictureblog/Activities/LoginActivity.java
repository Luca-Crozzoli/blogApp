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
import android.widget.TextView;

import com.example.pictureblog.Helpers.ToastShort;
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
    private TextView registerText, forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login );

        userMail = findViewById( R.id.login_mail );
        userPassword = findViewById( R.id.login_password );

        loginProgress = findViewById( R.id.login_progress );
        loginProgress.setVisibility( View.GONE );

        //Get the firebase instance
        mAuth = FirebaseAuth.getInstance();
        //initialize the Intent to move user at home after the login
        homeActivity = new Intent( this, HomeActivity.class );

        loginPhoto = findViewById( R.id.login_photo );

        registerText = findViewById( R.id.register_text_view );


        //we redirect the user to register Activity if he/she clicks on the text view register
        registerText.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerActivity = new Intent( getApplicationContext(), RegisterActivity.class );
                startActivity( registerActivity );
                finish();
            }
        } );

        forgotPassword = findViewById( R.id.forgot_password );
        forgotPassword.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent forgotPasswordActivity = new Intent( getApplicationContext(), ForgotPasswordActivity.class );
                startActivity( forgotPasswordActivity );
                finish();
            }
        } );


        btnLogin = findViewById( R.id.login_button );
        btnLogin.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginProgress.setVisibility( View.VISIBLE );
                btnLogin.setVisibility( View.GONE );

                final String mail = userMail.getText().toString().trim();
                final String password = userPassword.getText().toString().trim();

                //check the fields of the mail and password
                if (mail.isEmpty()) {
                    userMail.setError( "Mail is required" );
                    userMail.requestFocus();
                    btnLogin.setVisibility( View.VISIBLE );
                    loginProgress.setVisibility( View.GONE );
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher( mail ).matches()) {
                    userMail.setError( "Provide a valid Mail" );
                    userMail.requestFocus();
                    btnLogin.setVisibility( View.VISIBLE );
                    loginProgress.setVisibility( View.GONE );
                    return;
                }

                if (password.isEmpty()) {
                    userPassword.setError( "Password is required" );
                    userPassword.requestFocus();
                    btnLogin.setVisibility( View.VISIBLE );
                    loginProgress.setVisibility( View.GONE );
                    return;
                }

                if (password.length() < 6) {
                    userPassword.setError( "Password doesn't satisfy the length of 6 characters" );
                    userPassword.requestFocus();
                    btnLogin.setVisibility( View.VISIBLE );
                    loginProgress.setVisibility( View.GONE );
                    return;

                }

                signIn( mail, password );
            }
        } );
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            //user is already connected , need to redirect him to home page
            updateUI();
        }
    }

    private void signIn(String mail, String password) {
        mAuth.signInWithEmailAndPassword( mail, password ).addOnCompleteListener( new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //if the sign in task is done the user is signed in
                if (task.isSuccessful()) {
                    loginProgress.setVisibility( View.GONE );
                    btnLogin.setVisibility( View.VISIBLE );
                    updateUI();

                } else {
                    ToastShort ToastS = new ToastShort( task.getException().getMessage(), getApplicationContext() );
                    ToastS.showMessage();
                    btnLogin.setVisibility( View.VISIBLE );
                    loginProgress.setVisibility( View.GONE );
                }

            }
        } );

    }

    private void updateUI() {
        //move the user to the home activity
        startActivity( homeActivity );
        finish();
    }


}