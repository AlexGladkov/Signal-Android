package solonsky.signal.twitter.room.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.List;
import java.util.Set;

import solonsky.signal.twitter.room.RoomContract;

/**
 * Created by sunwi on 23.01.2018.
 */

@Entity(tableName = RoomContract.USER_ID_TABLE)
public class UserIDEntity {
    @PrimaryKey
    private long id;

    @ColumnInfo(name = "id_keys") private String idKeys;

    public UserIDEntity(long id, String idKeys) {
        this.id = id;
        this.idKeys = idKeys;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIdKeys() {
        return idKeys;
    }

    public void setIdKeys(String idKeys) {
        this.idKeys = idKeys;
    }
}
