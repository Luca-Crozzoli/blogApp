package com.example.pictureblog.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.pictureblog.Activities.ui.home.HomeFragment;
import com.example.pictureblog.Activities.ui.profile.ProfileFragment;
import com.example.pictureblog.Activities.ui.settings.SettingsFragment;
import com.example.pictureblog.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.app.TaskStackBuilder;
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

public class Home extends AppCompatActivity  {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHome2Binding binding;

    //Add the instances for firebase DONE BY ME!!!!!!!!!!!!!!!!!!!!!!!
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    Dialog popAddPost; //creating a new dialog variable to consent the upload of the image

    //popup widgets references on popup_add_post.xml
    ImageView popupUserImage,popupPostImage,popupAddButton;
    TextView popupTitle, popupDescription;
    ProgressBar popupClickProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        //DONE BY ME initialize firebase instances
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //DONE BY ME initialize popup
        iniPopup();

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
                R.id.nav_home, R.id.nav_profile, R.id.nav_settings) //TODO add eventually the R.id.nav_logout
                .setOpenableLayout( drawer )
                .build();
        NavController navController = Navigation.findNavController( this, R.id.nav_host_fragment_content_home );
        NavigationUI.setupActionBarWithNavController( this, navController, mAppBarConfiguration );
        NavigationUI.setupWithNavController( navigationView, navController );

        //DONE BY ME, after all those things onCreate we update the headerNavigation bar
        updateNavHeader();
    }

    //method to initialize the pop up menu used to upload a post
    private void iniPopup() {

        popAddPost = new Dialog( this );
        popAddPost.setContentView( R.layout.popup_add_post ); //Update our layout
        popAddPost.getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) ); //transparent pop up
        popAddPost.getWindow().setLayout( Toolbar.LayoutParams.MATCH_PARENT,Toolbar.LayoutParams.WRAP_CONTENT );
        popAddPost.getWindow().getAttributes().gravity = Gravity.TOP; //scroll down the window gravity on top

        //initialize popup widgets
        popupUserImage = popAddPost.findViewById( R.id.popup_user_image );
        popupPostImage = popAddPost.findViewById( R.id.popup_img );

        popupTitle = popAddPost.findViewById( R.id.popup_title );
        popupDescription = popAddPost.findViewById( R.id.popup_description );

        popupAddButton = popAddPost.findViewById( R.id.popup_add );
        popupClickProgress = popAddPost.findViewById( R.id.popup_progressBar );

        //load current user logged in image using Glide
        Glide.with(Home.this).load( currentUser.getPhotoUrl() ).into( popupUserImage );

        // add post click listener on the button
        popupAddButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupAddButton.setVisibility( View.INVISIBLE );
                popupClickProgress.setVisibility( View.VISIBLE );
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
    public boolean onOptionsItemSelected(MenuItem menuItem){
        if(menuItem.getItemId() == R.id.action_settings){
            FirebaseAuth.getInstance().signOut();
            Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity( loginActivity );
            finish();
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
        Glide.with( this).load( currentUser.getPhotoUrl() ).into(navUserPhoto);
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