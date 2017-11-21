package solonsky.signal.twitter.interfaces;

/**
 * Created by neura on 19.10.17.
 */

public interface NotificationListener {
    void onCreateLikeNotification(String text, String sender, String senderScreenName, String receiver, String avatar);
    void onCreateRetweetNotification(String text, String sender, String senderScreenName, String receiver, String avatar, long statusId);
    void onCreateQuoteNotification(String text, String sender, String senderScreenName, String receiver, String avatar, long statusId);
    void onCreateMentionNotification(String text, String sender, String senderScreenName, String receiver, String avatar);
    void onCreateDirectNotitifcation(String text, String sender, String senderScreenName, String receiver, String avatar);
    void onCreateReplyNotification(String text, String sender, String senderScreenName, String receiver, String avatar);
    void onCreateFollowNotification(String sender, String senderScreenName, String receiver, String avatar);
    void onCreateListedNotification(String listName, String sender, String senderScreenName, String receiver, String avatar);
}
