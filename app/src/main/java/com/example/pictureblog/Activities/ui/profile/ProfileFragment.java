package com.example.pictureblog.Activities.ui.profile;

import static com.example.pictureblog.R.id.postRV_profile;

import android.annotation.SuppressLint;
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
    PostAdapter postAdapter;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    List<Post> postList;

    //adding the currentuser dependencies
    FirebaseUser currentUser;
    String userId;

    @SuppressLint("MissingInflatedId")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate( R.layout.fragment_profile,container,false );
        postRecyclerView = fragmentView.findViewById( postRV_profile );
        postRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()) );
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Posts");
        //databaseReference = firebaseDatabase.getReference("Posts").child( userId );
        return  fragmentView;

        /*HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

    binding = FragmentHomeBinding.inflate(inflater, container, false);
    View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;*/
    }

    @Override
    public void onStart() {
        super.onStart();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId =currentUser.getUid();
        //get List posts from the database
        databaseReference.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList = new ArrayList<>();
                for(DataSnapshot postsnap: snapshot.getChildren()){
                    Post post = postsnap.getValue(Post.class);
                    String id_userPost = post.getUserId();
                    if (post.getUserId().equals( userId )){
                        postList.add(post);
                   }
                }

                postAdapter = new PostAdapter( getActivity(),postList );
                postRecyclerView.setAdapter( postAdapter );
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
    public interface OnFragmentInteractionListener{
        //TODO update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}