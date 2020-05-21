package com.example.pushin_v5.interfaceFragment.lecturematerials;
/***********************************************************************
 This file is an adapter for the materials recycler list in the lecture
 materials page.
 ***********************************************************************/
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushin_v5.R;
import com.example.pushin_v5.jsonTask.ParseJson;
import com.example.pushin_v5.jsonTask.HttpConnection;
import com.example.pushin_v5.dataModel.DataMaterials;
import com.example.pushin_v5.inputTask.GetNameSpinner;
import com.example.pushin_v5.interfaceFragment.examlist.CreateExamFragment;
import com.example.pushin_v5.interfaceFragment.topiclist.TopicListFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MaterialsAdapter extends RecyclerView.Adapter<MaterialsAdapter.ViewHolder> {
    private ArrayList<DataMaterials> arr;
    private LayoutInflater inflater;
    private RecyclerViewClickListener mListener;
    private HttpConnection httpConnection = new HttpConnection();
    private String jsonstring, jsonstring1, jsonstring2;
    private ParseJson pj = new ParseJson();
    private int userId = 0, moduleId = 0;

    MaterialsAdapter(int userId, Context context, ArrayList<DataMaterials> object, RecyclerViewClickListener mListener) {
        this.userId = userId;
        this.inflater = LayoutInflater.from(context);
        this.arr = object;
        this.mListener = mListener;
    }

    @Override
    public MaterialsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.style_material, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataMaterials material = arr.get(position);
        int materialId = material.getModuleId();
        String materialName = material.getModuleName();

        holder.t_moduleName.setText(String.valueOf(materialName));
        holder.b_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show an alertdialog to choose the method to add the materials
                AlertDialog.Builder builder = new AlertDialog.Builder(inflater.getContext());
                builder.setMessage("Select one exam to upload image or create an exam");
                View dialogView = View.inflate(inflater.getContext(), R.layout.alert_add_materials, null);
                builder.setView(dialogView);
                AlertDialog dialog = builder.show();

                Spinner s_exam = dialogView.findViewById(R.id.s_add_materials_exam);
                Button create = dialogView.findViewById(R.id.b_create_exam_add_materials);
                Button positive = dialogView.findViewById(R.id.b_add_materials_positive);
                Button negative = dialogView.findViewById(R.id.b_add_materials_negative);
                //initialize spinner
                GetNameSpinner getNameSpinner = new GetNameSpinner(inflater.getContext());
                List<Integer> examList = getNameSpinner.module(userId, s_exam);
                //get the exam that to add in
                s_exam.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        moduleId = examList.get(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                //jump to create exam fragment
                create.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        CreateExamFragment createExamFragment = new CreateExamFragment();
                        Bundle bundle = new Bundle();
                        bundle.putInt("userId", userId);
                        createExamFragment.setArguments(bundle);
                        NavController navController = Navigation.findNavController((Activity) inflater.getContext(), R.id.nav_host_fragment);
                        navController.navigate(R.id.nav_createExam);
                    }
                });

                positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (moduleId == 0)
                            Toast.makeText(inflater.getContext(), "Please pick an exam", Toast.LENGTH_SHORT).show();
                        else {
                            addMaterials(materialId);
                            dialog.cancel();
                        }
                    }
                });

                negative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        if (arr == null) return 0;
        else return arr.size();
    }

    public void addMaterials(int materialId) {
        //add materials in the exam
        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/" +
                "generateLectureMaterials.php?materialsid=" + materialId;
        try {
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null && pj.identification(jsonstring)) {
                JSONArray materialsArray = new JSONObject(jsonstring).getJSONArray("materials");

                //parse topic list
                for (int i = 0; i < materialsArray.length(); i++) {
                    JSONObject topic = materialsArray.getJSONObject(i);
                    String temp_topicTitle = topic.getString("topicTitle");
                    String temp_topicDetail = topic.getString("topicDetail");

                    //insert this topic
                    tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/topicInsertion.php?mid="
                            + moduleId + "&title=" + temp_topicTitle + "&detail=" + temp_topicDetail;
                    httpConnection = new HttpConnection();
                    jsonstring1 = httpConnection.execute(tempUrl).get();
                    if (jsonstring1 != null && pj.identification(jsonstring1)) {
                        //get topicId
                        int temp_topicId = new JSONObject(jsonstring1).getInt("topicId");

                        //parse flashcard list
                        if (topic.has("flashcard")) {
                            JSONArray flashcardArray = topic.getJSONArray("flashcard");
                            for (int j = 0; j < flashcardArray.length(); j++) {
                                JSONObject flashcard = flashcardArray.getJSONObject(j);
                                String temp_contentType = flashcard.getString("contentType");

                                tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/" +
                                        "flashcardInsertion.php?tid=" + temp_topicId + "&type=" +
                                        temp_contentType;
                                httpConnection = new HttpConnection();
                                jsonstring2 = httpConnection.execute(tempUrl).get();
                                if (jsonstring2 != null && pj.identification(jsonstring2)) {
                                    //get flahcardId
                                    int temp_flashcardId = new JSONObject(jsonstring2).getInt("flashcardId");

                                    //get paragraph
                                    if (temp_contentType.equals("TE") && flashcard.has("paragraph")) {
                                        JSONObject paragraph = flashcard.getJSONObject("paragraph");
                                        String temp_para_title = paragraph.getString("paragraphTitle");
                                        String temp_para_content = paragraph.getString("paragraphContent");

                                        tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/" +
                                                "paragraphInsertion.php?fid=" + temp_flashcardId + "&content=" +
                                                temp_para_content + "&title=" + temp_para_title;
                                        httpConnection = new HttpConnection();
                                        httpConnection.execute(tempUrl);

                                    } else if (temp_contentType.equals("IM") && flashcard.has("image")) {
                                        JSONObject image = flashcard.getJSONObject("image");
                                        String temp_image_path = image.getString("image_path");

                                        tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/" +
                                                "uploads/duplicateFlashcardPhoto.php?name=" + temp_image_path.substring(92)
                                                + "&fid=" + temp_flashcardId;
                                        httpConnection = new HttpConnection();
                                        String jsonstring3 = httpConnection.execute(tempUrl).get();
                                    }
                                }
                            }
                        }
                    }
                }

                Toast.makeText(inflater.getContext(), "Added", Toast.LENGTH_SHORT).show();
                TopicListFragment topicListFragment = new TopicListFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("userId", userId);
                bundle.putInt("moduleId", moduleId);
                topicListFragment.newInstance(bundle);
                NavController navController = Navigation.findNavController((Activity) inflater.getContext(), R.id.nav_host_fragment);
                navController.navigate(R.id.nav_topiclist);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView t_moduleName;
        public Button b_add;
        private RecyclerViewClickListener mListener;

        ViewHolder(View itemView, RecyclerViewClickListener listener) {
            super(itemView);
            mListener = listener;
            t_moduleName = itemView.findViewById(R.id.t_material_name);
            b_add = itemView.findViewById(R.id.b_download_materials);
            itemView.setOnClickListener(this);
        }

        public void onClick(View view) {
            mListener.onMaterialClick(view, getAdapterPosition());
        }
    }

    public interface RecyclerViewClickListener {
        void onMaterialClick(View view, int position);
    }
}

