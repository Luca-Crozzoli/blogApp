package com.example.pictureblog.Activities.ui.profile;

import static com.example.pictureblog.R.id.postRV_profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pictureblog.Adapters.PostAdapter;
import com.example.pictureblog.Adapters.PostProfileAdapter;
import com.example.pictureblog.Models.Post;
import com.example.pictureblog.R;
import com.example.pictureblog.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    RecyclerView postRecyclerView;
    PostProfileAdapter postProfileAdapter;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    List<Post> postList;

    //adding the current user dependencies
    FirebaseUser currentUser;
    String userId;

    @SuppressLint("MissingInflatedId")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate( R.layout.fragment_profile, container, false );
        postRecyclerView = fragmentView.findViewById( postRV_profile );
        postRecyclerView.setLayoutManager( new LinearLayoutManager( getActivity() ) );
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference( "Posts" );
        return fragmentView;

    }

    @Override
    public void onStart() {
        super.onStart();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentUser.getUid();
        //get List posts from the database only the ones where the user corresponds to the one logged in
        databaseReference.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList = new ArrayList<>();
                for (DataSnapshot postsnap : snapshot.getChildren()) {
                    Post post = postsnap.getValue( Post.class );
                    if (post.getUserId().equals( userId )) {
                        postList.add( post );
                    }
                }

                if (postList.isEmpty()) {
                    Toast.makeText( getContext(), "NO POSTS UPDATED", Toast.LENGTH_SHORT ).show();
                }
                postProfileAdapter = new PostProfileAdapter( getActivity(), postList );
                postRecyclerView.setAdapter( postProfileAdapter );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {


            }
        } );
    }

    //CAN BE REMOVED?
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach( context );
    }

    //CAN BE REMOVED?
    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //CAN BE REMOVED?
    public interface OnFragmentInteractionListener {
        //TODO update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}