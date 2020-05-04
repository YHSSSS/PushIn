package com.example.pushin_v5.interfaceFragment.review;
/***********************************************************************
 This file is an adapter for the flashcard recycler list in the review page
 including any functions like un-bookmarking item.
 ***********************************************************************/

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushin_v5.R;
import com.example.pushin_v5.dataModel.DataFlashcard;
import com.example.pushin_v5.imageTask.GetBitmapByURL;
import com.example.pushin_v5.jsonTask.HttpConnection;
import com.example.pushin_v5.jsonTask.ParseJson;
import com.github.chrisbanes.photoview.PhotoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ReviewFlashcardAdapter extends RecyclerView.Adapter<ReviewFlashcardAdapter.ViewHolder> {
    private int paragraphId = 0;
    private ArrayList<DataFlashcard> arr;
    private LayoutInflater inflater;
    private HttpConnection httpConnection = new HttpConnection();
    private String jsonstring, imagePath;
    private ParseJson pj = new ParseJson();
    private Bitmap bitmap;
    private View view;

    ReviewFlashcardAdapter(Context context, ArrayList<DataFlashcard> object) {
        this.inflater = LayoutInflater.from(context);
        this.arr = object;
    }

    @Override
    public ReviewFlashcardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = inflater.inflate(R.layout.style_review_flashcard, parent, false);
        return new ReviewFlashcardAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewFlashcardAdapter.ViewHolder holder, int position) {
        DataFlashcard flashcard = arr.get(position);
        int flashcardId = flashcard.getFlashcardId();
        String contentType = flashcard.getContentType();

        //check the content type of the flashcard
        if (contentType.equals("TE")) {
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

            holder.i_flashcardImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(inflater.getContext());
                    View mView = inflater.inflate(R.layout.alert_show_image, null);
                    PhotoView photoView = mView.findViewById(R.id.image_zoom);
                    photoView.setImageBitmap(bitmap);
                    mBuilder.setView(mView);
                    AlertDialog mDialog = mBuilder.create();
                    mDialog.show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (arr == null) return 0;
        else return arr.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView t_flashcardText, t_flashcardTitle;
        public ImageButton i_flashcardImage;
        public RelativeLayout imageLayout, textLayout, viewBackground, viewForeground;
        ;
        public ImageButton b_bookmark;

        ViewHolder(View itemView) {
            super(itemView);
            //edit text
            i_flashcardImage = itemView.findViewById(R.id.image_review_flashcardImage);
            t_flashcardText = itemView.findViewById(R.id.t_review_flashcardText);
            t_flashcardTitle = itemView.findViewById(R.id.t_review_flashcardTitle);
            //layout
            imageLayout = itemView.findViewById(R.id.layout_review_flashcard_list_image);
            textLayout = itemView.findViewById(R.id.layout_review_flashcard_list_text);
            viewForeground = itemView.findViewById(R.id.layout_review_flashcard_foreground);
            viewBackground = itemView.findViewById(R.id.layout_review_flashcard_blackground);
        }
    }
}