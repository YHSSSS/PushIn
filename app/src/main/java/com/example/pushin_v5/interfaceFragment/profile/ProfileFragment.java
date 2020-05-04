package com.example.pushin_v5.interfaceFragment.profile;
/***********************************************************************
 This file is a profile page that displaying the information of the users.
 ***********************************************************************/

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;

import com.example.pushin_v5.MainActivity;
import com.example.pushin_v5.R;
import com.example.pushin_v5.imageTask.GetBitmapByURL;
import com.example.pushin_v5.imageTask.ImageProcessClass;
import com.example.pushin_v5.inputTask.CheckStringQuotation;
import com.example.pushin_v5.inputTask.GetNameSpinner;
import com.example.pushin_v5.jsonTask.HttpConnection;
import com.example.pushin_v5.jsonTask.ParseJson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private static Bundle bundle;
    private int userId = 3;
    private HttpConnection httpConnection;
    private ParseJson pj = new ParseJson();
    private String jsonstring;

    private ImageButton profile;
    private Button b_save;
    private EditText t_userNickname;
    private String userLevel = null, userMajor = null, userNickname = null;
    private RadioGroup rg;
    private Spinner s_level;

    //variables for uploading new image
    private Bitmap bitmap;
    private ProgressDialog progressDialog;
    private String UserId = "userId";
    private String ImagePathTAG = "image_path";
    private String ServerUploadPath = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/uploads/uploadProfilePhoto.php";

    public static ProfileFragment newInstance(Bundle b) {
        ProfileFragment fragment = new ProfileFragment();
        bundle = b;
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        //pass parameters
        if (bundle != null) {
            userId = bundle.getInt("userId");
        }

        //initialize the variables
        t_userNickname = root.findViewById(R.id.i_userNickname);
        rg = root.findViewById(R.id.rg_major);
        s_level = root.findViewById(R.id.s_userLevel);
        profile = root.findViewById(R.id.image_userProfile);

        //set spinner value
        GetNameSpinner getNameSpinner = new GetNameSpinner(getContext());
        List<String> levelList = getNameSpinner.level(s_level);

        s_level.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                userLevel = levelList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //get and show profile data
        showUserProfile(root);

        //reset profile photo
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });

        //save the profile
        b_save = root.findViewById(R.id.b_saveProfile);
        b_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveUserProfile(root);
            }
        });

        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void showUserProfile(View root) {
        //get user profile photo
        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/uploads/getProfilePhoto.php?userId=" + userId;
        try {
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null) {
                if (pj.identification(jsonstring)) {
                    //get the image path on the server
                    String imagePath = new JSONObject(jsonstring).getString("image_path");
                    GetBitmapByURL load = new GetBitmapByURL();
                    Bitmap myBitmap = load.execute(imagePath).get();
                    if (myBitmap == null) System.out.println("null bitmap");
                    else {
                        bitmap = myBitmap;
                        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                        drawable.setCircular(true);
                        profile.setImageDrawable(drawable);
                    }
                } else {
                    //use default profile
                    profile.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher_foreground_black));
                }
            }

            //get user profile info
            tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/userInfo.php?id=" + userId;
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null) {
                if (pj.identification(jsonstring)) {
                    JSONObject userInfo = new JSONObject(jsonstring).getJSONArray("user").getJSONObject(0);
                    userMajor = userInfo.getString("userMajor");
                    userNickname = userInfo.getString("userNickname");
                    userLevel = userInfo.getString("userLevel");

                    if (userNickname != null) t_userNickname.setText(userNickname);

                    if (userLevel == null) s_level.setSelection(0);
                    else if (userLevel.equals("B1")) s_level.setSelection(1);
                    else if (userLevel.equals("B2")) s_level.setSelection(2);
                    else if (userLevel.equals("B3")) s_level.setSelection(3);
                    else if (userLevel.equals("B4")) s_level.setSelection(4);
                    else if (userLevel.equals("MS")) s_level.setSelection(5);

                    if (userMajor.equals("AC")) {
                        RadioButton rb = rg.findViewById(R.id.r_ap);
                        rb.setChecked(true);
                    } else if (userMajor.equals("CS")) {
                        RadioButton rb = rg.findViewById(R.id.r_cs);
                        rb.setChecked(true);
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

    private void saveUserProfile(View root) {
        //check if user choose the image in gallery
        if (bitmap == null)
            Toast.makeText(getActivity(), "please pick a profile image", Toast.LENGTH_SHORT).show();
        else {
            Toast.makeText(getActivity(), "Saving, please wait", Toast.LENGTH_SHORT).show();
            //get major
            int index = rg.getCheckedRadioButtonId();
            if (index == -1) userMajor = null;
            else {
                RadioButton selectedRadioButton = rg.findViewById(index);
                String selectedRadioButtonText = selectedRadioButton.getText().toString();
                if (selectedRadioButtonText.equals("Applied Computing")) userMajor = "AC";
                else if (selectedRadioButtonText.equals("Computer Science")) userMajor = "CS";
            }

            //get username
            userNickname = t_userNickname.getText().toString();

            //check if enter username
            if (userNickname == null || userNickname.equals("null"))
                Toast.makeText(getActivity(), "Please enter nickname",
                        Toast.LENGTH_SHORT).show();
                //check if enter level
            else if (userLevel == null)
                Toast.makeText(getActivity(), "Please pick a level",
                        Toast.LENGTH_SHORT).show();
                //check if enter major
            else if (userMajor == null) Toast.makeText(getActivity(), "Please pick a major",
                    Toast.LENGTH_SHORT).show();
            else {
                CheckStringQuotation checkStringQuotation = new CheckStringQuotation();
                userNickname = checkStringQuotation.quotation(userNickname);
                String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/userInfoUpdate.php?id=" + userId +
                        "&major=" + userMajor + "&level=" + userLevel + "&name=" + userNickname;
                try {
                    httpConnection = new HttpConnection();
                    jsonstring = httpConnection.execute(tempUrl).get();
                    if (jsonstring != null) {
                        if (pj.identification(jsonstring)) {
                            //upload profile image
                            ImageUploadToServerFunction();

                            //refresh the activity
                            Bundle b = new Bundle();
                            b.putInt("userId", userId);
                            Intent reload = new Intent(getActivity(), MainActivity.class);
                            reload.putExtras(b);
                            startActivity(reload);
                        }
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /***************************************************************************************
     *   
     *    The following lines of code is copied from:
     *    Title: Android Upload Image To Server Using PHP MySQL
     *    Author: Admin
     *    Date: March 4, 2017
     *    Code version: 1.0
     *    Availability: https://androidjson.com/android-upload-image-server-using-php-mysql/
     *
     ***************************************************************************************/

    //function for showing the gallery
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image From Gallery"), 1);
    }

    //get the bitmap of the image after users picking an image in the gallery
    @Override
    public void onActivityResult(int RC, int RQC, Intent I) {
        super.onActivityResult(RC, RQC, I);
        if (RC == 1 && RQC == RESULT_OK && I != null && I.getData() != null) {
            Uri uri = I.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                drawable.setCircular(true);
                profile.setImageDrawable(drawable);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //upload the image
    public void ImageUploadToServerFunction() {
        ByteArrayOutputStream byteArrayOutputStreamObject;
        byteArrayOutputStreamObject = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStreamObject);
        byte[] byteArrayVar = byteArrayOutputStreamObject.toByteArray();
        final String ConvertImage = Base64.encodeToString(byteArrayVar, Base64.DEFAULT);
        class AsyncTaskUploadClass extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = ProgressDialog.show(getContext(), "Image is Uploading", "Please Wait", false, false);
            }

            @Override
            protected void onPostExecute(String string1) {
                super.onPostExecute(string1);
                progressDialog.dismiss();
                Toast.makeText(getContext(), string1, Toast.LENGTH_LONG).show();
                System.out.println(string1);
            }

            @Override
            protected String doInBackground(Void... params) {
                ImageProcessClass imageProcessClass = new ImageProcessClass();
                HashMap<String, String> HashMapParams = new HashMap<String, String>();
                HashMapParams.put(UserId, Integer.toString(userId));
                HashMapParams.put(ImagePathTAG, ConvertImage);
                String FinalData = imageProcessClass.ImageHttpRequest(ServerUploadPath, HashMapParams);
                return FinalData;
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClassOBJ = new AsyncTaskUploadClass();
        AsyncTaskUploadClassOBJ.execute();
    }

    /***************************************************************************************
     *   
     *    Copy end
     *
     ***************************************************************************************/
}