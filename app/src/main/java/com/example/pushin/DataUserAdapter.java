package com.example.pushin;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pushin.model.DataUser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DataUserAdapter extends ArrayAdapter<DataUser> {

    List<DataUser> mDataUser;
    LayoutInflater mInflater;

    public DataUserAdapter(@NonNull Context context, @NonNull List<DataUser> objects) {
        super(context, R.layout.list_email, objects);

        mDataUser = objects;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.list_email, parent, false);
        }

        TextView tvEmail = (TextView) convertView.findViewById(R.id.text1);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.image1);

        DataUser user = mDataUser.get(position);

        tvEmail.setText(user.getUserEmail());
        //imageView.setImageResource(R.drawable.iu1);


        InputStream inputStream = null;
        try {
            String imageFile = user.getUserImage();
            inputStream = getContext().getAssets().open(imageFile);
            Drawable d = Drawable.createFromStream(inputStream, null);
            imageView.setImageDrawable(d);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(inputStream != null){
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return convertView;
    }
}
