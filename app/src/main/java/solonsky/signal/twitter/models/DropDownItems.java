package solonsky.signal.twitter.models;

/**
 * Created by neura on 30.05.17.
 */

public class DropDownItems {
    private int id;
    private String size;
    private String title;

    public DropDownItems(int id, String size, String title) {
        this.id = id;
        this.size = size;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
