package com.example.pictureblog.Activities;

import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.pictureblog.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pictureblog.databinding.ActivityHome2Binding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHome2Binding binding;

    //Add the instances for firebase DONE BY ME!!!!!!!!!!!!!!!!!!!!!!!
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        //DONE BY ME
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        binding = ActivityHome2Binding.inflate( getLayoutInflater() );
        setContentView( binding.getRoot() );


        setSupportActionBar( binding.appBarHome.toolbar );
        binding.appBarHome.fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make( view, "Replace with your own action", Snackbar.LENGTH_LONG )
                        .setAction( "Action", null ).show();
            }
        } );
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow ) //TODO add eventually the R.id.nav_logout
                .setOpenableLayout( drawer )
                .build();
        NavController navController = Navigation.findNavController( this, R.id.nav_host_fragment_content_home );
        NavigationUI.setupActionBarWithNavController( this, navController, mAppBarConfiguration );
        NavigationUI.setupWithNavController( navigationView, navController );

        //DONE BY ME, after all those things onCreate we update the headerNavigazion bar
        updateNavHeader();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.home, menu );
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController( this, R.id.nav_host_fragment_content_home );
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
}