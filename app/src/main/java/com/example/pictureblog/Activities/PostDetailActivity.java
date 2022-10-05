package com.example.pictureblog.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pictureblog.Models.Comment;
import com.example.pictureblog.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    ImageView imgPost, imgUserPost, imgCurrentUser;
    TextView txtPostDesc, txtPostDateName, txtPostTitle;
    EditText editTextComment;
    String PostKey;

    Button btnAddComment;

    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;

    FirebaseDatabase firebaseDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_post_detail );
        //transparent status bar TODO is it necessary?
        Window w = getWindow();
        w.setFlags( WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS );


        //we are hiding the Picture blog app bar
        getSupportActionBar().hide();

        //initialization of the views

        imgPost = findViewById( R.id.post_detail_img );
        imgUserPost = findViewById( R.id.post_detail_user_img );
        imgCurrentUser = findViewById( R.id.post_detail_currentuser_img );

        txtPostTitle = findViewById( R.id.post_detail_title );
        txtPostDesc = findViewById( R.id.post_detail_desc );
        txtPostDateName = findViewById( R.id.post_detail_date_name );

        editTextComment = findViewById( R.id.post_detail_comment );
        btnAddComment = findViewById( R.id.post_detail_add_comment_btn );


        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

        //Add comment button on click listener
        btnAddComment.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //after a click on the add button the button must be invisible
                btnAddComment.setVisibility( View.INVISIBLE);

                DatabaseReference commentReference = firebaseDatabase.getReference( "comment" ).child( PostKey ).push();
                String comment_content = editTextComment.getText().toString();
                String uid = currentUser.getUid();
                String uname = currentUser.getDisplayName();
                String uimg = currentUser.getPhotoUrl().toString();

                //Create a new instance of a comment object
                Comment comment = new Comment(comment_content,uid,uimg,uname);

                commentReference.setValue( comment ).addOnSuccessListener( new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        showMessage("comment Added ");
                        editTextComment.setText( "" );
                        btnAddComment.setVisibility( View.VISIBLE );
                    }
                } ).addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showMessage("Fail to add the comment"+ e.getMessage());
                    }
                } );


            }
        } );


        //binding the data into the views
        // first get the post data  and then display it
        //send post detail data to this activity first...
        //get post data

        String postImage = getIntent().getExtras().getString( "postImage" ); //LOOK at the put extra in post adapter used to pass the parameter names
        Glide.with( this ).load( postImage ).into( imgPost );

        String postTitle = getIntent().getExtras().getString( "title" );
        txtPostTitle.setText( postTitle );

        String userPostImage = getIntent().getExtras().getString( "userPhoto" );
        Glide.with( this ).load( userPostImage ).into( imgUserPost );

        String postDescription = getIntent().getExtras().getString( "description" );
        txtPostDesc.setText( postDescription );

        //retrieve the image of the current user which wants to add the comment
        Glide.with( this ).load( currentUser.getPhotoUrl() ).into( imgCurrentUser );

        //retrieve the post id
        PostKey = getIntent().getExtras().getString( "postKey" );

        //retrieve the date of the post
        String date = timeStampToString( getIntent().getExtras().getLong( "postDate" ) );
        txtPostDateName.setText( date );


    }

    private void showMessage(String message) {
        Toast.makeText( this,message, Toast.LENGTH_LONG ).show();

    }

    private String timeStampToString(long time) {
        Calendar calendar = Calendar.getInstance( Locale.ITALIAN );
        calendar.setTimeInMillis( time );
        SimpleDateFormat dataFormat = new SimpleDateFormat( "dd-MM-YYYY" );
        String date = dataFormat.format( calendar.getTime() );
        return date;

    }
}