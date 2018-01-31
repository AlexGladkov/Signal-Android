package solonsky.signal.twitter.room.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import solonsky.signal.twitter.room.RoomContract
import solonsky.signal.twitter.room.contracts.SettingsContract
import solonsky.signal.twitter.room.models.SettingsEntity
import solonsky.signal.twitter.room.models.UserIDEntity

/**
 * Created by agladkov on 31.01.18.
 */
@Dao
interface SettingsDao {

    @Query(SettingsContract.GET)
    fun getAll(): List<SettingsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: SettingsEntity)
}