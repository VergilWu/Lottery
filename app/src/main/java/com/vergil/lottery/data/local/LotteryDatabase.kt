package com.vergil.lottery.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vergil.lottery.core.constants.AppConstants
import com.vergil.lottery.data.local.dao.DrawHistoryDao
import com.vergil.lottery.data.local.entity.DrawHistoryEntity


@Database(
    entities = [DrawHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LotteryDatabase : RoomDatabase() {

    abstract fun drawHistoryDao(): DrawHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: LotteryDatabase? = null


        fun getInstance(context: Context): LotteryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LotteryDatabase::class.java,
                    AppConstants.DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()  
                    .build()
                INSTANCE = instance
                instance
            }
        }


        @Synchronized
        fun clearInstance() {
            INSTANCE = null
        }
    }
}

