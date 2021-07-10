package solonsky.signal.twitter.room.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import solonsky.signal.twitter.room.RoomContract;
import solonsky.signal.twitter.room.contracts.UsersContract;
import solonsky.signal.twitter.room.models.UserEntity;

/**
 * Created by sunwi on 22.01.2018.
 */

@Dao
public interface UsersDao {
    @Query(RoomContract.USER_GET_ALL)
    List<UserEntity> getAll();

    @Query(RoomContract.USER_GET_BY_SCREENNAME)
    List<UserEntity> getAllByScreen(String screenName);

    @Query(RoomContract.USER_GET_BY_NAME)
    List<UserEntity> getAllByName(String name);

    @Query(UsersContract.GET_USERS)
    List<UserEntity> getById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(UserEntity... users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity user);
}
