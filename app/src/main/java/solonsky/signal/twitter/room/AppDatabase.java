package solonsky.signal.twitter.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import solonsky.signal.twitter.room.dao.UserIDsDao;
import solonsky.signal.twitter.room.dao.UsersDao;
import solonsky.signal.twitter.room.models.UserEntity;
import solonsky.signal.twitter.room.models.UserIDEntity;

/**
 * Created by sunwi on 22.01.2018.
 */
@Database(entities = {UserEntity.class, UserIDEntity.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UsersDao usersDao();
    public abstract UserIDsDao userIDsDao();
}