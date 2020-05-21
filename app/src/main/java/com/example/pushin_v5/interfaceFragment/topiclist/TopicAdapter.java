package com.example.pushin_v5.interfaceFragment.topiclist;
/***********************************************************************
 This file is an adapter for the topic recycler list in the topic list page
 including any functions like removing item.
 ***********************************************************************/

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushin_v5.R;
import com.example.pushin_v5.dataModel.DataTopic;
import com.example.pushin_v5.jsonTask.HttpConnection;
import com.example.pushin_v5.jsonTask.ParseJson;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.ViewHolder> {
    private ArrayList<DataTopic> arr;
    private LayoutInflater inflater;
    private RecyclerViewClickListener mListener;
    private HttpConnection httpConnection = new HttpConnection();
    private String jsonstring;
    private ParseJson pj = new ParseJson();
    private View view;
    private boolean isBookmark = false;

    TopicAdapter(Context context, ArrayList<DataTopic> object, RecyclerViewClickListener mListener) {
        this.inflater = LayoutInflater.from(context);
        this.arr = object;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public TopicAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = inflater.inflate(R.layout.style_topic, parent, false);
        return new ViewHolder(view, mListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataTopic topic = arr.get(position);
        int topicId = topic.getTopicId();
        String topicTitle = topic.getTopicTitle();
        String topicDetail = topic.getTopicDetail();
        String createTime = topic.getTopicTimeStamp();
        isBookmark = topic.isBookmark();

        //initialize the image button
        if (isBookmark) {
            holder.b_bookmark.setBackground(view.getResources().getDrawable(R.drawable.ic_star_yellow_24dp));
        } else {
            holder.b_bookmark.setBackground(view.getResources().getDrawable(R.drawable.ic_star_border_yellow_24dp));
        }

        //check if the bookmark button is clicked
        holder.b_bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBookmark) {
                    //unmark the flashcard
                    String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/topicUpdateBookmark.php?id=" + topicId + "&isBookmark=0";
                    try {
                        httpConnection = new HttpConnection();
                        jsonstring = httpConnection.execute(tempUrl).get();
                        if (jsonstring != null) {
                            if (pj.identification(jsonstring)) {
                                Toast.makeText(view.getContext(), "you unmarked this topic", Toast.LENGTH_SHORT).show();
                                holder.b_bookmark.setBackground(view.getResources().getDrawable(R.drawable.ic_star_border_yellow_24dp));
                                topic.setBookmark(false);
                                isBookmark = false;
                            }
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    //mark the flashcard
                    String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/topicUpdateBookmark.php?id=" + topicId + "&isBookmark=1";
                    try {
                        httpConnection = new HttpConnection();
                        jsonstring = httpConnection.execute(tempUrl).get();
                        if (jsonstring != null) {
                            if (pj.identification(jsonstring)) {
                                Toast.makeText(view.getContext(), "you marked this topic", Toast.LENGTH_SHORT).show();
                                holder.b_bookmark.setBackground(view.getResources().getDrawable(R.drawable.ic_star_yellow_24dp));
                                topic.setBookmark(true);
                                isBookmark = true;
                            }
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        holder.t_detail.setText(String.valueOf(topicDetail));
        holder.t_title.setText(String.valueOf(topicTitle));
        holder.t_timeStamp.setText("Last edit " + String.valueOf(createTime));
    }

    @Override
    public int getItemCount() {
        if (arr == null) return 0;
        else return arr.size();
    }

    public DataTopic removeItem(int position) {
        boolean check = false;
        DataTopic topic = arr.get(position);
        int topicId = topic.getTopicId();
        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/topicDeletion.php?id=" + topicId;
        try {
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null && pj.identification(jsonstring)) {
                check = true;
            } else check = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        arr.remove(position);
        notifyItemRemoved(position);
        if (check) return topic;
        else return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView t_detail, t_title, t_timeStamp;
        public RelativeLayout viewBackground, viewForeground;
        public ImageButton b_bookmark;
        private RecyclerViewClickListener mListener;

        ViewHolder(View itemView, RecyclerViewClickListener listener) {
            super(itemView);
            mListener = listener;
            //bookmark
            b_bookmark = itemView.findViewById(R.id.b_topic_bookmark);
            //content of the topic
            t_detail = itemView.findViewById(R.id.t_topic_detail);
            t_title = itemView.findViewById(R.id.t_topic_title);
            t_timeStamp = itemView.findViewById(R.id.t_topic_createTime);

            viewBackground = itemView.findViewById(R.id.topic_view_background);
            viewForeground = itemView.findViewById(R.id.topic_view_foreground);
            itemView.setOnClickListener(this);
        }

        public void onClick(View view) {
            mListener.onTopicClick(view, getAdapterPosition());
        }
    }

    public interface RecyclerViewClickListener {
        void onTopicClick(View view, int position);
    }
}
