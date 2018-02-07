package solonsky.signal.twitter.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import solonsky.signal.twitter.helpers.Converter;
import solonsky.signal.twitter.room.dao.ConfigurationDao;
import solonsky.signal.twitter.room.dao.HostersDao;
import solonsky.signal.twitter.room.dao.SettingsDao;
import solonsky.signal.twitter.room.dao.UserIDsDao;
import solonsky.signal.twitter.room.dao.UsersDao;
import solonsky.signal.twitter.room.models.ConfigurationEntity;
import solonsky.signal.twitter.room.models.HosterEntity;
import solonsky.signal.twitter.room.models.SettingsEntity;
import solonsky.signal.twitter.room.models.UserEntity;
import solonsky.signal.twitter.room.models.UserIDEntity;

/**
 * Created by sunwi on 22.01.2018.
 */
@Database(entities = {UserEntity.class, UserIDEntity.class, SettingsEntity.class,
        HosterEntity.class, ConfigurationEntity.class}, version = 6)
@TypeConverters(Converter.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UsersDao usersDao();
    public abstract UserIDsDao userIDsDao();
    public abstract SettingsDao settingsDao();
    public abstract HostersDao hostersDao();
    public abstract ConfigurationDao configurationDao();
}
