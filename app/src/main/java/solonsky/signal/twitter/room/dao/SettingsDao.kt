package solonsky.signal.twitter.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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