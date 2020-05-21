package com.example.pushin_v5.interfaceFragment.topiclist;
/***********************************************************************
 This file is a creating topic page for users to create a topic and insert
 into the topic table with filling the input field and picking the exam
 that the topic belongs to.
 ***********************************************************************/

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.pushin_v5.R;
import com.example.pushin_v5.inputTask.CheckStringQuotation;
import com.example.pushin_v5.inputTask.GetNameSpinner;
import com.example.pushin_v5.jsonTask.HttpConnection;
import com.example.pushin_v5.jsonTask.ParseJson;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class CreateTopicFragment extends Fragment {
    private int userId = 0, moduleId = 0;
    private HttpConnection httpConnection;
    private String jsonstring;
    private ParseJson pj = new ParseJson();

    private List<Integer> moduleIdList;
    private EditText i_createTopicName, i_createTopicDescription;
    private TextView t_topicDescriptionTextCount, t_topicNameTextCount;
    private Spinner s_topicModule;
    private Button b_save;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_create_topic, container, false);
        //set the title of the page
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.create_topicTitle);

        //get the userId
        userId = (int) getActivity().getIntent().getSerializableExtra("userId");

        //initialize the variablescomponentss
        s_topicModule = root.findViewById(R.id.s_topicModule);
        t_topicDescriptionTextCount = root.findViewById(R.id.t_topicDescriptionTextCount);
        t_topicNameTextCount = root.findViewById(R.id.t_topicNameTextCount);
        i_createTopicName = root.findViewById(R.id.i_createTopicName);
        i_createTopicDescription = root.findViewById(R.id.i_createTopicDescription);
        b_save = root.findViewById(R.id.b_createTopicConfirm);

        //get the length of the input text
        i_createTopicName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                t_topicNameTextCount.setText(i_createTopicName.getText().toString().length() + "/100");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        i_createTopicDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                t_topicDescriptionTextCount.setText(i_createTopicDescription.getText().toString().length() + "/1200");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //initialize spinner
        //get name or title of those exam and topics
        GetNameSpinner getNameSpinner = new GetNameSpinner(getContext());
        moduleIdList = getNameSpinner.module(userId, s_topicModule);
        //get name of the exam
        s_topicModule.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                moduleId = moduleIdList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        b_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //check if any input field is empty
                if (TextUtils.isEmpty(i_createTopicName.getText().toString())) {
                    Toast.makeText(getContext(), "Please enter name of the topic", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(i_createTopicDescription.getText().toString())) {
                    Toast.makeText(getContext(), "Please enter description of the topic", Toast.LENGTH_SHORT).show();
                } else if (moduleId == 0) {
                    Toast.makeText(getContext(), "Please pick an exam", Toast.LENGTH_SHORT).show();
                } else {
                    createTopic(root);
                }
            }
        });

        return root;
    }

    //save the data and send a request to update the table data
    public void createTopic(View root) {
        String topicTitle = i_createTopicName.getText().toString();
        String topicDetail = i_createTopicDescription.getText().toString();
        topicTitle = CheckStringQuotation.quotation(topicTitle);
        topicDetail = CheckStringQuotation.quotation(topicDetail);

        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/topicInsertion.php?mid=" + moduleId + "&title=" +
                topicTitle + "&detail=" + topicDetail;
        try {
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null) {
                if (pj.identification(jsonstring)) {
                    TopicListFragment examList = new TopicListFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("userId", userId);
                    bundle.putInt("moduleId", moduleId);
                    examList.newInstance(bundle);
                    NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.nav_topiclist);
                    Toast.makeText(getContext(), "Create Successfully", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}