package com.parthjadav.nfcreaderexample.database.report;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReportDao {

    @Query("SELECT * FROM report")
    List<Report> getAllReport();

    @Query("SELECT * FROM report WHERE tag_no =:tagNo")
    List<Report> getReports(String tagNo);

    @Insert
    void insertReport(Report report);

    @Delete
    void deleteReport(Report report);

    @Update
    void updateReport(Report report);

}
