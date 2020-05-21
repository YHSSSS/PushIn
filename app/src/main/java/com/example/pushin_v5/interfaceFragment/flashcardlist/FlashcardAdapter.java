package com.example.pushin_v5.interfaceFragment.flashcardlist;
/***********************************************************************
 This file is an adapter for the flashcard recycler list in the flashcard
 list page including any functions like removing item.
 ***********************************************************************/

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushin_v5.R;
import com.example.pushin_v5.dataModel.DataFlashcard;
import com.example.pushin_v5.imageTask.GetBitmapByURL;
import com.example.pushin_v5.jsonTask.HttpConnection;
import com.example.pushin_v5.jsonTask.ParseJson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.ViewHolder> {
    private int paragraphId = 0;
    private ArrayList<DataFlashcard> arr;
    private LayoutInflater inflater;
    private RecyclerViewClickListener mListener;
    private HttpConnection httpConnection = new HttpConnection();
    private String jsonstring, imagePath;
    private ParseJson pj = new ParseJson();
    private Bitmap bitmap;
    private View view;
    private boolean isBookmark = false;

    FlashcardAdapter(Context context, ArrayList<DataFlashcard> object, RecyclerViewClickListener mListener) {
        this.inflater = LayoutInflater.from(context);
        this.arr = object;
        this.mListener = mListener;
    }

    @Override
    public FlashcardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = inflater.inflate(R.layout.style_flashcard, parent, false);
        return new ViewHolder(view, mListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataFlashcard flashcard = arr.get(position);
        int flashcardId = flashcard.getFlashcardId();
        String timestamp = flashcard.getTimestamp();
        String contentType = flashcard.getContentType();
        isBookmark = flashcard.isBookmark();

        //initialize the image button
        if (isBookmark) {
            holder.b_bookmark.setBackground(view.getResources().getDrawable(R.drawable.ic_star_yellow_24dp));
        } else {
            holder.b_bookmark.setBackground(view.getResources().getDrawable(R.drawable.ic_star_border_yellow_24dp));
        }

        //get the timestamp to textview
        holder.t_timestamp.setText("last edit at: " + String.valueOf(timestamp));

        //check the content type of the flashcard
        if (contentType.equals("TE")) {
            //set the timestamp textview below the paragraph
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.t_timestamp.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, holder.textLayout.getId());
            holder.t_timestamp.setLayoutParams(params);
            //make image button invisible
            holder.imageLayout.setVisibility(View.GONE);
            //show paragraph text
            String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/paragraphSingle.php?id=" + flashcardId;
            try {
                httpConnection = new HttpConnection();
                jsonstring = httpConnection.execute(tempUrl).get();
                if (jsonstring != null && pj.identification(jsonstring)) {
                    JSONObject para = new JSONObject(jsonstring).getJSONObject("paragraph");
                    paragraphId = para.getInt("paragraphId");
                    String title = para.getString("paragraphTitle");
                    String content = para.getString("paragraphContent");
                    holder.t_flashcardText.setText(String.valueOf(content));
                    holder.t_flashcardTitle.setText(String.valueOf(title));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (contentType.equals("IM")) {
            //set the timestamp textview below the paragraph
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.t_timestamp.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, holder.imageLayout.getId());
            //make paragraph text invisible
            holder.t_timestamp.setLayoutParams(params);
            holder.textLayout.setVisibility(View.GONE);
            holder.textLayout.setVisibility(View.GONE);
            //get image path and show the image
            String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/uploads/getFlashcardPhoto.php?id=" + flashcardId;
            try {
                httpConnection = new HttpConnection();
                jsonstring = httpConnection.execute(tempUrl).get();
                if (jsonstring != null) {
                    if (pj.identification(jsonstring)) {
                        imagePath = new JSONObject(jsonstring).getString("image_path");
                        GetBitmapByURL load = new GetBitmapByURL();
                        Bitmap myBitmap = load.execute(imagePath).get();
                        if (myBitmap == null) System.out.println("null bitmap");
                        else {
                            bitmap = myBitmap;
                            holder.i_flashcardImage.setImageBitmap(bitmap);
                        }
                    }
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //check if the bookmark button is clicked
        holder.b_bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBookmark) {
                    //unmark the flashcard
                    String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/flashcardUpdateBookmark.php?id=" + flashcardId + "&isBookmark=0";
                    try {
                        httpConnection = new HttpConnection();
                        jsonstring = httpConnection.execute(tempUrl).get();
                        if (jsonstring != null) {
                            if (pj.identification(jsonstring)) {
                                Toast.makeText(view.getContext(), "you unmarked this flashcard", Toast.LENGTH_SHORT).show();
                                holder.b_bookmark.setBackground(view.getResources().getDrawable(R.drawable.ic_star_border_yellow_24dp));
                                flashcard.setBookmark(false);
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
                    String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/flashcardUpdateBookmark.php?id=" + flashcardId + "&isBookmark=1";
                    try {
                        httpConnection = new HttpConnection();
                        jsonstring = httpConnection.execute(tempUrl).get();
                        if (jsonstring != null) {
                            if (pj.identification(jsonstring)) {
                                Toast.makeText(view.getContext(), "you marked this flashcard", Toast.LENGTH_SHORT).show();
                                holder.b_bookmark.setBackground(view.getResources().getDrawable(R.drawable.ic_star_yellow_24dp));
                                flashcard.setBookmark(true);
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

    }

    @Override
    public int getItemCount() {
        if (arr == null) return 0;
        else return arr.size();
    }

    public DataFlashcard removeItem(int position) {
        boolean check = false;
        //get the flashcard which is going to delete
        DataFlashcard flashcard = arr.get(position);
        int flashcardId = flashcard.getFlashcardId();
        //delete this flashcard
        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/flashcardDeletion.php?id=" + flashcardId;
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
        //make the list view visible for deletion
        arr.remove(position);
        notifyItemRemoved(position);
        if (check) return flashcard;
        else return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView t_flashcardText, t_flashcardTitle, t_timestamp;
        public ImageView i_flashcardImage;
        public RelativeLayout viewBackground, viewForeground, imageLayout, textLayout;
        public ImageButton b_bookmark;
        private RecyclerViewClickListener mListener;

        ViewHolder(View itemView, RecyclerViewClickListener listener) {
            super(itemView);
            mListener = listener;
            //bookmark
            b_bookmark = itemView.findViewById(R.id.b_flashcard_bookmark);
            //timestamp
            t_timestamp = itemView.findViewById(R.id.t_flashcardTimestamp);
            //edit text
            i_flashcardImage = itemView.findViewById(R.id.image_flashcardImage);
            t_flashcardText = itemView.findViewById(R.id.t_flashcardText);
            t_flashcardTitle = itemView.findViewById(R.id.t_flashcardTitle);
            //delete background
            viewBackground = itemView.findViewById(R.id.flashcard_view_background);
            viewForeground = itemView.findViewById(R.id.flashcard_view_foreground);

            imageLayout = itemView.findViewById(R.id.layout_flashcard_list_image);
            textLayout = itemView.findViewById(R.id.layout_flashcard_list_text);
            itemView.setOnClickListener(this);
        }

        public void onClick(View view) {
            mListener.onModuleClick(view, getAdapterPosition());
        }
    }

    public interface RecyclerViewClickListener {
        void onModuleClick(View view, int position);
    }
}
