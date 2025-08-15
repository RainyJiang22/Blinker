package com.blinker.video.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.blinker.video.model.Author
import com.blinker.video.ui.utils.AppGlobals

/**
 * @author jiangshiyu
 * @date 2025/8/15
 */
@Database(entities = [Author::class], version = 1)
abstract class BlinkerAppDataBase : RoomDatabase() {

    abstract fun authorDao(): AuthorDao

    companion object {
        @Volatile
        private var INSTANCE: BlinkerAppDataBase? = null

        fun getInstance(): BlinkerAppDataBase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    AppGlobals.getApplication().applicationContext,
                    BlinkerAppDataBase::class.java,
                    "blinker_database"
                ).fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}