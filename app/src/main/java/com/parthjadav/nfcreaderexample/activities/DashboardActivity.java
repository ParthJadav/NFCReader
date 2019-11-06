package com.parthjadav.nfcreaderexample.activities;

import android.annotation.SuppressLint;
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
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.parthjadav.nfcreaderexample.PreferenceManager;
import com.parthjadav.nfcreaderexample.R;
import com.parthjadav.nfcreaderexample.database.DatabaseClient;
import com.parthjadav.nfcreaderexample.database.log.Log;
import com.parthjadav.nfcreaderexample.database.report.Report;
import com.parthjadav.nfcreaderexample.database.tag.ScannerTag;
import com.parthjadav.nfcreaderexample.fragments.ConfigureTagFragment;
import com.parthjadav.nfcreaderexample.fragments.HistoryFragment;
import com.parthjadav.nfcreaderexample.nfc.NdefMessageParser;
import com.parthjadav.nfcreaderexample.nfc.ParsedNdefRecord;
import com.skyfishjy.library.RippleBackground;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DashboardActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    public static final int RESULT_CODE_REPORT = 0;
    public FragmentTransaction transaction;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tvTag1)
    TextView tvTag1;
    @BindView(R.id.tvTag2)
    TextView tvTag2;
    @BindView(R.id.tvTag3)
    TextView tvTag3;
    @BindView(R.id.tvBusNumber)
    TextView tvBusNumber;
    @BindView(R.id.tvScannedTime)
    TextView tvScannedTime;
    @BindView(R.id.tvSkippedText)
    TextView tvSkippedText;
    @BindView(R.id.tvScanned)
    TextView tvScanned;
    @BindView(R.id.tvRow)
    TextView tvRow;
    @BindView(R.id.content)
    RippleBackground content;
    @BindView(R.id.imgVerifiedGreen)
    ImageView imgVerifiedGreen;
    @BindView(R.id.imgVerifiedOrange)
    ImageView imgVerifiedOrange;
    @BindView(R.id.skippedRow)
    LinearLayout skippedRow;
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    String tag1, tag2, tag3;
    List<String> errorTag;
    List<String> imagePaths;
    List<String> description;
    boolean isTag1 = false;
    boolean isTag2 = false;
    boolean isTag3 = false;
    String scannedTag, row;
    String scanTag1Time, scanTag2Time, scanTag3Time;

    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        preferenceManager = new PreferenceManager(this);
        content.startRippleAnimation();

        errorTag = new ArrayList<>();
        imagePaths = new ArrayList<>();
        description = new ArrayList<>();
        preferenceManager.setKeyValueBoolean("report", false);
        getTasks();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "No Scanner", Toast.LENGTH_SHORT).show();
            return;
        }

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        //loadFragments(new HomeFragment());

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @OnClick(R.id.menuHome)
    void menuHome() {
        drawerLayout.closeDrawer(GravityCompat.START);
        Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.menuConfigTags)
    void menuConfigTags() {
        drawerLayout.closeDrawer(GravityCompat.START);
        ConfigureTagFragment configureTagFragment = new ConfigureTagFragment();
        configureTagFragment.show(getSupportFragmentManager(), "Config Tags");
        configureTagFragment.onDataSaveListeners(new ConfigureTagFragment.OnDataSave() {
            @Override
            public void onDataSave(boolean isSave) {
                if (isSave) {
                    getTasks();
                }
            }
        });
    }

    @OnClick(R.id.menuHistory)
    void menuHistory() {
        drawerLayout.closeDrawer(GravityCompat.START);
        HistoryFragment historyFragment = new HistoryFragment();
        historyFragment.show(getSupportFragmentManager(), "History");
    }

    public void loadFragments(Fragment fragment) {

        String backStateName = fragment.getClass().getName();

        FragmentManager manager = getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        if (!fragmentPopped) { //fragment not in back stack, create it.
            //FragmentTransaction ft = manager.beginTransaction();
            transaction = manager.beginTransaction();
            //transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
            transaction.add(R.id.nav_host_fragment, fragment);
            //transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            transaction.addToBackStack(backStateName);
            transaction.commit();
        }
        drawerLayout.closeDrawers();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled())
                showWirelessSettings();

            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }

        if (preferenceManager.getKeyValueBoolean("report")) {
            Toast.makeText(this, "Return", Toast.LENGTH_SHORT).show();
            preferenceManager.setKeyValueBoolean("report", false);

            if (scannedTag == tag1) {
                isTag1 = true;
            } else if (scannedTag == tag2) {
                isTag2 = true;
            } else if (scannedTag == tag3) {
                isTag3 = true;
            }

            imgVerifiedGreen.setVisibility(View.INVISIBLE);
            imgVerifiedOrange.setVisibility(View.INVISIBLE);
            tvScanned.setVisibility(View.VISIBLE);
            skippedRow.setVisibility(View.GONE);

            tvRow.setText("");
            tvScannedTime.setText("");
            tvScanned.setText("Row(s) Not Scanned Yet");

            if (errorTag != null) {
                if (errorTag.size() > 0) {
                    if (!errorTag.contains(scannedTag)) {
                        errorTag.add(scannedTag);
                        imagePaths.add(preferenceManager.getKeyValueString("imagePath"));
                        description.add(preferenceManager.getKeyValueString("description"));
                    }
                } else {
                    errorTag.add(scannedTag);
                    imagePaths.add(preferenceManager.getKeyValueString("imagePath"));
                    description.add(preferenceManager.getKeyValueString("description"));
                }
            } else {
                errorTag.add(scannedTag);
                imagePaths.add(preferenceManager.getKeyValueString("imagePath"));
                description.add(preferenceManager.getKeyValueString("description"));
            }

            SaveLogForReport(scannedTag, preferenceManager.getKeyValueString("imagePath"), preferenceManager.getKeyValueString("description"));

            android.util.Log.e("*** Image Path - ", imagePaths.toString());
            android.util.Log.e("*** Description - ", description.toString());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        resolveIntent(intent);
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

            /*if (toHex(id).equals(tag1) && !isTag1) {
                isTag1 = true;
                tvTag1.setBackgroundColor(ContextCompat.getColor(this, R.color.materialGreen500));
                saveLog(toHex(id));
                scanTag1Time = toHex(id) + " Scanned at " + getDate() + " " + getTime().toUpperCase();
            } else if (!toHex(id).equals(tag1) && !isTag1) {
                tvTag1.setBackgroundColor(ContextCompat.getColor(this, R.color.materialRed500));
                Toast.makeText(this, "Please scan tag 1 first.", Toast.LENGTH_SHORT).show();
            } else if (toHex(id).equals(tag2) && isTag1 && !isTag2) {
                isTag2 = true;
                tvTag2.setBackgroundColor(ContextCompat.getColor(this, R.color.materialGreen500));
                saveLog(toHex(id));
                scanTag2Time = toHex(id) + " Scanned at " + getDate() + " " + getTime().toUpperCase();
            } else if (!toHex(id).equals(tag2) && !isTag2) {
                tvTag2.setBackgroundColor(ContextCompat.getColor(this, R.color.materialRed500));
                Toast.makeText(this, "Please scan tag 2 first.", Toast.LENGTH_SHORT).show();
            } else if (toHex(id).equals(tag3) && isTag1 && isTag2) {
                isTag3 = true;
                tvTag3.setBackgroundColor(ContextCompat.getColor(this, R.color.materialGreen500));
                saveLog(toHex(id));
                scanTag3Time = toHex(id) + " Scanned at " + getDate() + " " + getTime().toUpperCase();
                showCustomDialog();
            } else if (!toHex(id).equals(tag3) && !isTag3) {
                tvTag3.setBackgroundColor(ContextCompat.getColor(this, R.color.materialRed500));
                Toast.makeText(this, "Please scan tag 3.", Toast.LENGTH_SHORT).show();
            }*/

            android.util.Log.e("*** Scanned - ", toHex(id));

            if (toHex(id).equals(tag1) && !isTag1) {
                isTag1 = true;
                imgVerifiedGreen.setVisibility(View.VISIBLE);
                imgVerifiedOrange.setVisibility(View.INVISIBLE);
                skippedRow.setVisibility(View.GONE);
                tvScanned.setVisibility(View.VISIBLE);
                scanTag1Time = toHex(id) + " - Scanned at " + getDate() + " " + getTime().toUpperCase();
                tvScannedTime.setText(getDate() + " " + getTime().toUpperCase());
                tvScanned.setText("1 Row(s) Scanned");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvRow.setText("2");
                        tvScanned.setText("Please Scan row 2");
                    }
                }, 3000);
                tvRow.setText("1");
                saveLog(toHex(id), "");
            } else if (toHex(id).equals(tag2) && !isTag2 && isTag1) {
                isTag2 = true;
                tvScanned.setVisibility(View.VISIBLE);
                imgVerifiedGreen.setVisibility(View.VISIBLE);
                imgVerifiedOrange.setVisibility(View.INVISIBLE);
                skippedRow.setVisibility(View.GONE);
                scanTag2Time = toHex(id) + " - Scanned at " + getDate() + " " + getTime().toUpperCase();
                tvScannedTime.setText(getDate() + " " + getTime().toUpperCase());
                tvScanned.setText("2 Row(s) Scanned");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvRow.setText("3");
                        tvScanned.setText("Please Scan row 3");
                    }
                }, 3000);
                tvRow.setText("2");
                saveLog(toHex(id), "");
            } else if (toHex(id).equals(tag3) && !isTag3 && isTag1 && isTag2) {
                isTag3 = true;
                skippedRow.setVisibility(View.GONE);
                tvScanned.setVisibility(View.VISIBLE);
                imgVerifiedGreen.setVisibility(View.VISIBLE);
                imgVerifiedOrange.setVisibility(View.INVISIBLE);
                scanTag3Time = toHex(id) + " - Scanned at " + getDate() + " " + getTime().toUpperCase();
                tvScannedTime.setText(getDate() + " " + getTime().toUpperCase());
                tvScanned.setText("3 Row(s) Scanned");
                tvRow.setText("3");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvScanned.setText("All Row scanned");
                    }
                }, 2000);
                saveLog(toHex(id), "");
                showCustomDialog();
            } else if (!isTag1 && !isTag2 && toHex(id).equals(tag3)) {
                imgVerifiedGreen.setVisibility(View.INVISIBLE);
                imgVerifiedOrange.setVisibility(View.VISIBLE);
                tvScanned.setVisibility(View.GONE);
                skippedRow.setVisibility(View.VISIBLE);
                tvScannedTime.setText(getDate() + " " + getTime().toUpperCase());
                tvSkippedText.setText("You have skipped scanning row 1 & 2");
            } else if (!isTag1 && toHex(id).equals(tag2)) {
                imgVerifiedGreen.setVisibility(View.INVISIBLE);
                imgVerifiedOrange.setVisibility(View.VISIBLE);
                tvScanned.setVisibility(View.GONE);
                skippedRow.setVisibility(View.VISIBLE);
                tvScannedTime.setText(getDate() + " " + getTime().toUpperCase());
                scannedTag = tag1;
                row = "1";
                tvSkippedText.setText("You have skipped scanning row 1");
            } else if (!isTag2 && toHex(id).equals(tag3)) {
                imgVerifiedGreen.setVisibility(View.INVISIBLE);
                imgVerifiedOrange.setVisibility(View.VISIBLE);
                tvScanned.setVisibility(View.GONE);
                skippedRow.setVisibility(View.VISIBLE);
                tvScannedTime.setText(getDate() + " " + getTime().toUpperCase());
                scannedTag = tag2;
                row = "2";
                tvSkippedText.setText("You have skipped scanning row 2");
            }

            displayMsgs(msgs);
        }
    }

    @OnClick(R.id.tvOk)
    void tvOk() {
        imgVerifiedGreen.setVisibility(View.INVISIBLE);
        imgVerifiedOrange.setVisibility(View.INVISIBLE);
        tvScanned.setVisibility(View.VISIBLE);
        skippedRow.setVisibility(View.GONE);
    }

    @OnClick(R.id.tvReportProblem)
    void tvReportProblem() {
        Intent intent = new Intent(DashboardActivity.this, ReportProblemActivity.class);
        intent.putExtra("tag", scannedTag);
        intent.putExtra("row", row);
        startActivity(intent);
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

        android.util.Log.e("*** Data - ", builder.toString());
    }


    private void showWirelessSettings() {
        Toast.makeText(this, "You need to enable Scanner", Toast.LENGTH_SHORT).show();
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

    private String getDate() {
        try {
            @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            return dateFormat.format(date);
        } catch (Exception dateTime) {

        }
        return null;
    }

    private String getTime() {
        try {
            @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss a");
            Date date = new Date();
            return dateFormat.format(date);
        } catch (Exception dateTime) {

        }
        return null;
    }

    private void saveLog(String tag, String imagePath) {

        class SaveLog extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {

                //creating a task
                Log log = new Log();
                log.setLogTag(tag);
                log.setImagePath(imagePath);
                log.setCreatedDate(getDate().toUpperCase());
                log.setCreatedTime(getTime().toUpperCase());

                //adding to database
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .logDao()
                        .insertLog(log);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                getLogs();
                //Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
            }
        }

        SaveLog saveLog = new SaveLog();
        saveLog.execute();
    }

    private void SaveLogForReport(String tag, String imagePath, String description) {

        class SaveLogForReport extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {

                //creating a task
                Report report = new Report();
                report.setTagNo(tag);
                report.setImagePath(imagePath);
                report.setDescription(description);
                report.setCreatedDate(getDate().toUpperCase());
                report.setCreatedTime(getTime().toUpperCase());

                //adding to database
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .reportDao()
                        .insertReport(report);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                getLogs();
                //Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
            }
        }

        SaveLogForReport saveLogForReport = new SaveLogForReport();
        saveLogForReport.execute();
    }

    private void getTasks() {
        class GetTasks extends AsyncTask<Void, Void, List<ScannerTag>> {

            @Override
            protected List<ScannerTag> doInBackground(Void... voids) {
                List<ScannerTag> taskList = DatabaseClient
                        .getInstance(getApplicationContext())
                        .getAppDatabase()
                        .tagDao()
                        .getAllTags();
                return taskList;
            }

            @Override
            protected void onPostExecute(List<ScannerTag> nfcTags) {
                super.onPostExecute(nfcTags);

                if (nfcTags.size() > 0) {

                    tag1 = nfcTags.get(0).getTagName();
                    tag2 = nfcTags.get(1).getTagName();
                    tag3 = nfcTags.get(2).getTagName();

                    tvTag1.setText(tag1);
                    tvTag2.setText(tag2);
                    tvTag3.setText(tag3);
                } else {
                    ConfigureTagFragment configureTagFragment;
                    configureTagFragment = new ConfigureTagFragment();
                    configureTagFragment.setCancelable(false);
                    configureTagFragment.show(getSupportFragmentManager(), "Config Tags");
                    configureTagFragment.onDataSaveListeners(new ConfigureTagFragment.OnDataSave() {
                        @Override
                        public void onDataSave(boolean isSave) {
                            if (isSave) {
                                getTasks();
                            }
                        }
                    });
                }
            }
        }

        GetTasks gt = new GetTasks();
        gt.execute();
    }

    private void getLogs() {
        class Logs extends AsyncTask<Void, Void, List<Log>> {

            @Override
            protected List<Log> doInBackground(Void... voids) {
                List<Log> allLogs = DatabaseClient
                        .getInstance(getApplicationContext())
                        .getAppDatabase()
                        .logDao()
                        .getAllLogs();
                return allLogs;
            }

            @Override
            protected void onPostExecute(List<Log> logList) {
                super.onPostExecute(logList);

                for (int i = 0; i < logList.size(); i++) {
                    android.util.Log.e("**** Tag - ", logList.get(i).getLogTag() +
                            ", Date - " + logList.get(i).getCreatedDate());
                }

            }
        }

        Logs gt = new Logs();
        gt.execute();
    }

    private void showCustomDialog() {
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialaog_log, viewGroup, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        TextView tvTag1Time = dialogView.findViewById(R.id.tvTag1Time);
        TextView tvTag2Time = dialogView.findViewById(R.id.tvTag2Time);
        TextView tvTag3Time = dialogView.findViewById(R.id.tvTag3Time);
        Button btnCancelDialog = dialogView.findViewById(R.id.btnCancelDialog);

        if (imagePaths.size() > 0) {
            for (int i = 0; i < imagePaths.size(); i++) {
                if (errorTag.get(i).equals(tag1)) {
                    tvTag1Time.setText("Reporting: " + "\n\n" + imagePaths.get(i) + "\n\nDescription:\n\n" + description.get(i));
                }
                if (errorTag.get(i).equals(tag2)) {
                    tvTag2Time.setText("Reporting: " + "\n\n" + imagePaths.get(i) + "\n\nDescription:\n\n" + description.get(i));
                }
                if (errorTag.get(i).equals(tag3)) {
                    tvTag3Time.setText("Reporting: " + "\n\n" + imagePaths.get(i) + "\n\nDescription:\n\n" + description.get(i));
                }
            }
        }

        if (tvTag1Time.getText().toString().length() == 0) {
            tvTag1Time.setText(scanTag1Time);
        }
        if (tvTag2Time.getText().toString().length() == 0) {
            tvTag2Time.setText(scanTag2Time);
        }
        if (tvTag3Time.getText().toString().length() == 0) {
            tvTag3Time.setText(scanTag3Time);
        }


        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        btnCancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                getSavedError();
                isTag1 = false;
                isTag2 = false;
                isTag3 = false;

                scanTag1Time = "";
                scanTag2Time = "";
                scanTag3Time = "";

                tvTag1.setBackgroundColor(ContextCompat.getColor(DashboardActivity.this, R.color.colorPrimary));
                tvTag2.setBackgroundColor(ContextCompat.getColor(DashboardActivity.this, R.color.colorPrimary));
                tvTag3.setBackgroundColor(ContextCompat.getColor(DashboardActivity.this, R.color.colorPrimary));

                imgVerifiedGreen.setVisibility(View.INVISIBLE);
                imgVerifiedOrange.setVisibility(View.INVISIBLE);
                tvScanned.setVisibility(View.VISIBLE);
                skippedRow.setVisibility(View.GONE);

                errorTag.clear();
                imagePaths.clear();
                description.clear();

                tvScannedTime.setText("");
                tvScanned.setText("Row(s) Not Scanned Yet");
                tvRow.setText("");
            }
        });
    }

    private void getSavedError() {
        class GetError extends AsyncTask<Void, Void, List<Report>> {

            @Override
            protected List<Report> doInBackground(Void... voids) {
                List<Report> taskList = DatabaseClient
                        .getInstance(getApplicationContext())
                        .getAppDatabase()
                        .reportDao()
                        .getAllReport();
                return taskList;
            }

            @Override
            protected void onPostExecute(List<Report> nfcTags) {
                super.onPostExecute(nfcTags);

                for (int i = 0; i < nfcTags.size(); i++) {
                    android.util.Log.e("*** Error Log - ", "\nTag No: " + nfcTags.get(i).getTagNo()
                            + "\nImage Path: " + nfcTags.get(i).getImagePath());
                }
            }
        }

        GetError gt = new GetError();
        gt.execute();
    }
}
