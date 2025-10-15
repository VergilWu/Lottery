package com.vergil.lottery.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vergil.lottery.data.local.entity.DrawHistoryEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface DrawHistoryDao {


    @Query("SELECT * FROM draw_history WHERE code = :code ORDER BY issue DESC LIMIT :limit")
    fun getHistoryByCode(code: String, limit: Int): Flow<List<DrawHistoryEntity>>


    @Query("SELECT * FROM draw_history WHERE code = :code ORDER BY issue DESC LIMIT :limit")
    suspend fun getHistoryByCodeOnce(code: String, limit: Int): List<DrawHistoryEntity>


    @Query("SELECT * FROM draw_history WHERE code = :code AND issue = :issue")
    suspend fun getByIssue(code: String, issue: String): DrawHistoryEntity?


    @Query("SELECT * FROM draw_history WHERE code = :code ORDER BY issue DESC LIMIT 1")
    suspend fun getLatest(code: String): DrawHistoryEntity?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DrawHistoryEntity)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DrawHistoryEntity>)


    @Query("""
        DELETE FROM draw_history 
        WHERE code = :code 
        AND id NOT IN (
            SELECT id FROM draw_history 
            WHERE code = :code 
            ORDER BY issue DESC 
            LIMIT :keepCount
        )
    """)
    suspend fun deleteOldRecords(code: String, keepCount: Int)


    @Query("SELECT COUNT(*) FROM draw_history WHERE code = :code")
    suspend fun getCount(code: String): Int


    @Query("DELETE FROM draw_history")
    suspend fun clearAll()


    @Query("DELETE FROM draw_history WHERE code = :code")
    suspend fun clearByCode(code: String)
}

