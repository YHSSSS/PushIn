package com.example.pushin_v5.interfaceFragment.flashcardlist;
/***********************************************************************
 This file is used to create a new flashcard with three spinners needed
 to be selected one item to create. Two spinners is for picking one exam
 and topic that the flashcard belongs to. Another spinner is for picking
 the type of the flashcard including text and image. If the type of the
 flashcard is an image, then a dialog will be provided for users to select
 the method to upload the image and insert the path of the image on the
 server into the database. If the type fo the flashcard is a text, then
 the app will jump to single flashcard page to enter the title and
 content of the flashcard.
 ***********************************************************************/

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
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.pushin_v5.R;
import com.example.pushin_v5.imageTask.ImageProcessClass;
import com.example.pushin_v5.inputTask.GetNameSpinner;
import com.example.pushin_v5.jsonTask.HttpConnection;
import com.example.pushin_v5.jsonTask.ParseJson;
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

public class CreateFlashcardFragment extends Fragment {
    private int userId = 0, moduleId = 0, topicId = 0, typeId = 0, flashcardId = 0;
    private HttpConnection httpConnection;
    private String jsonstring;
    private ParseJson pj = new ParseJson();

    private Spinner s_module, s_topic, s_contentType;
    private RelativeLayout layout_topic, layout_contentType, layout_intsetFlashcard;
    private List<Integer> moduleIdList, topicIdList, typeIdList;
    private Button b_insert;

    //process image
    private boolean isUsingCamera = false;
    private Bitmap bitmap;
    private ProgressDialog progressDialog;
    private String FlashcardId = "flashcardId";
    private String ImagePathTAG = "image_path";
    private String pathToFile, ServerUploadPath = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/uploads/insertFlashcardPhoto.php";
    private AlertDialog dialog;
    private EditText editText;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_create_flashcard, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.create_flashcardTitle);

        userId = (int) getActivity().getIntent().getSerializableExtra("userId");

        b_insert = root.findViewById(R.id.b_insertFlashcard);
        s_module = root.findViewById(R.id.s_flashcardmodule);
        s_topic = root.findViewById(R.id.s_flashcardtopic);
        s_contentType = root.findViewById(R.id.s_flashcardContent);
        layout_topic = root.findViewById(R.id.layout_topicSpinner);
        layout_contentType = root.findViewById(R.id.layout_contentTypeSpinner);
        layout_intsetFlashcard = root.findViewById(R.id.layout_intsetFlashcard);
        layout_topic.setVisibility(View.GONE);
        layout_contentType.setVisibility(View.GONE);
        layout_intsetFlashcard.setVisibility(View.GONE);

        //get name or title of those exam and topics
        GetNameSpinner getNameSpinner = new GetNameSpinner(getContext());
        moduleIdList = getNameSpinner.module(userId, s_module);

        //get name of the exam
        s_module.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                moduleId = moduleIdList.get(position);
                //Toast.makeText(getContext(), "moduleId" + moduleId, Toast.LENGTH_SHORT).show();
                if (moduleId > 0) {
                    topicIdList = getNameSpinner.topic(moduleId, s_topic);
                    layout_topic.setVisibility(View.VISIBLE);
                } else layout_topic.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //get title of the topic
        s_topic.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                topicId = topicIdList.get(position);
                System.out.println("topicId" + topicId);
                if (topicId > 0) {
                    typeIdList = getNameSpinner.flashcardType(s_contentType);
                    layout_contentType.setVisibility(View.VISIBLE);
                } else layout_contentType.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //get the content type of the flaschard
        s_contentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                typeId = typeIdList.get(position);
                //if the item selected is default item
                if (typeId == 0) layout_intsetFlashcard.setVisibility(View.GONE);
                else layout_intsetFlashcard.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        b_insert.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onInsertClick(root);
            }
        });

        return root;
    }

    public void onInsertClick(View root) {
        if (typeId == 1) {
            //text
            insertFlashcard("TE");
            //jump to single flashcard fragment
            SingleFlashcardFragment singleFlashcardFragment = new SingleFlashcardFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("userId", userId);
            bundle.putInt("topicId", topicId);
            bundle.putInt("moduleId", moduleId);
            bundle.putInt("flashcardId", flashcardId);
            singleFlashcardFragment.newInstance(bundle);
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.nav_flashcardSingle);
        } else if (typeId == 2) {
            //image
            insertFlashcard("IM");
            selectUploadMethod(root);
        }
    }

    //insert new flashcard with the order and content type
    public void insertFlashcard(String type) {
        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/flashcardInsertion.php?tid=" + topicId + "&type=" + type;
        try {
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null && pj.identification(jsonstring)) {
                flashcardId = new JSONObject(jsonstring).getInt("flashcardId");
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

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

    //link image
    public void onLinkClick() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Toast.makeText(getContext(), "Loading Image...", Toast.LENGTH_LONG).show();
                Picasso.with(getContext()).load(editText.getText().toString()).into(new Target() {
                    @Override
                    public void onBitmapLoaded(final Bitmap b, Picasso.LoadedFrom from) {
                        bitmap = b;
                        dialog.dismiss();
                        ImageUploadToServerFunction();

                        //jump to flaschard page
                        FlashcardListFragment flashcardList = new FlashcardListFragment();
                        Bundle bundle = new Bundle();
                        bundle.putInt("userId", userId);
                        bundle.putInt("topicId", topicId);
                        bundle.putInt("moduleId", moduleId);
                        flashcardList.setArguments(bundle);
                        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                        navController.navigate(R.id.nav_flashcardlist);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void onCameraClick() {
        isUsingCamera = true;
        dispatchPictureTakeAction();
    }

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

    //open gallery
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image From Gallery"), 1);
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
        ImageUploadToServerFunction();

        //jump to flashcard page
        FlashcardListFragment flashcardList = new FlashcardListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("userId", userId);
        bundle.putInt("topicId", topicId);
        bundle.putInt("moduleId", moduleId);
        flashcardList.newInstance(bundle);
        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.nav_flashcardlist);
    }

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

}
