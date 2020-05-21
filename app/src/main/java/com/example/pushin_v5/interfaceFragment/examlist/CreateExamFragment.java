package com.example.pushin_v5.interfaceFragment.examlist;
/***********************************************************************
 This file is used to create a new exam with an input field needed to
 be inputted and two buttons needed to be clicked then pick a date and
 time.
 ***********************************************************************/

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.pushin_v5.R;
import com.example.pushin_v5.inputTask.CheckStringQuotation;
import com.example.pushin_v5.jsonTask.HttpConnection;
import com.example.pushin_v5.jsonTask.ParseJson;

import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class CreateExamFragment extends Fragment {
    private int userId = 0;
    private HttpConnection httpConnection;
    private String jsonstring;
    private ParseJson pj = new ParseJson();

    //variables for getting date and time of exam
    private int s_day = 0;
    private int s_month = 0;
    private int s_year = 0;
    private int s_hour = 0;
    private int s_minute = 0;
    private String displaytime = "";
    private String displaydate = "";

    //dialogs for getting data and time
    private DatePickerDialog dpd;
    private TimePickerDialog tpd;
    private Calendar c;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_create_exam, container, false);
        //set the title of this page
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.create_examTitle);
        userId = (int) getActivity().getIntent().getSerializableExtra("userId");

        Button b_examDate, b_examTime, b_confirm;

        TextView t_examDate, t_examTime, t_count;
        EditText i_moduleName;
        //initialize the components of layout
        b_examDate = root.findViewById(R.id.b_selectExamDate);
        b_examTime = root.findViewById(R.id.b_selectExamTime);
        b_confirm = root.findViewById(R.id.b_moduleConfirm);
        t_examDate = root.findViewById(R.id.t_selectExamDate);
        t_examTime = root.findViewById(R.id.t_selectExamTime);
        t_count = root.findViewById(R.id.t_create_count_exam_name);
        i_moduleName = root.findViewById(R.id.i_create_moduleName);

        //counting the length of input text
        i_moduleName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                t_count.setText(i_moduleName.getText().toString().length() + "/60");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //clicking the button to get the date of the exam
        b_examDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dpd = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        displaydate = year + "-" + (month + 1) + "-" + dayOfMonth;
                        t_examDate.setText(displaydate);
                        s_day = dayOfMonth;
                        s_month = month + 1;
                        s_year = year;
                    }
                }, s_day, s_month, s_year);
                c = Calendar.getInstance();
                DatePicker dp = dpd.getDatePicker();

                //set the date can be picked after the manipulating date
                dp.setMinDate(c.getTimeInMillis());
                dpd.show();
            }
        });

        //clicking the button to get the time of the exam
        b_examTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                tpd = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        //make the time in custom display style
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

        //upload the info gotten form the page
        b_confirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //check if the input field is empty
                if (TextUtils.isEmpty(i_moduleName.getText().toString())) {
                    Toast.makeText(getActivity(), "Please enter the name or subject of the exam!",
                            Toast.LENGTH_SHORT).show();
                } //check if the date of the exam is picked
                else if (s_day == 0 || s_month == 0 || s_year == 0) {
                    Toast.makeText(getActivity(), "Please pick a date!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    //get the input text and check if the text contains any marks
                    String moduleName = i_moduleName.getText().toString();
                    moduleName = CheckStringQuotation.quotation(moduleName);

                    //send a request to the server to create a new exam
                    String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/moduleInsertion.php?" +
                            "name=" + moduleName + "&date=" + displaydate
                            + "&time=" + displaytime + "&uid=" + userId;
                    try {
                        httpConnection = new HttpConnection();
                        jsonstring = httpConnection.execute(tempUrl).get();
                        if (jsonstring != null && pj.identification(jsonstring)) {
                            //created successfully and jump the page to exam list page
                            Toast.makeText(getActivity(), "Create successfully!", Toast.LENGTH_SHORT).show();
                            ExamListFragment examList = new ExamListFragment();
                            Bundle bundle = new Bundle();
                            bundle.putInt("userId", userId);
                            examList.newInstance(bundle);
                            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                            navController.navigate(R.id.nav_examlist);
                        } else {
                            Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return root;
    }
}