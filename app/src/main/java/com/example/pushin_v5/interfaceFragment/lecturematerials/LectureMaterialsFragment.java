package com.example.pushin_v5.interfaceFragment.lecturematerials;
/***********************************************************************
 This file is a lecture materials page which is used for users to add
 materials in their exam notes.
 ***********************************************************************/
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pushin_v5.R;
import com.example.pushin_v5.jsonTask.ParseJson;
import com.example.pushin_v5.jsonTask.HttpConnection;
import com.example.pushin_v5.dataModel.DataMaterials;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class LectureMaterialsFragment extends Fragment {
    private static Bundle bundle;
    private int userId = 0;
    private HttpConnection httpConnection;
    private String jsonstring;
    private ParseJson pj = new ParseJson();
    private ArrayList<DataMaterials> materialList;
    private RecyclerView list;
    private MaterialsAdapter adapter;
    RecyclerView.LayoutManager manager;

    public static LectureMaterialsFragment newInstance(Bundle b) {
        LectureMaterialsFragment fragment = new LectureMaterialsFragment();
        bundle = b;
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_lecture_materials, container, false);

        //pass parameters
        if(bundle != null){
            userId = bundle.getInt("userId");
        }

        //add data in to the list
        addItemToArray(root);

        //set adapter
        manager = new LinearLayoutManager(getActivity());
        MaterialsAdapter.RecyclerViewClickListener listener = (view, position) -> {
            //get the module which is clicked
            DataMaterials material = materialList.get(position);
            int moduleId = material.getModuleId();
        };
        adapter = new MaterialsAdapter(userId, getActivity(), materialList, (MaterialsAdapter.RecyclerViewClickListener) listener);
        list = root.findViewById(R.id.l_lecture_materials);
        list.setHasFixedSize(true);
        list.setLayoutManager(manager);
        list.setItemAnimator(new DefaultItemAnimator());
        list.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        list.setAdapter(adapter);

        return root;
    }

    private void addItemToArray(View root){
        //get data by sending request to the server
        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/lectureMaterialsList.php";
        try {
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null && pj.identification(jsonstring)) {
                final JSONArray topicArray = new JSONObject(jsonstring).getJSONArray("module");
                materialList = new ArrayList<>();
                for(int i = 0; i < topicArray.length(); i++){
                    JSONObject module = topicArray.getJSONObject(i);
                    materialList.add(pj.displaymaterials(module));
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}