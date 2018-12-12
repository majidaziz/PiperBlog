package com.example.cuadmin.testblog;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.w3c.dom.Text;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder>
{

    public List<BlogPost> blog_list;
    public Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;


    public BlogRecyclerAdapter(List<BlogPost> blog_list)
    {
        this.blog_list = blog_list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);

        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {


        final String blogPostId = blog_list.get(position).BlogPostId;
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();
        String desc_data = blog_list.get(position).getDesc();
        holder.setDescText(desc_data);

        String image_url = blog_list.get(position).getImage_url();
        holder.setBlogImage(image_url);

        String user_id = blog_list.get(position).getUser_id();

        firebaseFirestore.collection("User").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("images");

                    holder.setUserData(userName, userImage);
                }
                else {

                }
            }
        });

        long millisex = blog_list.get(position).getTimestamp().getTime();
        String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisex)).toString();

        holder.setTime(dateString);

        //Get num likes
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.drawable.ic_heart_click));
                }
                else{
                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.drawable.ic_heart));
                }
            }
        });

        //Heart button
        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(!task.getResult().exists()){
                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(likesMap);

                        }
                        else{
                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).delete();
                        }

                    }
                });

            }
        });
    }

    @Override
    public int getItemCount()
    {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {

        private View mView;
        private TextView descView;
        private ImageView blogImageView;
        private TextView blogDate;

        private TextView blogUsername;
        private CircleImageView blogUserImage;

        private ImageView blogLikeBtn;
        private TextView blogLikeCount;

        public ViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;

            blogLikeBtn = mView.findViewById(R.id.blog_like_btn);

        }

        public void setDescText(String descText)
        {
            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(descText);
        }

        public void setBlogImage(String downloadUri)
        {
            blogImageView = mView.findViewById(R.id.blog_image);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.maine_coon);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri).into(blogImageView);
        }

        public void setTime(String date)
        {

            blogDate = mView.findViewById(R.id.blog_date);
            blogDate.setText(date);
        }

        public void setUserData(String name, String image){
            blogUserImage = mView.findViewById(R.id.blog_user_image);
            blogUsername = mView.findViewById(R.id.blog_user_name);

            blogUsername.setText(name);

            RequestOptions placeHolderOption = new RequestOptions();
            placeHolderOption.placeholder(R.drawable.maine_coon);
            Glide.with(context).applyDefaultRequestOptions(placeHolderOption).load(image).into(blogUserImage);
        }
    }
}
