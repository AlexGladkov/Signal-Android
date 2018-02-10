package solonsky.signal.twitter.room.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import solonsky.signal.twitter.room.contracts.HostersContract
import solonsky.signal.twitter.room.models.HosterEntity

/**
 * Created by agladkov on 01.02.18.
 */
@Dao
interface HostersDao {
    @Query(HostersContract.GET_DATE)
    fun getAllByDate(): List<HosterEntity>

    @Query(HostersContract.GET)
    fun getAll(): List<HosterEntity>

    @Query(HostersContract.GET_LAST)
    fun getLast(): HosterEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun update(hosterEntity: HosterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(hosterEntity: HosterEntity)
}