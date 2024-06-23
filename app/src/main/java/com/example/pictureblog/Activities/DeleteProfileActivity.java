package com.example.pictureblog.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pictureblog.Entities.Post;
import com.example.pictureblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class DeleteProfileActivity extends AppCompatActivity {
    private FirebaseUser currentUser;
    private FirebaseStorage firebaseStorage;
    private DatabaseReference databaseReference;
    private String currentUserId;
    //popup widgets references on popup_add_post.xml
    private Button deleteButton;
    private EditText confirmPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_delete );

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        deleteButton = findViewById( R.id.delete_button );
        confirmPassword = findViewById( R.id.confirm_password_delete );
        currentUserId = currentUser.getUid();

        deleteButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = confirmPassword.getText().toString().trim();

                if (password.isEmpty()) {
                    confirmPassword.setError( "Please insert the password to delete your account" );
                    confirmPassword.requestFocus();
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential( currentUser.getEmail(), password );

                //user re-authentication to delete the profile
                currentUser.reauthenticate( credential ).addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        confirmPassword.setText( "" );
                        Toast.makeText( getApplicationContext(), "User re-Authenticate correctly", Toast.LENGTH_SHORT ).show();

                        deleteUserAndPosts( currentUserId );
                    }
                } );

            }
        } );

    }

    @Override
    public void onBackPressed() {

        Intent homeActivity = new Intent( getApplicationContext(), HomeActivity.class );
        startActivity( homeActivity );
        finish();
    }

    private void deleteUserAndPosts(String currentUserId) {
        ArrayList<Post> postList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference( "Posts" );
        databaseReference.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot postsnap : snapshot.getChildren()) {
                    Post post = postsnap.getValue( Post.class );
                    if (post.getUserId().equals( currentUserId )) {
                        postList.add( post );
                    }
                }

                for (int i = 0; i < postList.size(); i++) {
                    String postKey = postList.get( i ).getPostKey();
                    String imgUrl = postList.get( i ).getPicture();

                    deletePost( postKey, imgUrl );
                }

                String userProfileImage = currentUser.getPhotoUrl().toString();
                deleteUserProfileImage( userProfileImage );
                currentUser.delete().addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText( getApplicationContext(), "Deletion of profile", Toast.LENGTH_SHORT ).show();
                            Intent intent = new Intent( DeleteProfileActivity.this, LoginActivity.class );
                            startActivity( intent );
                            finish();

                        } else {

                        }
                    }
                } );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private void deleteUserProfileImage(String userProfileImage) {
        if (userProfileImage.contains( "userphoto" )) {
            return;
        } else {
            firebaseStorage = FirebaseStorage.getInstance();
            StorageReference imgReference = firebaseStorage.getReferenceFromUrl( userProfileImage );
            imgReference.delete().addOnSuccessListener( new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText( DeleteProfileActivity.this, "User removed correctly", Toast.LENGTH_SHORT ).show();
                }
            } );
        }

    }

    private void deletePost(String postKey, String imgUrl) {
        firebaseStorage = FirebaseStorage.getInstance();
        StorageReference imgReference = firebaseStorage.getReferenceFromUrl( imgUrl );
        imgReference.delete().addOnSuccessListener( new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //if removed successfully from the storage then we need to remove from the database
                databaseReference = FirebaseDatabase.getInstance().getReference( "Posts" );
                databaseReference.child( postKey ).removeValue().addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText( getApplicationContext(), "Cancellation of posts done", Toast.LENGTH_SHORT ).show();
                        } else {
                            Toast.makeText( getApplicationContext(), "Fail to delete", Toast.LENGTH_SHORT ).show();

                        }
                    }
                } );

                //also we need to remove the corresponding comments related to that image
                databaseReference = FirebaseDatabase.getInstance().getReference( "Comments" );
                databaseReference.child( postKey ).removeValue().addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText( getApplicationContext(), "Cancellation of related comments done", Toast.LENGTH_SHORT ).show();
                        } else {
                            Toast.makeText( getApplicationContext(), "Fail to delete", Toast.LENGTH_SHORT ).show();
                        }
                    }
                } );

            }
        } );


    }
}