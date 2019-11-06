package com.parthjadav.nfcreaderexample.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parthjadav.nfcreaderexample.R;
import com.parthjadav.nfcreaderexample.database.log.Log;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    Context context;
    List<Log> logList;

    public HistoryAdapter(Context context, List<Log> logList) {
        this.context = context;
        this.logList = logList;
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {

        holder.tvTag.setText(logList.get(position).getLogTag());
        holder.tvTagTime.setText(logList.get(position).getCreatedDate() + " " + logList.get(position).getCreatedTime());
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvTagTime)
        TextView tvTagTime;
        @BindView(R.id.tvTag)
        TextView tvTag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }
}
