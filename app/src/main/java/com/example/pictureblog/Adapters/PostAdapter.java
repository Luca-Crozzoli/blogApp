package com.example.pictureblog.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pictureblog.Activities.PostDetailActivity;
import com.example.pictureblog.Entities.Post;
import com.example.pictureblog.R;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {

    private Context mContext;
    private List<Post> mData;

    public PostAdapter(Context mContext, List<Post> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //When we create the view holder we are inflating the row_post_item_layout provided in layouts
        View row = LayoutInflater.from( mContext ).inflate( R.layout.row_post_item, parent, false ); // row_post_item.xml

        return new MyViewHolder( row );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //update the holder with the appropriate values: post image and title of the post and user profile
        // we are calling the methods defined in the post class
        holder.tvTitle.setText( mData.get( position ).getTitle() );
        Glide.with( mContext ).load( mData.get( position ).getPicture() ).into( holder.imgPost );

        String userImg = mData.get( position ).getUserPhoto();
        Glide.with( mContext ).load( userImg ).into( holder.imgPostProfile );

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        ImageView imgPost, imgPostProfile;

        public MyViewHolder(@NonNull View itemView) {
            super( itemView );

            //initialize the text views and image views
            tvTitle = itemView.findViewById( R.id.row_post_title );
            imgPost = itemView.findViewById( R.id.row_post_img );
            imgPostProfile = itemView.findViewById( R.id.row_post_profile_img );

            itemView.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent postDetailActivity = new Intent( mContext, PostDetailActivity.class );
                    int position = getAdapterPosition();

                    postDetailActivity.putExtra( "title", mData.get( position ).getTitle() );
                    postDetailActivity.putExtra( "postImage", mData.get( position ).getPicture() );
                    postDetailActivity.putExtra( "description", mData.get( position ).getDescription() );
                    postDetailActivity.putExtra( "postKey", mData.get( position ).getPostKey() );
                    postDetailActivity.putExtra( "userPhoto", mData.get( position ).getUserPhoto() );
                    postDetailActivity.putExtra( "postLocation", mData.get( position ).getPostLocation() );
                    postDetailActivity.putExtra( "postPlace",mData.get( position ).getPostPlace() );
                    postDetailActivity.putExtra( "userName", mData.get( position ).getUserName() );


                    //here we provide the time stamp
                    long timestamp = (long) mData.get( position ).getTimeStamp();
                    postDetailActivity.putExtra( "postDate", timestamp );

                    //start the activity after passing the different parameter using put extra
                    mContext.startActivity( postDetailActivity );


                }
            } );

        }
    }
}
