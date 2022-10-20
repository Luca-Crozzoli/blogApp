package com.example.pictureblog.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.pictureblog.Helpers.GeoLocation;
import com.example.pictureblog.Helpers.ToastShort;
import com.example.pictureblog.Models.Post;
import com.example.pictureblog.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pictureblog.databinding.ActivityHome2Binding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Home extends AppCompatActivity {



    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHome2Binding binding;

    //Add the instances for firebase DONE BY ME!!!!!!!!!!!!!!!!!!!!!!!
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Dialog popAddPost; //creating a new dialog variable to consent the upload of the image

    //popup widgets references on popup_add_post.xml
    private ImageView popupUserImage, popupPostImage, popupMapIcon,popupAddButton;
    private TextView popupTitle, popupPlace, popupDescription;
    private ProgressBar popupClickProgress;
    private String latitudeLongitude;
    private Uri pickedImgUri = null;
    FusedLocationProviderClient fusedLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        //DONE BY ME initialize firebase instances
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient( Home.this );

        //DONE BY ME initialize popup
        iniPopup();
        setupPopupImageClick();

        binding = ActivityHome2Binding.inflate( getLayoutInflater() );
        setContentView( binding.getRoot() );


        //Here we have our floating action Button
        setSupportActionBar( binding.appBarHome.toolbar );
        binding.appBarHome.fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popAddPost.show();

            }
        } );

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile )
                .setOpenableLayout( drawer )
                .build();
        NavController navController = Navigation.findNavController( this, R.id.nav_host_fragment_content_home );
        NavigationUI.setupActionBarWithNavController( this, navController, mAppBarConfiguration );
        NavigationUI.setupWithNavController( navigationView, navController );

        //DONE BY ME, after all those things onCreate we update the headerNavigation bar
        updateNavHeader();
    }

    //here we take the reference of the image view used in the pop_up_add_post Layout
    private void setupPopupImageClick() {

        popupPostImage.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.Companion.with( Home.this )
                        /*.crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)*/
                        .start();
            }
        } );
    }

    //When user picked an image...
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        pickedImgUri = data.getData();
        popupPostImage.setImageURI( pickedImgUri );
    }

    //method to initialize the pop up menu used to upload a post
    private void iniPopup() {
        //check the permission for the localization when we want to update a post
        /*if (ActivityCompat.checkSelfPermission( Home.this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED){
        }else {
            ActivityCompat.requestPermissions( Home.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44 );
        }*/

        popAddPost = new Dialog( this );
        popAddPost.setContentView( R.layout.popup_add_post ); //Update our layout
        popAddPost.getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) ); //transparent pop up
        popAddPost.getWindow().setLayout( Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT );
        popAddPost.getWindow().getAttributes().gravity = Gravity.TOP; //scroll down the window gravity on top

        //initialize popup widgets
        popupUserImage = popAddPost.findViewById( R.id.popup_user_image );
        popupMapIcon = popAddPost.findViewById( R.id.map_icon_get_current_location );
        popupMapIcon.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission( Home.this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED){
                    showLocation();
                }else {
                    ActivityCompat.requestPermissions( Home.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44 );
                    //Toast.makeText( Home.this, "Permissions added, click again to provide location", Toast.LENGTH_SHORT ).show();
                }
            }
        } );
        popupPostImage = popAddPost.findViewById( R.id.popup_img );

        popupTitle = popAddPost.findViewById( R.id.popup_title );
        //popupTitle.setFilters( new InputFilter[]{new InputFilter.LengthFilter( 60 )} );

        popupDescription = popAddPost.findViewById( R.id.popup_description );
        //popupDescription.setFilters( new InputFilter[]{new InputFilter.LengthFilter( 60 )} );

        popupPlace = popAddPost.findViewById( R.id.et_place );
        //popupPlace.setFilters( new InputFilter[]{new InputFilter.LengthFilter( 60 )} );


        popupAddButton = popAddPost.findViewById( R.id.popup_add );
        popupClickProgress = popAddPost.findViewById( R.id.popup_progressBar );

        //load current user logged in image using Glide
        Glide.with( Home.this ).load( currentUser.getPhotoUrl() ).into( popupUserImage );

        // add post click listener on the button
        popupAddButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupAddButton.setVisibility( View.INVISIBLE );
                popupClickProgress.setVisibility( View.VISIBLE );


                final String postTitle = popupTitle.getText().toString();

                final String postPlace = popupPlace.getText().toString();

                GeoLocation geoLocation = new GeoLocation();
                geoLocation.getAddress( postPlace, getApplicationContext(), new GeoHandler() );


                final String postDescription = popupDescription.getText().toString();

                //test all input fields (Title and description) and post image
                if (postTitle.isEmpty()) {
                    popupTitle.setError( "Title is required" );
                    popupTitle.requestFocus();
                    popupAddButton.setVisibility( View.VISIBLE );
                    popupClickProgress.setVisibility( View.GONE );
                    return;
                }
                if (postPlace.isEmpty()) {
                    popupPlace.setError( "Place is required" );
                    popupPlace.requestFocus();
                    popupAddButton.setVisibility( View.VISIBLE );
                    popupClickProgress.setVisibility( View.GONE );
                    return;
                }
                if (postDescription.isEmpty()) {
                    popupDescription.setError( "Description is required" );
                    popupDescription.requestFocus();
                    popupAddButton.setVisibility( View.VISIBLE );
                    popupClickProgress.setVisibility( View.GONE );
                    return;
                }
                if (pickedImgUri == null) {
                    ToastShort ToastS = new ToastShort( "You need to upload an Image for the post", getApplicationContext() );
                    ToastS.showMessage();
                    popupPostImage.requestFocus();
                    popupAddButton.setVisibility( View.VISIBLE );
                    popupClickProgress.setVisibility( View.GONE );
                    return;
                }

                //If everything was okay
                //create a Post object and save it in the real time database in Firebase
                //first upload post image to firebase storage
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child( "blog_images" );
                StorageReference imageFilePath = storageReference.child( pickedImgUri.getLastPathSegment() );
                imageFilePath.putFile( pickedImgUri ).addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageFilePath.getDownloadUrl().addOnSuccessListener( new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageDownloadLink = uri.toString();
                                // create post Object here after upload the image in firebase storage successfully


                                Post post = new Post( postTitle, postDescription, imageDownloadLink, currentUser.getUid(), currentUser.getPhotoUrl().toString(), latitudeLongitude,postPlace,currentUser.getDisplayName() );
                                //add the post to the database
                                addPost( post );

                            }
                        } ).addOnFailureListener( new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //something goes wrong uploading the post
                                ToastShort ToastS = new ToastShort( e.getMessage(), getApplicationContext() );
                                ToastS.showMessage();
                                popupClickProgress.setVisibility( View.GONE );
                                popupAddButton.setVisibility( View.VISIBLE );
                            }
                        } );
                    }
                } );
            }
        } );


    }

    @SuppressLint("MissingPermission")
    private void showLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener( new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if(location != null){
                    Geocoder geocoder = new Geocoder( Home.this, Locale.getDefault() );
                    try {
                        List<Address> AddressList = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1  );
                        popupPlace.setText(AddressList.get( 0 ).getAddressLine( 0 ) );
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }else {
                    Toast.makeText( Home.this, "Tap again the map icon", Toast.LENGTH_SHORT ).show();
                }

            }
        } );

    }

    private void addPost(Post post) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference( "Posts" ).push();

        // get post unique ID and update post key
        String key = myRef.getKey();
        post.setPostKey( key );

        // add post data to firebase database
        myRef.setValue( post ).addOnSuccessListener( new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                ToastShort ToastS = new ToastShort( "Post added correctly!", getApplicationContext() );
                ToastS.showMessage();
                popupTitle.setText( "" );
                popupPlace.setText( "" );
                popupDescription.setText( "" );
                popupPostImage.setImageURI( null );
                popupClickProgress.setVisibility( View.GONE );
                popupAddButton.setVisibility( View.VISIBLE );
                popAddPost.dismiss();
            }
        } );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.home, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        //Logout of the user
        if (menuItem.getItemId() == R.id.logout_settings) {
            FirebaseAuth.getInstance().signOut();
            Intent loginActivity = new Intent( getApplicationContext(), LoginActivity.class );
            startActivity( loginActivity );
            finish();
            return true;
        }
        //Deletion of the user
        if (menuItem.getItemId() == R.id.delete_settings) {
            Intent deleteActivity = new Intent( getApplicationContext(), DeleteActivity.class );
            startActivity( deleteActivity );
            finish();
            /*Intent loginActivity = new Intent( getApplicationContext(), LoginActivity.class );
            startActivity( loginActivity );
            finish();*/
            return true;

        }
        return super.onOptionsItemSelected( menuItem );
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController( this, R.id.nav_host_fragment_content_home ); //CONTAINED IN CONTENT HOME XML
        return NavigationUI.navigateUp( navController, mAppBarConfiguration )
                || super.onSupportNavigateUp();
    }

    public void updateNavHeader() {
        //we are retrieving the navigation view
        NavigationView navigationView = findViewById( R.id.nav_view );
        //from the nav view we get the header nav view
        View headerView = navigationView.getHeaderView( 0 );
        //then we upload the headerView fields with the data obtained from the current user
        ImageView navUserPhoto = headerView.findViewById( R.id.nav_user_photo );
        TextView navUserName = headerView.findViewById( R.id.nav_username );
        TextView navUserMail = headerView.findViewById( R.id.nav_user_mail );

        //set the corresponding text view with the data of the current user
        navUserName.setText( currentUser.getDisplayName() );
        navUserMail.setText( currentUser.getEmail() );

        //Now we use Glide to upload the image of the user
        Glide.with( this ).load( currentUser.getPhotoUrl() ).into( navUserPhoto );

    }

    private class GeoHandler extends Handler {

        @Override
        public void handleMessage(@NonNull Message msg) {
            String address;
            //check if the code is 1 . this means the address in the message is processed correctly ****
            if (msg.what == 1) {
                Bundle bundle = msg.getData();
                address = bundle.getString( "Address" );
            } else {
                address = null;
            }
            latitudeLongitude = address;
        }
    }
}