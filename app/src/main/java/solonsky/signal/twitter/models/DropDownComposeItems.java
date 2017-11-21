package solonsky.signal.twitter.models;

/**
 * Created by neura on 30.05.17.
 */

public class DropDownComposeItems {
    private long id;
    private String imageUrl;
    private String text;
    private String clientToken;
    private String clientSecret;

    public DropDownComposeItems(long id, String imageUrl, String text, String clientToken,
                                String clientSecret) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.text = text;
        this.clientToken = clientToken;
        this.clientSecret = clientSecret;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
