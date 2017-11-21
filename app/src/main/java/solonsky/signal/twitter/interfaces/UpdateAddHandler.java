package solonsky.signal.twitter.interfaces;

/**
 * Created by neura on 15.09.17.
 */

public interface UpdateAddHandler {
    void onUpdate();
    void onAdd();
    void onError();
    void onDelete(int position);
}
