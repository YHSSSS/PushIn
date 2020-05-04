package com.example.pushin_v5.interfaceFragment.review;
/***********************************************************************
 This file is a review page providing two recycle list including topic
 and flashcard list for users to review and removing the items that they
 have remember
 ***********************************************************************/

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushin_v5.R;
import com.example.pushin_v5.dataModel.DataFlashcard;
import com.example.pushin_v5.dataModel.DataTopic;
import com.example.pushin_v5.inputTask.GetNameSpinner;
import com.example.pushin_v5.inputTask.RecyclerItemTouchHelperListener;
import com.example.pushin_v5.jsonTask.HttpConnection;
import com.example.pushin_v5.jsonTask.ParseJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ReviewFragment extends Fragment implements RecyclerItemTouchHelperListener {

    //variables for http connection and parsing json
    private static Bundle bundle;
    private int userId = 0, moduleId = 0, topicId = 0;
    private HttpConnection httpConnection;
    private String jsonstring, jsonstring1;
    private ParseJson pj = new ParseJson();

    private RecyclerView flashcardlist, topiclist;
    private RecyclerView.LayoutManager flashcardManager, topicManager;
    private ReviewFlashcardAdapter flashcardAdapter;
    private ReviewTopicAdapter topicAdapter;
    private ArrayList<DataFlashcard> flashcardList;
    private ArrayList<DataTopic> topicList;
    private TextView t_empty_topic, t_empty_flashcard;

    public static ReviewFragment newInstance(Bundle b) {
        ReviewFragment fragment = new ReviewFragment();
        bundle = b;
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_review, container, false);

        //pass parameters
        if (bundle != null) {
            userId = bundle.getInt("userId");
        }

        t_empty_topic = root.findViewById(R.id.t_empty_review_topic);
        t_empty_flashcard = root.findViewById(R.id.t_empty_review_flashcard);

        RelativeLayout review = root.findViewById(R.id.layout_review);
        review.setVisibility(View.GONE);

        //get name or title of those exam and topics
        Spinner s_module = root.findViewById(R.id.s_review_exam);
        List<Integer> moduleIdList;

        GetNameSpinner getNameSpinner = new GetNameSpinner(getContext());
        moduleIdList = getNameSpinner.module(userId, s_module);

        //get name of the exam
        s_module.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                moduleId = moduleIdList.get(position);
                //add data
                if (moduleId != 0) {
                    addItemToArray(root, moduleId);
                    review.setVisibility(View.VISIBLE);
                } else {
                    review.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return root;
    }

    private void addItemToArray(View root, int moduleId) {
        topicList = new ArrayList<>();
        flashcardList = new ArrayList<>();
        //get topic id list from database
        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/topicList.php?id=" + moduleId;
        try {
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null && pj.identification(jsonstring)) {
                JSONArray topicArray = new JSONObject(jsonstring).getJSONArray("topic");
                for (int i = 0; i < topicArray.length(); i++) {
                    //parse data and insert into array list
                    JSONObject topic = topicArray.getJSONObject(i);
                    topicId = topic.getInt("topicId");
                    //check if the item is marked
                    if (topic.getInt("isBookmark") == 1) topicList.add(pj.displaytopic(topic));

                    //get flashcard list from database
                    String tempUrl1 = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/flashcardList.php?id=" + topicId;
                    httpConnection = new HttpConnection();
                    jsonstring1 = httpConnection.execute(tempUrl1).get();
                    if (jsonstring1 != null && pj.identification(jsonstring1)) {
                        JSONArray flashcardArray = new JSONObject(jsonstring1).getJSONArray("flashcard");
                        for (int j = 0; j < flashcardArray.length(); j++) {
                            JSONObject flashcard = flashcardArray.getJSONObject(j);
                            if (flashcard.getInt("isBookmark") == 1)
                                flashcardList.add(pj.displayflashcard(flashcard));
                        }
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

        //check if the list is empty
        if (topicList.size() == 0) t_empty_topic.setVisibility(View.VISIBLE);
        else t_empty_topic.setVisibility(View.GONE);
        if (flashcardList.size() == 0) t_empty_flashcard.setVisibility(View.VISIBLE);
        else t_empty_flashcard.setVisibility(View.GONE);

        //set topic adapter
        topicManager = new GridLayoutManager(getActivity(), 1, GridLayoutManager.HORIZONTAL, false);
        ReviewTopicAdapter.RecyclerViewClickListener topicListener = (view, position) -> {
            DataTopic topic = topicList.get(position);
            int topicId = topic.getTopicId();

            //set the focus item of the flashcard list
            for (int k = 0; k < flashcardList.size(); k++) {
                DataFlashcard flashcard = flashcardList.get(k);
                if (topicId == flashcard.getTopicId()) {
                    flashcardlist.scrollToPosition(k);
                    break;
                }
            }
            topiclist.scrollToPosition(position);
        };
        topicAdapter = new ReviewTopicAdapter(getContext(), topicList, topicListener);
        topiclist = root.findViewById(R.id.l_review_topic_list);
        topiclist.setHasFixedSize(true);
        topiclist.setLayoutManager(topicManager);
        topiclist.setItemAnimator(new DefaultItemAnimator());
        topiclist.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL));
        topiclist.setAdapter(topicAdapter);
        topiclist.scrollToPosition(0);

        //set flashcard adapter
        flashcardManager = new GridLayoutManager(getActivity(), 1, GridLayoutManager.HORIZONTAL, false);
        flashcardAdapter = new ReviewFlashcardAdapter(getActivity(), flashcardList);
        flashcardlist = root.findViewById(R.id.l_review_flashcard_list);
        flashcardlist.setHasFixedSize(true);
        flashcardlist.setLayoutManager(flashcardManager);
        flashcardlist.setItemAnimator(new DefaultItemAnimator());
        flashcardlist.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL));
        flashcardlist.setAdapter(flashcardAdapter);

        //item swipe to delete
        ItemTouchHelper.SimpleCallback flashcardItemTouchHelperCallBack =
                new ReviewFlashcardRecyclerItemTouchHelper(0, ItemTouchHelper.DOWN, this);
        new ItemTouchHelper(flashcardItemTouchHelperCallBack).attachToRecyclerView(flashcardlist);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        //not long see again
        int flashcardId = flashcardList.get(position).getFlashcardId();
        //unmark the flashcard
        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/flashcardUpdateBookmark.php?id=" + flashcardId + "&isBookmark=0";
        try {
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null) {
                if (pj.identification(jsonstring)) {
                    flashcardAdapter.notifyItemRemoved(position);
                    flashcardList.remove(position);
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
