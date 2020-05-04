package com.example.pushin_v5.inputTask;
/***********************************************************************
 This file is used to provide the interface of recycler item touch helper
 listener
 ***********************************************************************/
import androidx.recyclerview.widget.RecyclerView;

public interface RecyclerItemTouchHelperListener {
    void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
}
