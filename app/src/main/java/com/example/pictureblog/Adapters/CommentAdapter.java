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
import com.example.pictureblog.Entities.Comment;
import com.example.pictureblog.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {

    private Context mContext;
    private List<Comment> mData;

    public CommentAdapter(Context mContext, List<Comment> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //When we create the view holder we are inflating the row_post_item_layout provided in layouts
        View row = LayoutInflater.from(mContext).inflate( R.layout.row_comment , parent,false); // row_comment.xml file

        return new MyViewHolder( row );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Glide.with(mContext).load( mData.get( position ).getUimg() ).into(holder.img_user );
        holder.tv_name.setText( mData.get( position ).getUname() );
        holder.tv_content.setText( mData.get( position ).getContent() );
        holder.tv_date.setText(  timeStampToString( (Long) mData.get( position ).getTimestamp() ));


    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView img_user;
        TextView tv_name,tv_content,tv_date;
        public MyViewHolder(View itemView){
            super(itemView);
            img_user = itemView.findViewById( R.id.comment_user_img );
            tv_name = itemView.findViewById(R.id.comment_username  );
            tv_content = itemView.findViewById( R.id.comment_content );
            tv_date = itemView.findViewById( R.id.comment_date );
        }
    }

    private String timeStampToString(long time) {
        Calendar calendar = Calendar.getInstance( Locale.ITALIAN );
        calendar.setTimeInMillis( time );
        SimpleDateFormat dataFormat = new SimpleDateFormat( "dd-MM-YYYY" );
        String date = dataFormat.format( calendar.getTime() );
        return date;

    }
}
