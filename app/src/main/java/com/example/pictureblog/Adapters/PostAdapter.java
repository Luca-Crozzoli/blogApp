package com.example.pictureblog.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pictureblog.Models.Post;
import com.example.pictureblog.R;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {

    Context mContext;
    List<Post> mData;

    public PostAdapter(Context mContext, List<Post> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //When we create the view holder we are inflating the row_post_item_layout provided in layouts
        View row = LayoutInflater.from(mContext).inflate( R.layout.row_post_item , parent,false);

        return new MyViewHolder( row );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //update the holder with the appropriate values: post image and title of the post and user profile
        // we are calling the methods defined in the post class
        holder.tvTitle.setText( mData.get(position).getTitle() );
        Glide.with(mContext).load( mData.get( position ).getPicture() ).into(holder.imgPost);
        Glide.with(mContext).load( mData.get( position ).getUserPhoto() ).into(holder.imgPostProfile);


    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tvTitle;
        ImageView imgPost, imgPostProfile;

        public MyViewHolder(@NonNull View itemView) {
            super( itemView );

            //initialize the text views and image views
            tvTitle = itemView.findViewById( R.id.row_post_title );
            imgPost = itemView.findViewById( R.id.row_post_img );
            imgPostProfile = itemView.findViewById( R.id.row_post_profile_img );

        }
    }
}
