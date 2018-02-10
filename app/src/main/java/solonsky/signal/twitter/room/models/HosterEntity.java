package solonsky.signal.twitter.room.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import solonsky.signal.twitter.room.RoomContract;
import solonsky.signal.twitter.room.contracts.HostersContract;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Created by agladkov on 01.02.18.
 */
@Entity(tableName = RoomContract.HOSTERS_TABLE,
        foreignKeys = @ForeignKey(entity = UserEntity.class,
                                parentColumns = "id",
                                childColumns = HostersContract.USER_ID,
                                onDelete = CASCADE))
public class HosterEntity {
    @PrimaryKey public final long id;
    @ColumnInfo(name = HostersContract.TIMESTAMP) private String timestamp;
    @ColumnInfo(name = HostersContract.USER_ID) private final long userId;

    public HosterEntity(long id, String timestamp, long userId) {
        this.id = id;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public long getId() {
        return id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public long getUserId() {
        return userId;
    }
}
