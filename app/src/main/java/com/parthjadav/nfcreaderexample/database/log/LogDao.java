package com.parthjadav.nfcreaderexample.database.log;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LogDao {

    @Query("SELECT * FROM log")
    List<Log> getAllLogs();

    @Query("SELECT * FROM log WHERE log_tag =:logTag")
    List<Log> getLogs(String logTag);

    @Insert
    void insertLog(Log log);

    @Delete
    void deleteLog(Log log);

    @Update
    void updateLog(Log log);

}
