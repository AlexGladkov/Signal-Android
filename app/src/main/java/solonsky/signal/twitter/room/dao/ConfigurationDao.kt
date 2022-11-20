package solonsky.signal.twitter.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import solonsky.signal.twitter.room.RoomContract
import solonsky.signal.twitter.room.models.ConfigurationEntity

/**
 * Created by agladkov on 05.02.18.
 */
@Dao
interface ConfigurationDao {

    @Query("SELECT * FROM " + RoomContract.CONFIGURATION_TABLE + " WHERE userId = :userId")
    fun getConfigurationById(userId: Long): List<ConfigurationEntity>

    @Query("SELECT * FROM " + RoomContract.CONFIGURATION_TABLE)
    fun getAllConfigurations(): List<ConfigurationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun update(entity: ConfigurationEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(entity: ConfigurationEntity)
}