package solonsky.signal.twitter.room.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import solonsky.signal.twitter.room.RoomContract;
import solonsky.signal.twitter.room.models.UserEntity;
import solonsky.signal.twitter.room.models.UserIDEntity;

/**
 * Created by sunwi on 23.01.2018.
 */

@Dao
public interface UserIDsDao {
    @Query(RoomContract.USER_GET_BY_ID)
    List<UserIDEntity> getAllById(Long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(UserIDEntity... users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserIDEntity user);
}
