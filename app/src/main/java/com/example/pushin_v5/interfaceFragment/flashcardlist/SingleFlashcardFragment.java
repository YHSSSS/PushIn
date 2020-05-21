package com.example.pushin_v5.interfaceFragment.flashcardlist;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.pushin_v5.R;
import com.example.pushin_v5.imageTask.GetBitmapByURL;
import com.example.pushin_v5.imageTask.ImageProcessClass;
import com.example.pushin_v5.inputTask.CheckStringQuotation;
import com.example.pushin_v5.inputTask.GetNameSpinner;
import com.example.pushin_v5.jsonTask.HttpConnection;
import com.example.pushin_v5.jsonTask.ParseJson;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class SingleFlashcardFragment extends Fragment {

    //show images
    private static Bundle bundle;
    private int topicId = 0, moduleId = 0, userId = 0, flashcardId = 0, paragraphId = 0, updated_topicId = 0;
    private HttpConnection httpConnection;
    private String jsonstring;
    private ParseJson pj = new ParseJson();
    private Bitmap bitmap;
    private ImageButton imageButton;

    private List<Integer> topicIdList;

    //upload images
    private boolean isUsingCamera = false;
    private AlertDialog dialog;
    private EditText editText;
    private ProgressDialog progressDialog;
    private String FlashcardId = "flashcardId";
    private String ImagePathTAG = "image_path";
    private String pathToFile, ServerUploadPath = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/uploads/uploadFlashcardPhoto.php";

    private EditText i_flashcardText, i_flashcardTitle;
    private String contentType = "";

    public static SingleFlashcardFragment newInstance(Bundle b) {
        SingleFlashcardFragment fragment = new SingleFlashcardFragment();
        bundle = b;
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_flashcardsingle, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.flashcard);

        //pass parameters
        if (bundle != null) {
            flashcardId = bundle.getInt("flashcardId");
            topicId = bundle.getInt("topicId");
            moduleId = bundle.getInt("moduleId");
            userId = bundle.getInt("userId");
        }

        String timestamp = "";

        Spinner s_flashcard;
        TextView t_titleCount, t_timestamp;
        RelativeLayout layout_image, layout_text;
        Button b_saveImage, b_upload;

        s_flashcard = root.findViewById(R.id.s_update_flashcard_topic);
        //for text flashcard
        layout_text = root.findViewById(R.id.layout_flashcard_paragraph);
        i_flashcardText = root.findViewById(R.id.i_flashcardText);
        i_flashcardTitle = root.findViewById(R.id.i_flashcardTitle);
        t_titleCount = root.findViewById(R.id.t_countTitle);
        t_timestamp = root.findViewById(R.id.t_flashcard_single_timestamp);

        //for image flashcard
        imageButton = root.findViewById(R.id.image_flashcardImage);
        layout_image = root.findViewById(R.id.layout_flashcard_image);
        b_saveImage = root.findViewById(R.id.b_saveImageFlashcard);
        b_upload = root.findViewById(R.id.b_uploadImageFlashcard);

        //initialize spinner
        //get name or title of those exam and topics
        GetNameSpinner getNameSpinner = new GetNameSpinner(getContext());
        topicIdList = getNameSpinner.topic(moduleId, s_flashcard);
        for (int i = 0; i < topicIdList.size(); i++) {
            //set current topic
            if (topicIdList.get(i) == topicId) {
                s_flashcard.setSelection(i);
            }
        }

        //get name of the topic
        s_flashcard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updated_topicId = topicIdList.get(position);
                updatedTopicId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //get the flashcard info
        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/flashcardSingle.php?id=" + flashcardId;
        try {
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null && pj.identification(jsonstring)) {
                JSONObject flashcard = new JSONObject(jsonstring).getJSONObject("flashcard");
                contentType = flashcard.getString("contentType");
                timestamp = flashcard.getString("lastEditFlashcardTimestamp");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (contentType.equals("TE")) {
            //if is text flashcard
            layout_image.setVisibility(GONE);

            tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/paragraphSingle.php?id=" + flashcardId;
            try {
                httpConnection = new HttpConnection();
                jsonstring = httpConnection.execute(tempUrl).get();
                if (jsonstring != null && pj.identification(jsonstring)) {
                    JSONObject para = new JSONObject(jsonstring).getJSONObject("paragraph");
                    paragraphId = para.getInt("paragraphId");
                    String title = para.getString("paragraphTitle");
                    String content = para.getString("paragraphContent");

                    i_flashcardText.setText(String.valueOf(content));
                    i_flashcardTitle.setText(String.valueOf(title));
                    t_timestamp.setText(String.valueOf(timestamp));
                    //get the title words count
                    t_titleCount.setText(title.length() + "/100");
                } else {
                    //insert a new paragraph
                    insertParagraph(i_flashcardText.getText().toString(), i_flashcardTitle.getText().toString());
                    t_timestamp.setText(String.valueOf(timestamp));
                    //get the title words count
                    t_titleCount.setText(i_flashcardTitle.getText().toString().length() + "/100");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            i_flashcardText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    saveText();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            i_flashcardTitle.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    t_titleCount.setText(i_flashcardTitle.getText().toString().length() + "/100");
                    saveText();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

        } else if (contentType.equals("IM")) {
            //if is image flashcard
            layout_text.setVisibility(GONE);
            tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/uploads/getFlashcardPhoto.php?id=" + flashcardId;
            try {
                httpConnection = new HttpConnection();
                jsonstring = httpConnection.execute(tempUrl).get();
                if (jsonstring != null) {
                    if (pj.identification(jsonstring)) {
                        String imagePath = new JSONObject(jsonstring).getString("image_path");
                        GetBitmapByURL load = new GetBitmapByURL();
                        Bitmap myBitmap = load.execute(imagePath).get();
                        if (myBitmap == null) System.out.println("null bitmap");
                        else {
                            bitmap = myBitmap;
                            imageButton.setImageBitmap(bitmap);
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

            imageButton.setOnClickListener(new View.OnClickListener() {
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

            b_upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectUploadMethod(root);
                }
            });

            b_saveImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (topicId != updated_topicId) updatedTopicId();
                    ImageUploadToServerFunction();
                }
            });
        }

        return root;
    }

    public void saveText() {
        if (!TextUtils.isEmpty(i_flashcardTitle.getText().toString()) &&
                !TextUtils.isEmpty(i_flashcardText.getText().toString())) {
            String temp_content = i_flashcardText.getText().toString();
            String temp_title = i_flashcardTitle.getText().toString();
            temp_content = CheckStringQuotation.quotation(temp_content);
            temp_title = CheckStringQuotation.quotation(temp_title);
            String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/paragraphInfoUpdate.php?id=" +
                    paragraphId + "&content=" + temp_content + "&title=" + temp_title;
            try {
                httpConnection = new HttpConnection();
                jsonstring = httpConnection.execute(tempUrl).get();
                if (jsonstring != null && pj.identification(jsonstring)) {
                    Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void updatedTopicId() {
        if (updated_topicId != 0) {
            String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/topicIdUpdate.php?tid=" +
                    updated_topicId + "&fid=" + flashcardId;
            try {
                httpConnection = new HttpConnection();
                jsonstring = httpConnection.execute(tempUrl).get();
                if (jsonstring != null && pj.identification(jsonstring)) {
                    Toast.makeText(getContext(), "Updated", Toast.LENGTH_SHORT).show();
                    topicId = updated_topicId;
                    updated_topicId = 0;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    //insert new paragraph with title and content
    public boolean insertParagraph(String content, String title) {
        //transform quotation, space which are easy to pass through php
        content = CheckStringQuotation.quotation(content);
        title = CheckStringQuotation.quotation(title);
        //send a request to server
        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/paragraphInsertion.php?" +
                "fid=" + flashcardId + "&content=" + content + "&title=" + title;
        try {
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null && pj.identification(jsonstring)) {
                return true;
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    //an alert dialog to choose using gallery, url or camera to get the image
    public void selectUploadMethod(View root) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        dialog = builder.create();
        View dialogView = View.inflate(getContext(), R.layout.alert_upload_images, null);
        dialog.setView(dialogView);
        dialog.setTitle("Select the method to upload image");
        dialog.show();
        View urlLayout;

        editText = dialogView.findViewById(R.id.i_upload_image_link);
        urlLayout = dialogView.findViewById(R.id.layout_upload_link);
        urlLayout.setVisibility(GONE);

        Button camera, url, gallery;
        camera = dialogView.findViewById(R.id.b_uploadByCamera);
        url = dialogView.findViewById(R.id.b_uploadByLink);
        gallery = dialogView.findViewById(R.id.b_uploadByGallery);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                urlLayout.setVisibility(GONE);
                onCameraClick();
            }
        });

        url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                urlLayout.setVisibility(VISIBLE);
                onLinkClick();
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                urlLayout.setVisibility(GONE);
                onGalleryClick();
            }
        });
    }

    //gallery image
    public void onGalleryClick() {
        isUsingCamera = false;
        showFileChooser();
    }

    //url image
    public void onLinkClick() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Toast.makeText(getContext(), "Loading Image...", Toast.LENGTH_LONG).show();
                Picasso.with(getContext()).load(editText.getText().toString()).into(new Target() {
                    @Override
                    public void onBitmapLoaded(final Bitmap b, Picasso.LoadedFrom from) {
                        bitmap = b;
                        imageButton.setImageBitmap(bitmap);
                        dialog.dismiss();
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
            }
        });
    }

    //camera image
    public void onCameraClick() {
        isUsingCamera = true;
        dispatchPictureTakeAction();
    }

    //capture camera
    private void dispatchPictureTakeAction() {
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePic.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            photoFile = createPhotoFile();

            if (photoFile != null) {
                pathToFile = photoFile.getAbsolutePath();
                Uri photoURI = FileProvider.getUriForFile(getContext(), "com.example.pushin_v5", photoFile);
                takePic.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePic, 1);
            }
        }
    }

    private File createPhotoFile() {
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(name, ".png", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    //including camera and gallery result
    @Override
    public void onActivityResult(int RC, int RQC, Intent I) {
        super.onActivityResult(RC, RQC, I);
        //gallery action
        if (!isUsingCamera) {
            if (RC == 1 && RQC == RESULT_OK && I != null && I.getData() != null) {
                Uri uri = I.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }//camera action
        else {
            if (RC == 1 && RQC == RESULT_OK) {
                bitmap = BitmapFactory.decodeFile(pathToFile);
            }
        }
        dialog.dismiss();
        imageButton.setImageBitmap(bitmap);
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

    //open gallery
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image From Gallery"), 1);
    }

    //upload images
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
            }

            @Override
            protected String doInBackground(Void... params) {
                ImageProcessClass imageProcessClass = new ImageProcessClass();
                HashMap<String, String> HashMapParams = new HashMap<String, String>();
                HashMapParams.put(FlashcardId, Integer.toString(flashcardId));
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
