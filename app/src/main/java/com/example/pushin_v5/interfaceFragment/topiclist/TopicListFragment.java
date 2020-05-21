package com.example.pushin_v5.interfaceFragment.topiclist;
/***********************************************************************
 This file is topic list page containing a topic recycler list using
 data topic model. Also, an editing topic window is floating on the
 top the screen provided users to edit the exam information. The
 window is allowed to be collapsed and expanded.
 ***********************************************************************/

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
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
import com.example.pushin_v5.dataModel.DataTopic;
import com.example.pushin_v5.inputTask.CheckStringQuotation;
import com.example.pushin_v5.inputTask.RecyclerItemTouchHelperListener;
import com.example.pushin_v5.interfaceFragment.flashcardlist.FlashcardListFragment;
import com.example.pushin_v5.jsonTask.HttpConnection;
import com.example.pushin_v5.jsonTask.ParseJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.ExecutionException;


public class TopicListFragment extends Fragment implements RecyclerItemTouchHelperListener {
    //receive data from other fragment
    private static Bundle bundle;
    private int moduleId = 0, userId = 0;

    //variables for http connection and parsing json
    private HttpConnection httpConnection;
    private String jsonstring;
    private ParseJson pj = new ParseJson();
    private ArrayList<DataTopic> topicList;

    private RecyclerView list;
    private TopicAdapter adapter;
    private RecyclerView.LayoutManager manager;

    //variables for getting data about the time and date of the exam
    private int s_day = 0;
    private int s_month = 0;
    private int s_year = 0;
    private int s_hour = 0;
    private int s_minute = 0;
    private String displaytime = "";
    private String displaydate = "";

    private ImageButton b_expand;
    private DatePickerDialog dpd;
    private TimePickerDialog tpd;
    private Calendar c;

    //expand & collapse
    private RelativeLayout editExamLayout;
    private int minHeight = 0, maxHeight = 0;
    private boolean isExpandingMore = false;

    public static TopicListFragment newInstance(Bundle b) {
        TopicListFragment fragment = new TopicListFragment();
        bundle = b;
        return fragment;
    }

    @SuppressLint("WrongViewCast")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_topiclist, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.topiclist);

        //pass parameters
        if (bundle != null) {
            moduleId = bundle.getInt("moduleId");
            userId = bundle.getInt("userId");
        }

        //set edit exam
        editModule(root, moduleId);

        //add date in to array
        addItemToArray(root);

        //set adapter
        manager = new LinearLayoutManager(getActivity());
        TopicAdapter.RecyclerViewClickListener listener = (view, position) -> {
            DataTopic topic = topicList.get(position);
            int topicId = topic.getTopicId();

            //pass the parameter and navigate to topic fragment
            Bundle b = new Bundle();
            b.putInt("userId", userId);
            b.putInt("moduleId", moduleId);
            b.putInt("topicId", topicId);
            FlashcardListFragment flashcard = new FlashcardListFragment();
            flashcard.newInstance(b);
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.nav_flashcardlist);
        };
        adapter = new TopicAdapter(getActivity(), topicList, listener);
        list = root.findViewById(R.id.l_topiclist);
        list.setHasFixedSize(true);
        list.setLayoutManager(manager);
        list.setItemAnimator(new DefaultItemAnimator());
        list.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        list.setAdapter(adapter);

        //item swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallBack =
                new TopicRecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallBack).attachToRecyclerView(list);

        //item drag and target
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback
                (ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder
                    dragged, @NonNull RecyclerView.ViewHolder target) {
                int position_dragged = dragged.getAdapterPosition();
                int position_target = target.getAdapterPosition();
                Collections.swap(topicList, position_dragged, position_target);
                adapter.notifyItemMoved(position_dragged, position_target);
                if (position_dragged != position_target) {
                    //get data model
                    DataTopic topicDragged = topicList.get(position_dragged);
                    DataTopic topicTarget = topicList.get(position_target);
                    //get topic id
                    int target_topicId = topicTarget.getTopicId();
                    //get order
                    int dragged_order = topicDragged.getTopicOrder();
                    //get moduleId
                    int moduleId = topicDragged.getModuleId();
                    //update order
                    String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/" +
                            "topicUpdateOrder.php?targetId=" + target_topicId + "&draggedOrder=" + dragged_order
                            + "&moduleId=" + moduleId;
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
        TextView empty = root.findViewById(R.id.t_empty_topic);
        //get data by sending request to the server
        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/topicList.php?id=" + moduleId;
        try {
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null && pj.identification(jsonstring)) {
                //get json array
                JSONArray topicArray = new JSONObject(jsonstring).getJSONArray("topic");
                topicList = new ArrayList<>();
                for (int i = 0; i < topicArray.length(); i++) {
                    //parse data and insert into array list
                    JSONObject topic = topicArray.getJSONObject(i);
                    topicList.add(pj.displaytopic(topic));
                }
                empty.setText("");
            } else {
                empty.setText("Click the button at the bottom to create a new topic");
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
        if (viewHolder instanceof TopicAdapter.ViewHolder) {
            int deleteIndex = viewHolder.getAdapterPosition();
            String title = topicList.get(deleteIndex).getTopicTitle();
            AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
            ad.setCancelable(false);
            ad.setTitle(title);
            ad.setMessage("Confirm to delete this topic?");
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

    public void groupTime() {
        if (s_minute >= 0 && s_minute < 10) {
            if (s_hour >= 0 && s_hour < 10) {
                displaytime = "0" + s_hour + ":0" + s_minute;
            } else {
                displaytime = s_hour + ":0" + s_minute;
            }
        } else {
            if (s_hour >= 0 && s_hour < 10) {
                displaytime = "0" + s_hour + ":" + s_minute;
            } else {
                displaytime = s_hour + ":" + s_minute;
            }
        }
    }

    public void convertDateStringToInt(String dateString) {
        String[] date = dateString.split("-");
        s_year = Integer.parseInt(date[0]);
        s_month = Integer.parseInt(date[1]);
        s_day = Integer.parseInt(date[2]);
        displaydate = s_year + "-" + s_month + "-" + s_day;
    }

    public void converTimeStringToInt(String timeString) {
        String[] time = timeString.split(":");
        s_hour = Integer.parseInt(time[0]);
        s_minute = Integer.parseInt(time[1]);
        groupTime();
    }

    //edting exam window
    public void editModule(View root, int moduleId) {
        //initialize components of the window layout
        TextView t_examDate, t_examTime, t_count_name;
        EditText i_moduleName;
        Button b_examDate, b_examTime, b_save;
        b_examDate = root.findViewById(R.id.b_selectExamDate);
        b_examTime = root.findViewById(R.id.b_selectExamTime);
        b_save = root.findViewById(R.id.b_moduleConfirm);
        t_examDate = root.findViewById(R.id.t_selectExamDate);
        t_examTime = root.findViewById(R.id.t_selectExamTime);
        t_count_name = root.findViewById(R.id.t_count_exam_name);
        i_moduleName = root.findViewById(R.id.i_moduleName);
        editExamLayout = root.findViewById(R.id.layout_edit_exam);
        b_expand = root.findViewById(R.id.b_topic_expand);

        //get current exam information
        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/moduleSingle.php?id=" + moduleId;
        try {
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null && pj.identification(jsonstring)) {
                JSONObject module = new JSONObject(jsonstring).getJSONObject("module");
                t_examDate.setText(module.getString("moduleExamDate"));
                convertDateStringToInt(module.getString("moduleExamDate"));
                t_examTime.setText(module.getString("moduleExamTime"));
                converTimeStringToInt(module.getString("moduleExamTime"));
                i_moduleName.setText(module.getString("moduleName"));
                t_count_name.setText(i_moduleName.getText().toString().length() + "/60");
            } else {
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //check if the description is changed
        i_moduleName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                t_count_name.setText(i_moduleName.getText().toString().length() + "/30");
                maxHeight = editExamLayout.getHeight();
            }

            @Override
            public void afterTextChanged(Editable s) {
                maxHeight = editExamLayout.getHeight();
            }
        });

        //pick exam date
        b_examDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dpd = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        s_day = dayOfMonth;
                        s_month = month + 1;
                        s_year = year;
                        displaydate = s_year + "-" + s_month + "-" + s_day;
                        t_examDate.setText(displaydate);
                    }
                }, s_day, s_month, s_year);
                c = Calendar.getInstance();
                DatePicker dp = dpd.getDatePicker();
                dp.setMinDate(c.getTimeInMillis());
                dpd.show();
            }
        });

        //pick exam time
        b_examTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                tpd = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (minute >= 0 && minute < 10) {
                            if (hourOfDay >= 0 && hourOfDay < 10) {
                                displaytime = "0" + hourOfDay + ":0" + minute;
                                t_examTime.setText(displaytime + " AM");
                            } else {
                                displaytime = hourOfDay + ":0" + minute;
                                t_examTime.setText(displaytime + " PM");
                            }
                        } else {
                            if (hourOfDay >= 0 && hourOfDay < 10) {
                                displaytime = "0" + hourOfDay + ":" + minute;
                                t_examTime.setText(displaytime + " AM");
                            } else {
                                displaytime = hourOfDay + ":" + minute;
                                t_examTime.setText(displaytime + " PM");
                            }
                        }
                        s_hour = hourOfDay;
                        s_minute = minute;
                    }
                }, 0, 0, false);
                tpd.show();
            }
        });

        //confirm to save
        b_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (TextUtils.isEmpty(i_moduleName.getText().toString())) {
                    Toast.makeText(getContext(), "Please enter module name!", Toast.LENGTH_SHORT).show();
                } else if (s_day == 0 || s_month == 0 || s_year == 0) {
                    Toast.makeText(getContext(), "Please pick a date!", Toast.LENGTH_SHORT).show();
                } else {
                    String moduleName = i_moduleName.getText().toString();
                    moduleName = CheckStringQuotation.quotation(moduleName);
                    String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/moduleInfoUpdate.php?" +
                            "name=" + moduleName + "&date=" + displaydate
                            + "&time=" + displaytime + "&mid=" + moduleId;
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

        //initialize the config of collapsing and expanding window
        editExamLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public boolean onPreDraw() {
                editExamLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                ViewGroup.LayoutParams layoutParams = editExamLayout.getLayoutParams();
                minHeight = 1;
                maxHeight = editExamLayout.getHeight();
                editExamLayout.setLayoutParams(layoutParams);

                //make the window is collapsed initially
                collapseView();
                b_expand.setBackground(getResources().getDrawable(R.drawable.ic_expand_more_24dp));

                return true;
            }
        });

        //press the button to collapse or expand
        b_expand.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                if (isExpandingMore) {
                    //collapse
                    collapseView();
                    b_expand.setBackground(getResources().getDrawable(R.drawable.ic_expand_more_24dp));
                    isExpandingMore = false;
                } else {
                    //expand
                    expandView(maxHeight);
                    b_expand.setBackground(getResources().getDrawable(R.drawable.ic_expand_less_24dp));
                    isExpandingMore = true;
                }
            }
        });
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

    //collapse the window
    public void collapseView() {
        ValueAnimator anim = ValueAnimator.ofInt(editExamLayout.getMeasuredHeightAndState(),
                minHeight);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = editExamLayout.getLayoutParams();
                layoutParams.height = val;
                editExamLayout.setLayoutParams(layoutParams);
            }
        });
        anim.start();
    }

    //expand the window
    public void expandView(int height) {
        ValueAnimator anim = ValueAnimator.ofInt(editExamLayout.getMeasuredHeightAndState(),
                height);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = editExamLayout.getLayoutParams();
                layoutParams.height = val;
                editExamLayout.setLayoutParams(layoutParams);
            }
        });
        anim.start();
    }

    /***************************************************************************************
     *   
     *    Copy end
     *
     ***************************************************************************************/

}
