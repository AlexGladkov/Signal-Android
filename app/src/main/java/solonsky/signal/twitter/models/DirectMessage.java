package solonsky.signal.twitter.models;

import com.google.gson.JsonArray;

import java.util.Date;

/**
 * Created by neura on 15.08.17.
 */

public class DirectMessage {
    private long id;
    private long senderId;
    private long recipientId;
    private Date createdAt;
    private String text;
    private String senderScreenName;
    private String recipientScreenName;
    private JsonArray mediaEntities;
    private User sender;
    private User recipient;

    public DirectMessage(long id, long senderId, long recipientId, Date createdAt, String text,
                         String senderScreenName, String recipientScreenName, User sender, User recipient,
                         JsonArray mediaEntities) {
        this.id = id;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.createdAt = createdAt;
        this.text = text;
        this.senderScreenName = senderScreenName;
        this.recipientScreenName = recipientScreenName;
        this.sender = sender;
        this.recipient = recipient;
        this.mediaEntities = mediaEntities;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(long recipientId) {
        this.recipientId = recipientId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderScreenName() {
        return senderScreenName;
    }

    public void setSenderScreenName(String senderScreenName) {
        this.senderScreenName = senderScreenName;
    }

    public String getRecipientScreenName() {
        return recipientScreenName;
    }

    public void setRecipientScreenName(String recipientScreenName) {
        this.recipientScreenName = recipientScreenName;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public JsonArray getMediaEntities() {
        return mediaEntities;
    }

    public void setMediaEntities(JsonArray mediaEntities) {
        this.mediaEntities = mediaEntities;
    }
}
