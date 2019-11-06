package com.parthjadav.nfcreaderexample.fragments;


import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.parthjadav.nfcreaderexample.R;
import com.parthjadav.nfcreaderexample.database.DatabaseClient;
import com.parthjadav.nfcreaderexample.database.log.Log;
import com.parthjadav.nfcreaderexample.nfc.NdefMessageParser;
import com.parthjadav.nfcreaderexample.nfc.ParsedNdefRecord;
import com.parthjadav.nfcreaderexample.database.tag.ScannerTag;
import com.parthjadav.nfcreaderexample.utils.Mediator;
import com.skyfishjy.library.RippleBackground;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    @BindView(R.id.tvTag1)
    TextView tvTag1;
    @BindView(R.id.tvTag2)
    TextView tvTag2;
    @BindView(R.id.tvTag3)
    TextView tvTag3;
    @BindView(R.id.content)
    RippleBackground content;

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;

    String tag1, tag2, tag3;

    boolean isTag1 = false;
    boolean isTag2 = false;
    boolean isTag3 = false;

    public static HomeFragment newInstance() {

        return new HomeFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        Mediator.homeFragment = HomeFragment.this;
        content.startRippleAnimation();
    }

    public void init() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(getContext());

        if (nfcAdapter == null) {
            Toast.makeText(getContext(), "No Scanner", Toast.LENGTH_SHORT).show();
            return;
        }

        pendingIntent = PendingIntent.getActivity(getContext(), 0,
                new Intent(getContext(), this.getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }


    private void resolveIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            Tag tag = null;

            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];

                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }

            } else {
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                byte[] payload = dumpTagData(tag).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                msgs = new NdefMessage[]{msg};
            }

            byte[] id = tag.getId();

            if (toHex(id).equals(tag1)) {
                isTag1 = true;
                tvTag1.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.materialGreen500));
            } else if (!toHex(id).equals(tag1) && !isTag1) {
                tvTag1.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.materialRed500));
                Toast.makeText(getContext(), "Please scan tag 1 first.", Toast.LENGTH_SHORT).show();
            } else if (toHex(id).equals(tag2) && isTag1) {
                isTag2 = true;
                tvTag2.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.materialGreen500));
            } else if (!toHex(id).equals(tag2) && !isTag2) {
                tvTag2.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.materialRed500));
                Toast.makeText(getContext(), "Please scan tag 2 first.", Toast.LENGTH_SHORT).show();
            } else if (toHex(id).equals(tag3) && isTag1 && isTag2) {
                isTag3 = true;
                tvTag3.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.materialGreen500));
            } else if (!toHex(id).equals(tag3) && !isTag3) {
                tvTag3.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.materialRed500));
            } else if (isTag1 && isTag2 && !isTag3 && !toHex(id).equals(tag3)) {
                tvTag3.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.materialRed500));
            }

            //displayMsgs(msgs);
        }
    }

    private void displayMsgs(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0)
            return;

        StringBuilder builder = new StringBuilder();
        List<ParsedNdefRecord> records = NdefMessageParser.parse(msgs[0]);
        final int size = records.size();

        for (int i = 0; i < size; i++) {
            ParsedNdefRecord record = records.get(i);
            String str = record.str();
            builder.append(str).append("\n");
        }

        //mTvMessage.setText(builder.toString());
    }


    private void showWirelessSettings() {
        Toast.makeText(getContext(), "You need to enable Scanner", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    private String dumpTagData(Tag tag) {
        StringBuilder sb = new StringBuilder();
        byte[] id = tag.getId();

        sb.append("ID (hex): ").append(toHex(id)).append('\n');
        sb.append("ID (reversed hex): ").append(toReversedHex(id)).append('\n');
        sb.append("ID (dec): ").append(toDec(id)).append('\n');
        sb.append("ID (reversed dec): ").append(toReversedDec(id)).append('\n');

        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(",\n");
        }

        sb.delete(sb.length() - 2, sb.length());

        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                String type = "Unknown";

                try {
                    MifareClassic mifareTag = MifareClassic.get(tag);

                    switch (mifareTag.getType()) {
                        case MifareClassic.TYPE_CLASSIC:
                            type = "Classic";
                            break;
                        case MifareClassic.TYPE_PLUS:
                            type = "Plus";
                            break;
                        case MifareClassic.TYPE_PRO:
                            type = "Pro";
                            break;
                    }
                    sb.append("Mifare Classic type: ");
                    sb.append(type);
                    sb.append('\n');

                    sb.append("Mifare size: ");
                    sb.append(mifareTag.getSize() + " bytes");
                    sb.append('\n');

                    sb.append("Mifare sectors: ");
                    sb.append(mifareTag.getSectorCount());
                    sb.append('\n');

                    sb.append("Mifare blocks: ");
                    sb.append(mifareTag.getBlockCount());
                } catch (Exception e) {
                    sb.append("Mifare classic error: " + e.getMessage());
                }
            }

            if (tech.equals(MifareUltralight.class.getName())) {
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        type = "Ultralight";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        type = "Ultralight C";
                        break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type);
            }
        }

        return sb.toString();
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String toReversedHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
        }
        return sb.toString();
    }

    private long toDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    private long toReversedDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    private String getDateTime() {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
            Date date = new Date();
            return dateFormat.format(date);
        } catch (Exception dateTime) {

        }
        return null;
    }

    private void saveLog(String tag) {

        class SaveLog extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {

                //creating a task
                Log log = new Log();
                log.setLogTag(tag);
                log.setCreatedDate(getDateTime().toUpperCase());

                //adding to database
                DatabaseClient.getInstance(getContext()).getAppDatabase()
                        .logDao()
                        .insertLog(log);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
            }
        }

        SaveLog saveLog = new SaveLog();
        saveLog.execute();
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
                tag1 = nfcTags.get(0).getTagName();
                tag2 = nfcTags.get(1).getTagName();
                tag3 = nfcTags.get(2).getTagName();

                tvTag1.setText(tag1);
                tvTag2.setText(tag2);
                tvTag3.setText(tag3);
            }
        }

        GetTasks gt = new GetTasks();
        gt.execute();
    }
}
