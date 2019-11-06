package com.parthjadav.nfcreaderexample.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.parthjadav.nfcreaderexample.database.log.Log;
import com.parthjadav.nfcreaderexample.database.log.LogDao;
import com.parthjadav.nfcreaderexample.database.report.Report;
import com.parthjadav.nfcreaderexample.database.report.ReportDao;
import com.parthjadav.nfcreaderexample.database.tag.ScannerTag;
import com.parthjadav.nfcreaderexample.database.tag.TagDao;

@Database(entities = {Log.class, ScannerTag.class, Report.class}, version = 2,exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LogDao logDao();
    public abstract TagDao tagDao();
    public abstract ReportDao reportDao();
}
