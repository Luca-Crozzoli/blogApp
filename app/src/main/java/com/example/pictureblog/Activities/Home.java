package com.example.pictureblog.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.example.pictureblog.Activities.ui.home.HomeFragment;
import com.example.pictureblog.Activities.ui.profile.ProfileFragment;
import com.example.pictureblog.Helpers.GeoLocation;
import com.example.pictureblog.Models.Post;
import com.example.pictureblog.R;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
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

public class Home extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHome2Binding binding;

    //Add the instances for firebase DONE BY ME!!!!!!!!!!!!!!!!!!!!!!!
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    Dialog popAddPost; //creating a new dialog variable to consent the upload of the image

    //popup widgets references on popup_add_post.xml
    ImageView popupUserImage, popupPostImage, popupAddButton;
    TextView popupTitle, popupPlace, popupDescription;
    ProgressBar popupClickProgress;
    String latitudeLongitude;
    private static final int PreqCode = 2;
    private static final int REQUESCODE = 2;
    private Uri pickedImgUri = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        //DONE BY ME initialize firebase instances
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

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
                //here when image clicked we need to open the gallery
                //before open the gallery check if the app have acess to user file (like in the register activity)
                checkAndRequestForPermission();

            }
        } );
    }


    //Code used to check permission to access the gallery ot upload an image
    private void checkAndRequestForPermission() {
        //If the permission are not allowed!!
        if (ContextCompat.checkSelfPermission( Home.this, Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale( Home.this, Manifest.permission.READ_EXTERNAL_STORAGE )) {
                Toast.makeText( Home.this, "Please accept for required permission", Toast.LENGTH_LONG ).show();
            } else {
                ActivityCompat.requestPermissions( Home.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PreqCode );
            }
        } else {
            //we have all the permission to access user gallery
            openGallery();
        }

    }

    private void openGallery() {
        //TODO: openGallery intent and wait for user to pick a photo
        Intent galleryIntent = new Intent( Intent.ACTION_GET_CONTENT );
        galleryIntent.setType( "image/*" );
        startActivityForResult( galleryIntent, REQUESCODE ); //TODO deprecated method find an alternative!!

    }

    //When user picked an image...
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null) {
            //the user has successfully picked the image
            // we need to save its reference to a URI variable
            pickedImgUri = data.getData();
            popupPostImage.setImageURI( pickedImgUri );
        }
    }

    //method to initialize the pop up menu used to upload a post
    private void iniPopup() {

        popAddPost = new Dialog( this );
        popAddPost.setContentView( R.layout.popup_add_post ); //Update our layout
        popAddPost.getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) ); //transparent pop up
        popAddPost.getWindow().setLayout( Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT );
        popAddPost.getWindow().getAttributes().gravity = Gravity.TOP; //scroll down the window gravity on top

        //initialize popup widgets
        popupUserImage = popAddPost.findViewById( R.id.popup_user_image );
        popupPostImage = popAddPost.findViewById( R.id.popup_img );

        popupTitle = popAddPost.findViewById( R.id.popup_title );
        popupTitle.setFilters(new InputFilter[] {new InputFilter.LengthFilter(60)});

        popupDescription = popAddPost.findViewById( R.id.popup_description );
        popupDescription.setFilters(new InputFilter[] {new InputFilter.LengthFilter(60)});

        popupPlace = popAddPost.findViewById( R.id.et_place );
        popupPlace.setFilters(new InputFilter[] {new InputFilter.LengthFilter(60)});


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
                geoLocation.getAddress(postPlace,getApplicationContext(),new GeoHandler());



                final String postDescription = popupDescription.getText().toString();

                //test all input fields (Title and description) and post image
                if (postTitle.isEmpty()) {
                    popupTitle.setError( "Title is required" );
                    popupTitle.requestFocus();
                    popupAddButton.setVisibility( View.VISIBLE );
                    popupClickProgress.setVisibility( View.INVISIBLE );
                    return;
                }
                if (postPlace.isEmpty()) {
                    popupPlace.setError( "Place is required" );
                    popupPlace.requestFocus();
                    popupAddButton.setVisibility( View.VISIBLE );
                    popupClickProgress.setVisibility( View.INVISIBLE );
                    return;
                }
                if (postDescription.isEmpty()) {
                    popupDescription.setError( "Description is required" );
                    popupDescription.requestFocus();
                    popupAddButton.setVisibility( View.VISIBLE );
                    popupClickProgress.setVisibility( View.INVISIBLE );
                    return;
                }
                if (pickedImgUri == null) {
                    showMessage( "You need to upload an image for the post!" );
                    popupPostImage.requestFocus();
                    popupAddButton.setVisibility( View.VISIBLE );
                    popupClickProgress.setVisibility( View.INVISIBLE );
                    return;
                }

                //If everything was okay
                //TODO create a Post object and save it in the real time database in Firebase
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


                                Post post = new Post( postTitle, postDescription, imageDownloadLink, currentUser.getUid(), currentUser.getPhotoUrl().toString(), latitudeLongitude );
                                //add the post to the database
                                addPost( post );

                            }
                        } ).addOnFailureListener( new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //something goes wrong uploading the post
                                showMessage( e.getMessage() );
                                popupClickProgress.setVisibility( View.INVISIBLE );
                                popupAddButton.setVisibility( View.VISIBLE );
                            }
                        } );
                    }
                } );
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
                showMessage( "Post added correctly!" );
                popupClickProgress.setVisibility( View.INVISIBLE );
                popupAddButton.setVisibility( View.VISIBLE );
                popAddPost.dismiss();
            }
        } );
    }

    private void showMessage(String message) {
        Toast.makeText( getApplicationContext(), message, Toast.LENGTH_LONG ).show();
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
        if (menuItem.getItemId() == R.id.action_settings) {
            FirebaseAuth.getInstance().signOut();
            Intent loginActivity = new Intent( getApplicationContext(), LoginActivity.class );
            startActivity( loginActivity );
            finish();
            return true;
        }
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

    //DONE BY ME!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
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
            if(msg.what == 1){
                Bundle bundle = msg.getData();
                address = bundle.getString( "Address" );
            }else{
                address = null;
            }
           latitudeLongitude = address;
        }
    }

    /*
    //TODO remember when uncomment ro let the class implement the following
    implements NavigationView.OnNavigationItemSelectedListener
    @SuppressWarnings( "StatementWHitEmptyBody" )
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.nav_home){
            getSupportActionBar().setTitle( "Home" );
            getSupportFragmentManager().beginTransaction().replace( R.id.nav_host_fragment_content_home, new HomeFragment() ).commit();
        }else if ( id == R.id.nav_profile){
            getSupportActionBar().setTitle( "Profile" );
            getSupportFragmentManager().beginTransaction().replace( R.id.nav_host_fragment_content_home, new ProfileFragment() ).commit();
        }else if(id == R.id.nav_settings){
            getSupportActionBar().setTitle( "Settings" );
            getSupportFragmentManager().beginTransaction().replace( R.id.nav_host_fragment_content_home, new SettingsFragment() ).commit();
        }else  if(id == R.id.nav_logout){
            FirebaseAuth.getInstance().signOut();
            Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity( loginActivity );
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        drawer.closeDrawer( GravityCompat.START );
        return true;
    }*/
}