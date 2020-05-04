package com.example.pushin_v5.interfaceFragment.examlist;
/***********************************************************************
 This file is the exam list page which includes a recycler list. The data
 in the list will be added here and set the custom adapter of the list.
 ***********************************************************************/

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushin_v5.R;
import com.example.pushin_v5.dataModel.DataExam;
import com.example.pushin_v5.inputTask.RecyclerItemTouchHelperListener;
import com.example.pushin_v5.interfaceFragment.topiclist.TopicListFragment;
import com.example.pushin_v5.jsonTask.HttpConnection;
import com.example.pushin_v5.jsonTask.ParseJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ExamListFragment extends Fragment implements RecyclerItemTouchHelperListener {

    private static Bundle bundle;
    private int userId = 0;
    private HttpConnection httpConnection;
    private String jsonstring;
    private ParseJson pj = new ParseJson();
    private ArrayList<DataExam> examList;
    private RecyclerView list;
    private ExamAdapter adapter;
    RecyclerView.LayoutManager manager;

    public static ExamListFragment newInstance(Bundle b) {
        ExamListFragment fragment = new ExamListFragment();
        bundle = b;
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_examlist, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.examlist);

        //pass parameters
        if (bundle != null) {
            userId = bundle.getInt("userId");
        }

        addItemToArray(root);

        //set adapter
        manager = new LinearLayoutManager(getActivity());
        ExamAdapter.RecyclerViewClickListener listener = (view, position) -> {
            //get the module which is clicked
            DataExam module = examList.get(position);
            int moduleId = module.getModuleId();

            //pass the parameter and navigate to topic fragment
            Bundle b = new Bundle();
            b.putInt("userId", userId);
            b.putInt("moduleId", moduleId);
            TopicListFragment topic = new TopicListFragment();
            topic.newInstance(b);
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.nav_topiclist);
        };
        adapter = new ExamAdapter(getActivity(), examList, listener);
        list = root.findViewById(R.id.l_examlist);
        list.setHasFixedSize(true);
        list.setLayoutManager(manager);
        list.setItemAnimator(new DefaultItemAnimator());
        list.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        list.setAdapter(adapter);

        //item swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallBack =
                new ExamRecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallBack).attachToRecyclerView(list);

        return root;
    }

    private void addItemToArray(View root) {
        TextView empty = root.findViewById(R.id.t_empty_exam);
        //get data of the list
        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/moduleList.php?id=" + userId;
        try {
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null && pj.identification(jsonstring)) {
                //get the array of the exam data
                final JSONArray moduleArray = new JSONObject(jsonstring).getJSONArray("module");
                examList = new ArrayList<>();
                for (int i = 0; i < moduleArray.length(); i++) {
                    //get the json object form the array and parse them into data model
                    JSONObject module = moduleArray.getJSONObject(i);
                    examList.add(pj.displaymodule(module));
                }
                empty.setText("");
            } else {
                empty.setText("Press the button at the bottom to create a new exam");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof ExamAdapter.ViewHolder) {
            //get the item that is going to be deleted
            DataExam module = examList.get(viewHolder.getAdapterPosition());
            String title = module.getModuleName();
            int deleteIndex = viewHolder.getAdapterPosition();
            //create a dialog to ask users for confirming deletion
            AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
            ad.setCancelable(false);
            ad.setTitle(title);
            ad.setMessage("Are you sure to delete this exam?");
            ad.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    adapter.removeItem(deleteIndex);
                }
            });
            ad.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            ad.show();
        }
    }
}