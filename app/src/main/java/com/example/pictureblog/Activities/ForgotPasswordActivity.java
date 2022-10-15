package com.example.pictureblog.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.pictureblog.Helpers.ToastShort;
import com.example.pictureblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordButton;
    private ProgressBar progressBar;

    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_forgot_password );

        emailEditText = (EditText)findViewById( R.id.reset_password_mail );
        resetPasswordButton = (Button)findViewById(R.id.reset_button );
        progressBar = (ProgressBar)findViewById(R.id.reset_progress );

        auth = FirebaseAuth.getInstance();

        //Add an event listern on the clikc of the reset password button
        resetPasswordButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        } );
    }

    private void resetPassword(){
        String email = emailEditText.getText().toString().trim();

        if(email.isEmpty()){
            emailEditText.setError( "Email is requested!" );
            emailEditText.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher( email ).matches()){
            emailEditText.setError( "Please provide a valid email!" );
            emailEditText.requestFocus();
            return;
        }

        progressBar.setVisibility( View.VISIBLE );
        auth.sendPasswordResetEmail( email ).addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //If the task went good everything is okay, if not we only inform the user with a toast
                if(task.isSuccessful()){
                    ToastShort toastS = new ToastShort("Check your email to reset the password!",ForgotPasswordActivity.this);
                    toastS.showMessage();
                    progressBar.setVisibility( View.GONE );
                    Intent loginActivity = new Intent( getApplicationContext(), LoginActivity.class );
                    startActivity( loginActivity );
                    finish();
                }else{
                    ToastShort toastS = new ToastShort("Try again!! something wrong happened be sure you are already registered!",ForgotPasswordActivity.this);
                    toastS.showMessage();
                    progressBar.setVisibility( View.GONE );
                }
            }
        } );

    }
}