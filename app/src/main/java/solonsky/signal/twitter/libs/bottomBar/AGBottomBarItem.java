package solonsky.signal.twitter.libs.bottomBar;

/**
 * Created by neura on 26.05.17.
 */

public abstract class AGBottomBarItem {
    private int id;
    private int position;
    private boolean hasNew;
    private boolean isStart;

    public AGBottomBarItem(int id, boolean hasNew, boolean isStart) {
        this.id = id;
        this.hasNew = hasNew;
        this.isStart = isStart;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    public boolean isHasNew() {
        return hasNew;
    }
    public void setHasNew(boolean hasNew) {
        this.hasNew = hasNew;
    }
    public boolean isStart() {
        return isStart;
    }
    public void setStart(boolean start) {
        isStart = start;
    }
}
