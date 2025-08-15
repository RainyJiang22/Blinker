package com.blinker.video.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.blinker.video.model.Author

/**
 * @author jiangshiyu
 * @date 2025/8/15
 */
@Dao
interface AuthorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(author: Author): Long

    @Query("select * from author limit 1")
    suspend fun getUser(): Author?


    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(author: Author): Int

    @Delete
    suspend fun delete(author: Author): Int
}