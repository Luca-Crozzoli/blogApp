package com.example.pictureblog.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pictureblog.Adapters.CommentAdapter;
import com.example.pictureblog.Models.Comment;
import com.example.pictureblog.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;

import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    ImageView imgPost, imgUserPost, imgCurrentUser;
    TextView txtPostDesc, txtPostDateName, txtPostTitle;
    MapView post_map;
    EditText editTextComment;
    String PostKey;

    Button btnAddComment;

    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;

    FirebaseDatabase firebaseDatabase;

    //new recycler view for holding the comments
    RecyclerView rVComment;
    // used to adapt the content of the comments
    CommentAdapter commentAdapter;
    List<Comment> listComment;
    static String COMMENT_KEY = "comment";
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_post_detail );

        /*OSMODROID*/
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));


        //transparent status bar TODO is it necessary?
        Window w = getWindow();
        w.setFlags( WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS );


        //we are hiding the Picture blog app bar
        getSupportActionBar().hide();

        //initialization of the views

        rVComment = findViewById( R.id.rv_comments );
        imgPost = findViewById( R.id.post_detail_img );
        imgUserPost = findViewById( R.id.post_detail_user_img );
        imgCurrentUser = findViewById( R.id.post_detail_currentuser_img );

        txtPostTitle = findViewById( R.id.post_detail_title );

        txtPostDesc = findViewById( R.id.post_detail_desc );

        txtPostDateName = findViewById( R.id.post_detail_date_name );

        editTextComment = findViewById( R.id.post_detail_comment );
        editTextComment.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editTextComment, InputMethodManager.SHOW_FORCED);

        /*imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
        imm.showSoftInput(editTextComment,InputMethodManager.SHOW_FORCED);*/

        btnAddComment = findViewById( R.id.post_detail_add_comment_btn );

        post_map = (MapView) findViewById( R.id.post_detail_map );
        post_map.setTileSource( TileSourceFactory.MAPNIK );

        String[] permessi = { "Manifest.permission.ACCESS_FINE_LOCATION"," Manifest.permission.WRITE_EXTERNAL_STORAGE"};
        requestPermissionsIfNecessary( permessi);


        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

        editTextComment.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextComment.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editTextComment, InputMethodManager.SHOW_FORCED);

                /*imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
                imm.showSoftInput(editTextComment,InputMethodManager.SHOW_FORCED);*/
            }
        } );


        //Add comment button on click listener
        btnAddComment.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //after a click on the add button the button must be invisible
                btnAddComment.setVisibility( View.INVISIBLE);

                DatabaseReference commentReference = firebaseDatabase.getReference( COMMENT_KEY ).child( PostKey ).push();
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

        String postLocation = getIntent().getExtras().getString( "postLocation" );
        String [] coordinates = postLocation.split("\n");
        // https://stackoverflow.com/questions/11873573/converting-a-string-to-an-int-for-an-android-geopoint
        Double latitude = Double.parseDouble(coordinates[0]);
        Double longitude = Double.parseDouble(coordinates[1]);

        //TODO ADD THE LATITUDE AND LONGITUDE RETRIEVE IT FROM THE DATABASE FIELD
        IMapController mapController = post_map.getController();
        mapController.setZoom(9.5);
        GeoPoint startPoint = new GeoPoint(latitude,longitude);
        mapController.setCenter(startPoint);


        //Adding the marker for the location where we took the picture
        Marker markerLocation = new Marker( post_map );
        markerLocation.setPosition( startPoint );


        Drawable myTooltip = getDrawable( R.drawable.marker_map );
        markerLocation.setIcon( myTooltip );
        markerLocation.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        post_map.getOverlays().add(markerLocation);

        /*Marker startMarker = new Marker(post_map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        post_map.getOverlays().add(startMarker);*/


        //initialize Recyclerview of the comments
        iniRvComment();


    }


    //retrieve all the comments associated to the post and display it using the comment adapter
    private void iniRvComment() {

        rVComment.setLayoutManager( new LinearLayoutManager( this ) );

        DatabaseReference commentRef = firebaseDatabase.getReference(COMMENT_KEY).child(PostKey);
        commentRef.addValueEventListener( new ValueEventListener() {

            //https://stackoverflow.com/questions/61703700/when-to-use-datasnapshot-getchildren
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listComment = new ArrayList<>();
                for(DataSnapshot snap : snapshot.getChildren()){
                    Comment comment = snap.getValue(Comment.class);
                    listComment.add( comment );
                }
                commentAdapter = new CommentAdapter( getApplicationContext(),listComment );
                rVComment.setAdapter( commentAdapter );

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
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

    /*OSMODROID*/
    @Override
    protected void onResume() {
        super.onResume();
        post_map.onResume();
    }
    /*OSMODRODI*/
    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        post_map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    /*OSMODROID*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add( permissions[i] );
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray( new String[0] ),
                    REQUEST_PERMISSIONS_REQUEST_CODE );
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
}