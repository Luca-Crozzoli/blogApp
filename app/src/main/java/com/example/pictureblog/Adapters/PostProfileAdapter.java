package com.example.pictureblog.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pictureblog.Models.Post;
import com.example.pictureblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class PostProfileAdapter extends RecyclerView.Adapter<PostProfileAdapter.MyViewHolder> {

    DatabaseReference databaseReference;

    Context mContext;
    List<Post> mData;
    Post emptyPost;

    public PostProfileAdapter(Context mContext, List<Post> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //When we create the view holder we are inflating the row_post_item_layout provided in layouts
        View row = LayoutInflater.from(mContext).inflate( R.layout.row_post_profile , parent,false); // the row_post_item.xml

        return new MyViewHolder( row );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //update the holder with the appropriate values: the title and the image of the post
        holder.tvTitle.setText( mData.get(position).getTitle() );
        Glide.with(mContext).load( mData.get( position ).getPicture() ).into(holder.imgPost);

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tvTitle;
        ImageView imgPost;
        Button deleteButton;

        public MyViewHolder(@NonNull View itemView) {
            super( itemView );

            //initialize the text views and image views
            tvTitle = itemView.findViewById( R.id.row_post_title_profile );
            imgPost = itemView.findViewById( R.id.row_post_img_profile );
            deleteButton = itemView.findViewById( R.id.row_post_delete_button );



            deleteButton.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    String postKey = mData.get( position).getPostKey();
                    deletePost(postKey);

                    Toast.makeText( mContext.getApplicationContext(), "delete clicked", Toast.LENGTH_SHORT ).show();

                }
            } );

        }
    }

    public PostProfileAdapter(Context mContext, Post emptyPost) {
        this.mContext = mContext;
        this.emptyPost = emptyPost;
    }

    private void deletePost(String postKey) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        databaseReference.child( postKey ).removeValue().addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText( mContext, "Cancellation done properly", Toast.LENGTH_SHORT ).show();
                }else{
                    Toast.makeText( mContext, "Fail to delete", Toast.LENGTH_SHORT ).show();
                }
            }
        } );


        databaseReference = FirebaseDatabase.getInstance().getReference("comment");
        databaseReference.child( postKey ).removeValue().addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText( mContext, "Cancellation done properly", Toast.LENGTH_SHORT ).show();
                }else{
                    Toast.makeText( mContext, "Fail to delete", Toast.LENGTH_SHORT ).show();
                }
            }
        } );
    }
}
