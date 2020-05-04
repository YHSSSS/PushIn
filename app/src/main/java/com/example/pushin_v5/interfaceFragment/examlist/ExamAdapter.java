package com.example.pushin_v5.interfaceFragment.examlist;
/***********************************************************************
 This file is an adapter for the exam recycler list in the exam list page
 including any functions like removing item.
 ***********************************************************************/
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushin_v5.R;
import com.example.pushin_v5.dataModel.DataExam;
import com.example.pushin_v5.jsonTask.HttpConnection;
import com.example.pushin_v5.jsonTask.ParseJson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ViewHolder> {
    private ArrayList<DataExam> arr;
    private LayoutInflater inflater;
    private RecyclerViewClickListener mListener;
    private HttpConnection httpConnection = new HttpConnection();
    private String jsonstring;
    private ParseJson pj = new ParseJson();

    ExamAdapter(Context context, ArrayList<DataExam> object, RecyclerViewClickListener mListener) {
        this.inflater = LayoutInflater.from(context);
        this.arr = object;
        this.mListener = mListener;
    }

    @Override
    public ExamAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.style_exam, parent, false);
        return new ViewHolder(view, mListener);
    }

    //set the value in the calendar
    public Calendar toCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //get the data model using the position of this item
        DataExam module = arr.get(position);
        String moduleName = module.getModuleName();
        String moduleExamTime = module.getModuleExamTime();
        Date moduleExamDate = module.getModuleExamDate();
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd");

        //get the date difference
        long msDiff = toCalendar(moduleExamDate).getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
        long daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff) + 1;
        //if (daysDiff < 0) System.out.println("days error");

        //set the value of the components
        holder.t_moduleName.setText(String.valueOf(moduleName));
        holder.t_examCount.setText(String.valueOf(daysDiff));
        holder.t_ExamTime.setText(moduleExamTime+" on " + ft.format(moduleExamDate));
    }

    @Override
    public int getItemCount() {
        if (arr == null) return 0;
        else return arr.size();
    }

    public void removeItem(int position) {
        //get the item that is going to be deleted
        DataExam module = arr.get(position);
        int moduleId = module.getModuleId();

        //send a request to the server to delete this row in the table
        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/moduleDeletion.php?id=" + moduleId;
        try {
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null && pj.identification(jsonstring)) {
                //deleted successfully
                arr.remove(position);
                notifyItemRemoved(position);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView t_moduleName, t_examCount, t_ExamTime;
        public RelativeLayout viewBackground, viewForeground;
        private RecyclerViewClickListener mListener;

        ViewHolder(View itemView, RecyclerViewClickListener listener) {
            super(itemView);
            mListener = listener;
            t_moduleName = itemView.findViewById(R.id.t_moduleName);
            t_examCount = itemView.findViewById(R.id.t_examCount);
            t_ExamTime = itemView.findViewById(R.id.t_modulExamTime);
            viewBackground = itemView.findViewById(R.id.exam_view_background);
            viewForeground = itemView.findViewById(R.id.exam_view_foreground);
            itemView.setOnClickListener(this);
        }

        public void onClick(View view) {
            mListener.onModuleClick(view, getAdapterPosition());
        }
    }

    public interface RecyclerViewClickListener {
        void onModuleClick(View view, int position);
    }
}

