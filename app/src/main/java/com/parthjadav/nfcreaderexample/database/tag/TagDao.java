package com.parthjadav.nfcreaderexample.database.tag;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TagDao {

    @Query("SELECT * FROM scannerTag")
    List<ScannerTag> getAllTags();

    @Insert
    void insertTag(ScannerTag nfcTag);

    @Delete
    void deleteTag(ScannerTag nfcTag);

    @Query("UPDATE scannerTag SET tag_name = :tag WHERE id = :tagId")
    void updateTag(String tag, int tagId);

    @Update
    void update(ScannerTag nfcTag);

}
