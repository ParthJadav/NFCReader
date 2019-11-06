package com.parthjadav.nfcreaderexample.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.parthjadav.nfcreaderexample.R;
import com.parthjadav.nfcreaderexample.database.DatabaseClient;
import com.parthjadav.nfcreaderexample.database.tag.ScannerTag;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfigureTagFragment extends BottomSheetDialogFragment {


    @BindView(R.id.edtTag1)
    EditText edtTag1;
    @BindView(R.id.edtTag2)
    EditText edtTag2;
    @BindView(R.id.edtTag3)
    EditText edtTag3;
    @BindView(R.id.btnUpdateTags)
    AppCompatButton btnUpdateTags;
    @BindView(R.id.btnAddTags)
    AppCompatButton btnAddTags;

    int tId1,tId2,tId3;

    private OnDataSave onClickListener = null;

    public ConfigureTagFragment() {
        // Required empty public constructor
    }

    public void onDataSaveListeners(OnDataSave onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnDataSave {
        void onDataSave(boolean isSave);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_configure_tag, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        getTasks();
    }

    @OnClick(R.id.btnAddTags)
    void btnAddTags() {
        if (edtTag1.getText().toString().isEmpty()) {
            edtTag1.requestFocus();
            edtTag1.setError("Please add TAG 1");
        } else if (edtTag2.getText().toString().isEmpty()) {
            edtTag2.requestFocus();
            edtTag2.setError("Please add TAG 2");
        } else if (edtTag3.getText().toString().isEmpty()) {
            edtTag3.requestFocus();
            edtTag3.setError("Please add TAG 3");
        } else {
            insertTag(edtTag1.getText().toString());
            insertTag(edtTag2.getText().toString());
            insertTag(edtTag3.getText().toString());
        }
    }

    @OnClick(R.id.btnUpdateTags)
    void btnUpdateTags() {
        if (edtTag1.getText().toString().isEmpty()) {
            edtTag1.requestFocus();
            edtTag1.setError("Please add TAG 1");
        } else if (edtTag2.getText().toString().isEmpty()) {
            edtTag2.requestFocus();
            edtTag2.setError("Please add TAG 2");
        } else if (edtTag3.getText().toString().isEmpty()) {
            edtTag3.requestFocus();
            edtTag3.setError("Please add TAG 3");
        } else {
            updateTag(edtTag1.getText().toString(),tId1);
            updateTag(edtTag2.getText().toString(),tId2);
            updateTag(edtTag3.getText().toString(),tId3);
        }
    }

    private void insertTag(String tag) {

        class SaveTag extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {

                //creating a task
                ScannerTag nfcTag = new ScannerTag();
                nfcTag.setTagName(tag);

                //adding to database
                DatabaseClient.getInstance(getContext()).getAppDatabase()
                        .tagDao()
                        .insertTag(nfcTag);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                dismiss();
                onClickListener.onDataSave(true);
            }
        }

        SaveTag saveTag = new SaveTag();
        saveTag.execute();
    }

    private void updateTag(String tag,int id) {

        class SaveTag extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {

                //creating a task
                ScannerTag nfcTag = new ScannerTag();
                nfcTag.setTagName(tag);

                //adding to database
                DatabaseClient.getInstance(getContext()).getAppDatabase()
                        .tagDao()
                        .updateTag(tag,id);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                dismiss();
                onClickListener.onDataSave(true);
            }
        }

        SaveTag saveTag = new SaveTag();
        saveTag.execute();
    }

    private void getTasks() {
        class GetTasks extends AsyncTask<Void, Void, List<ScannerTag>> {

            @Override
            protected List<ScannerTag> doInBackground(Void... voids) {
                List<ScannerTag> taskList = DatabaseClient
                        .getInstance(getContext())
                        .getAppDatabase()
                        .tagDao()
                        .getAllTags();
                return taskList;
            }

            @Override
            protected void onPostExecute(List<ScannerTag> nfcTags) {
                super.onPostExecute(nfcTags);

                if (nfcTags.size() > 0) {
                    btnUpdateTags.setVisibility(View.VISIBLE);
                    btnAddTags.setVisibility(View.GONE);
                    edtTag1.setText(nfcTags.get(0).getTagName());
                    edtTag2.setText(nfcTags.get(1).getTagName());
                    edtTag3.setText(nfcTags.get(2).getTagName());
                    tId1 = nfcTags.get(0).getId();
                    tId2 = nfcTags.get(1).getId();
                    tId3 = nfcTags.get(2).getId();
                }else{
                    btnUpdateTags.setVisibility(View.GONE);
                    btnAddTags.setVisibility(View.VISIBLE);
                }
            }
        }

        GetTasks gt = new GetTasks();
        gt.execute();
    }
}
