package com.example.pushin_v5.interfaceFragment.flashcardlist;
/***********************************************************************
 This file is a flashcard list page containing a flashcard recycler list
 using data flashcard model. Also, an editing topic window is floating
 on the top the screen provided users to edit the topic information. The
 window is allowed to be collapsed and expanded.
 ***********************************************************************/

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import com.example.pushin_v5.dataModel.DataFlashcard;
import com.example.pushin_v5.inputTask.CheckStringQuotation;
import com.example.pushin_v5.inputTask.GetNameSpinner;
import com.example.pushin_v5.inputTask.RecyclerItemTouchHelperListener;
import com.example.pushin_v5.jsonTask.HttpConnection;
import com.example.pushin_v5.jsonTask.ParseJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FlashcardListFragment extends Fragment implements RecyclerItemTouchHelperListener {
    //receive data from other fragment
    private static Bundle bundle;
    private int topicId = 0, moduleId = 0, userId = 0, updated_moduleId = 0;

    //variables for http connection and parsing json
    private HttpConnection httpConnection;
    private String jsonstring;
    private ParseJson pj = new ParseJson();
    private RecyclerView list;
    private RecyclerView.LayoutManager manager;
    private FlashcardAdapter adapter;
    private ArrayList<DataFlashcard> flashcardList;

    //flashcard list
    private RelativeLayout editTopicLayout;
    private List<Integer> moduleIdList;

    //expand & collapse
    private int minHeight = 0, maxHeight = 0;
    private boolean isExpandMore = true;

    public static FlashcardListFragment newInstance(Bundle b) {
        FlashcardListFragment fragment = new FlashcardListFragment();
        bundle = b;
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_flashcardlist, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.flashcardlist);

        //pass parameters
        if (bundle != null) {
            topicId = bundle.getInt("topicId");
            moduleId = bundle.getInt("moduleId");
            userId = bundle.getInt("userId");
        }

        //edit topic function
        editTopic(root);

        //add flashcard list
        addItemToArray(root);

        //set adapter
        manager = new LinearLayoutManager(getActivity());
        FlashcardAdapter.RecyclerViewClickListener listener = (view, position) -> {
            //get the flashcard which is clicked
            DataFlashcard flashcard = flashcardList.get(position);
            int flashcardId = flashcard.getFlashcardId();
            SingleFlashcardFragment singleFlashcardFragment = new SingleFlashcardFragment();
            Bundle b = new Bundle();
            b.putInt("userId", userId);
            b.putInt("moduleId", moduleId);
            b.putInt("flashcardId", flashcardId);
            b.putInt("topicId", topicId);
            singleFlashcardFragment.newInstance(b);
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.nav_flashcardSingle);

        };
        adapter = new FlashcardAdapter(getActivity(), flashcardList, listener);
        list = root.findViewById(R.id.l_flashcardlist);
        list.setHasFixedSize(true);
        list.setLayoutManager(manager);
        list.setItemAnimator(new DefaultItemAnimator());
        list.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        list.setAdapter(adapter);

        //item swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallBack =
                new FlashcardRecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallBack).attachToRecyclerView(list);

        //item drag and target
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback
                (ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder
                    dragged, @NonNull RecyclerView.ViewHolder target) {
                int position_dragged = dragged.getAdapterPosition();
                int position_target = target.getAdapterPosition();
                Collections.swap(flashcardList, position_dragged, position_target);
                adapter.notifyItemMoved(position_dragged, position_target);
                if (position_dragged != position_target) {
                    //get data model
                    DataFlashcard topicDragged = flashcardList.get(position_dragged);
                    DataFlashcard topicTarget = flashcardList.get(position_target);
                    //get topic id
                    int target_flashcardId = topicTarget.getFlashcardId();
                    //get order
                    int dragged_topicOrder = topicDragged.getFlashcardOrder();
                    //get topicId
                    int topicId = topicDragged.getTopicId();
                    //update order
                    String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/" +
                            "flashcardUpdateOrder.php?targetId=" + target_flashcardId + "&draggedOrder=" + dragged_topicOrder
                            + "&topicId=" + topicId;
                    try {
                        httpConnection = new HttpConnection();
                        jsonstring = httpConnection.execute(tempUrl).get();
                        if (jsonstring != null && pj.identification(jsonstring)) {
                            Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }
        });
        helper.attachToRecyclerView(list);

        return root;
    }

    private void addItemToArray(View root) {
        TextView empty = root.findViewById(R.id.t_empty_flashcard);
        //get data from database
        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/flashcardList.php?id=" + topicId;
        try {
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null && pj.identification(jsonstring)) {
                final JSONArray flashcardArray = new JSONObject(jsonstring).getJSONArray("flashcard");
                flashcardList = new ArrayList<>();
                for (int i = 0; i < flashcardArray.length(); i++) {
                    JSONObject flashcard = flashcardArray.getJSONObject(i);
                    flashcardList.add(pj.displayflashcard(flashcard));
                }
                empty.setText("");
            } else {
                empty.setText("Click the button at the bottom to create a new flashcard");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void editTopic(View root) {
        Spinner s_topic;
        Button b_save_topic;
        ImageButton b_expand;
        EditText i_topicTitle, i_topicDescription;
        TextView t_titleCount, t_detailCount;

        s_topic = root.findViewById(R.id.s_update_topic);
        i_topicTitle = root.findViewById(R.id.i_topicTitle);
        i_topicDescription = root.findViewById(R.id.i_topicDescription);
        t_detailCount = root.findViewById(R.id.t_editTopicDetailTextCount);
        t_titleCount = root.findViewById(R.id.t_editTopicTitleTextCount);
        b_expand = root.findViewById(R.id.b_flashcard_expand);
        editTopicLayout = root.findViewById(R.id.layout_edit_topic);
        b_save_topic = root.findViewById(R.id.b_topicConfirm);

        //initialize spinner
        //get name or title of those exam and topics
        GetNameSpinner getNameSpinner = new GetNameSpinner(getContext());
        moduleIdList = getNameSpinner.module(userId, s_topic);
        for (int i = 0; i < moduleIdList.size(); i++) {
            //set current topic
            if (moduleIdList.get(i) == moduleId) s_topic.setSelection(i);
        }
        //get name of the exam
        s_topic.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updated_moduleId = moduleIdList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //get the detail data of topic
        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/topicSingle.php?" + "id=" + topicId;
        try {
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null && pj.identification(jsonstring)) {
                JSONObject topic = new JSONObject(jsonstring).getJSONObject("topic");
                i_topicTitle.setText(topic.getString("topicTitle"));
                t_titleCount.setText(i_topicTitle.getText().toString().length() + "/100");
                i_topicDescription.setText(topic.getString("topicDetail"));
                t_detailCount.setText(i_topicDescription.getText().toString().length() + "/800");
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //check if the title is changed
        i_topicTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                t_titleCount.setText(i_topicTitle.getText().toString().length() + "/100");
                maxHeight = editTopicLayout.getHeight();
            }

            @Override
            public void afterTextChanged(Editable s) {
                maxHeight = editTopicLayout.getHeight();
            }
        });

        //check if the description is changed
        i_topicDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                t_detailCount.setText(i_topicDescription.getText().toString().length() + "/1200");
                maxHeight = editTopicLayout.getHeight();
            }

            @Override
            public void afterTextChanged(Editable s) {
                maxHeight = editTopicLayout.getHeight();
            }
        });

        //save the title and description to the database
        b_save_topic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //check if there is any empty
                if (TextUtils.isEmpty(i_topicTitle.getText().toString())) {
                    Toast.makeText(getContext(), "Please enter topic title!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(i_topicDescription.getText().toString())) {
                    Toast.makeText(getContext(), "Please enter topic description!", Toast.LENGTH_SHORT).show();
                } else if (updated_moduleId == 0) {
                    Toast.makeText(getContext(), "Please pick one exam!", Toast.LENGTH_SHORT).show();
                } else {
                    //check if change the exam belongs
                    if (moduleId != updated_moduleId) {
                        updatedModuleId();
                    }
                    //get the text of the edit text
                    String topicTitle = i_topicTitle.getText().toString();
                    topicTitle = CheckStringQuotation.quotation(topicTitle);
                    String topicDetail = i_topicDescription.getText().toString();
                    topicDetail = CheckStringQuotation.quotation(topicDetail);
                    //connect the php on the server to update the data
                    String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/topicInfoUpdate.php?" +
                            "tid=" + topicId +
                            "&topicTitle=" + topicTitle +
                            "&topicDetail=" + topicDetail +
                            "&mid=" + moduleId;
                    try {
                        httpConnection = new HttpConnection();
                        jsonstring = httpConnection.execute(tempUrl).get();
                        if (jsonstring != null && pj.identification(jsonstring)) {
                            Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        });

        //initialize the layout
        editTopicLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public boolean onPreDraw() {
                editTopicLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                ViewGroup.LayoutParams layoutParams = editTopicLayout.getLayoutParams();
                minHeight = 1;
                maxHeight = editTopicLayout.getHeight();
                editTopicLayout.setLayoutParams(layoutParams);

                //make the window is collapsed initially
                collapseView();
                b_expand.setBackground(getResources().getDrawable(R.drawable.ic_expand_more_24dp));

                return true;
            }
        });

        b_expand.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                if (isExpandMore) {
                    //collapse
                    collapseView();
                    b_expand.setBackground(getResources().getDrawable(R.drawable.ic_expand_more_24dp));
                    isExpandMore = false;
                } else {
                    //expand
                    expandView(maxHeight);
                    b_expand.setBackground(getResources().getDrawable(R.drawable.ic_expand_less_24dp));
                    isExpandMore = true;
                }
            }
        });
    }

    public void updatedModuleId() {
        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/moduleIdUpdate.php?mid=" +
                updated_moduleId + "&tid=" + topicId;
        try {
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null && pj.identification(jsonstring)) {
                Toast.makeText(getContext(), "Updated", Toast.LENGTH_SHORT).show();
                moduleId = updated_moduleId;
                updated_moduleId = 0;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /***************************************************************************************
     *   
     *    The following lines of code is copied from:
     *    Title: CardView: Expand & Collapse
     *    Author: Akshay S
     *    Date: Nov 14, 2015
     *    Code version: 1.0
     *    Availability: https://medium.com/@akshay.shinde/cardview-expand-collapse-cd10916bb77c
     *
     ***************************************************************************************/


    public void collapseView() {
        ValueAnimator anim = ValueAnimator.ofInt(editTopicLayout.getMeasuredHeightAndState(),
                minHeight);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = editTopicLayout.getLayoutParams();
                layoutParams.height = val;
                editTopicLayout.setLayoutParams(layoutParams);
            }
        });
        anim.start();
    }

    public void expandView(int height) {
        ValueAnimator anim = ValueAnimator.ofInt(editTopicLayout.getMeasuredHeightAndState(),
                height);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = editTopicLayout.getLayoutParams();
                layoutParams.height = val;
                editTopicLayout.setLayoutParams(layoutParams);
            }
        });
        anim.start();
    }

    /***************************************************************************************
     *   
     *    Copy end
     *
     ***************************************************************************************/

    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof FlashcardAdapter.ViewHolder) {
            int deleteIndex = viewHolder.getAdapterPosition();
            AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
            ad.setCancelable(false);
            ad.setMessage("Are you sure to delete this flashcard?");
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
