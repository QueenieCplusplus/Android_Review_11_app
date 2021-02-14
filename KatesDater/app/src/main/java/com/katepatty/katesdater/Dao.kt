package com.katepatty.katesdater

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
//import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Defines methods for using the SleepNight class with Room.
 */
@Dao
interface Dao {

    @Insert
    suspend fun insert(tour: Touring)

    /**
     * When updating a row with a value already set in a column,
     * replaces the old value with the new one.
     *
     * @param night new value to write
     */
    @Update
    suspend  fun update(tour: Touring)

    /**
     * Selects and returns the row that matches the supplied start time, which is our key.
     *
     * @param key startTimeMilli to match
     */
    @Query("SELECT * from daily_tourguide_quality_table WHERE id = :key")
    suspend fun get(key: Long): Touring?
    /**
     * Deletes all values from the table.
     *
     * This does not delete the table, only its contents.
     */
    @Query("DELETE FROM daily_tourguide_quality_table")
    suspend fun clear()

    /**
     * Selects and returns all rows in the table,
     *
     * sorted by start time in descending order.
     */
    @Query("SELECT * FROM daily_tourguide_quality_table ORDER BY id DESC")
    fun getAllTours(): LiveData<List<Touring>>

    /**
     * Selects and returns the latest night.
     */
    @Query("SELECT * FROM daily_tourguide_quality_table ORDER BY id DESC LIMIT 1")
    suspend fun getTour(): Touring?
}
