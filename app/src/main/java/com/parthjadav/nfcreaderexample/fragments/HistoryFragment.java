package com.parthjadav.nfcreaderexample.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.parthjadav.nfcreaderexample.R;
import com.parthjadav.nfcreaderexample.adapter.HistoryAdapter;
import com.parthjadav.nfcreaderexample.database.DatabaseClient;
import com.parthjadav.nfcreaderexample.database.log.Log;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends BottomSheetDialogFragment {

    @BindView(R.id.recyclerViewHistory)
    RecyclerView recyclerViewHistory;

    public HistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        recyclerViewHistory.setHasFixedSize(true);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewHistory.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        getLogs();
    }

    private void getLogs() {
        class Logs extends AsyncTask<Void, Void, List<Log>> {

            @Override
            protected List<Log> doInBackground(Void... voids) {
                List<Log> allLogs = DatabaseClient
                        .getInstance(getActivity())
                        .getAppDatabase()
                        .logDao()
                        .getAllLogs();
                return allLogs;
            }

            @Override
            protected void onPostExecute(List<Log> logList) {
                super.onPostExecute(logList);
                recyclerViewHistory.setAdapter(new HistoryAdapter(getContext(), logList));
            }
        }

        Logs gt = new Logs();
        gt.execute();
    }

    @OnClick(R.id.cancel)
    void cancel(){
        dismiss();
    }
}
