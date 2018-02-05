package solonsky.signal.twitter.room.dao

import android.arch.persistence.room.*
import android.content.res.Configuration
import solonsky.signal.twitter.room.RoomContract
import solonsky.signal.twitter.room.models.ConfigurationEntity

/**
 * Created by agladkov on 05.02.18.
 */
@Dao
interface ConfigurationDao {

    @Query("SELECT * FROM " + RoomContract.CONFIGURATION_TABLE + " WHERE userId = :userId")
    fun getConfigurationById(userId: Long): List<ConfigurationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: ConfigurationEntity)
}