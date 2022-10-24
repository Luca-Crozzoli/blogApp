package com.example.pictureblog.Activities.ui.home;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pictureblog.Adapters.PostAdapter;
import com.example.pictureblog.Models.Post;
import com.example.pictureblog.R;
import com.example.pictureblog.databinding.FragmentHomeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    RecyclerView postRecyclerView;
    PostAdapter postAdapter;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    List<Post> postList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate( R.layout.fragment_home, container, false );
        postRecyclerView = fragmentView.findViewById( R.id.postRV );
        postRecyclerView.setLayoutManager( new LinearLayoutManager( getActivity() ) );
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference( "Posts" );
        return fragmentView;

    }

    @Override
    public void onStart() {
        super.onStart();

        //get List posts from the database
        databaseReference.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList = new ArrayList<>();
                for (DataSnapshot postsnap : snapshot.getChildren()) {
                    Post post = postsnap.getValue( Post.class );
                    postList.add( post );
                }

                postAdapter = new PostAdapter( getActivity(), postList );
                postRecyclerView.setAdapter( postAdapter );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}