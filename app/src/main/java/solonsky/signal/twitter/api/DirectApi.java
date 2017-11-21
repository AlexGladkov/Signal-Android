package solonsky.signal.twitter.api;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.interfaces.UpdateAddHandler;
import solonsky.signal.twitter.models.ChatModel;
import twitter4j.AsyncTwitter;
import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.URLEntity;

/**
 * Created by neura on 18.09.17.
 */

public class DirectApi {
    private static final String TAG = DirectApi.class.getSimpleName();
    private static volatile DirectApi instance;
    private final Gson gson;

    public static DirectApi getInstance() {
        DirectApi localInstance = instance;
        if (localInstance == null) {
            synchronized (DirectApi.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new DirectApi();
                }
            }
        }
        return localInstance;
    }

    private ArrayList<ChatModel> chatModels;
    private long userId;
    private long topId = 0;
    private String screenName;
    private String userName;
    private UpdateAddHandler updateAddHandler;

    private DirectApi() {
        this.gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
        this.chatModels = new ArrayList<>();
        this.userId = -1;
        this.screenName = "";
        this.userName = "";
        this.updateAddHandler = null;
    }

    public void loadData() {
        if (userId == -1 && TextUtils.isEmpty(screenName) && TextUtils.isEmpty(userName)) return;
        final Handler handler = new Handler();
        final Paging paging;

        if (topId == 0) {
            paging = new Paging(1, 250);
        } else {
            paging = new Paging(1, 250);
            paging.setMaxId(topId);
        }

        final ArrayList<DirectMessage> loadedDirects = new ArrayList<>();
        final AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                Log.e(TAG, "Direct error " + te.getLocalizedMessage());
                loadedDirects.clear();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (updateAddHandler != null)
                            updateAddHandler.onError();
                    }
                });
            }

            @Override
            public void gotSentDirectMessages(ResponseList<DirectMessage> messages) {
                super.gotSentDirectMessages(messages);
                loadedDirects.addAll(messages);
                Collections.sort(loadedDirects, new Comparator<DirectMessage>() {
                    @Override
                    public int compare(DirectMessage o1, DirectMessage o2) {
                        return o1.getCreatedAt().compareTo(o2.getCreatedAt());
                    }
                });

                int count = 0;

                if (userId == AppData.ME.getId()) {
                    for (DirectMessage directMessage : loadedDirects) {
                        if ((directMessage.getSenderId() == directMessage.getRecipientId())
                                && (directMessage.getSenderId() == AppData.ME.getId())) {
                            ChatModel chatModel = createDirect(directMessage, count);
                            if (chatModel != null)
                                chatModels.add(chatModel);
                            count = count + 1;
                        }
                    }
                } else {
                    for (DirectMessage directMessage : loadedDirects) {
                        ChatModel chatModel = createDirect(directMessage, count);
                        if (chatModel != null)
                            chatModels.add(chatModel);
                        count = count + 1;
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (updateAddHandler != null) {
                            if (topId == 0) {
                                updateAddHandler.onUpdate();
                            } else {
                                updateAddHandler.onAdd();
                            }
                        }
                    }
                });
            }

            @Override
            public void gotDirectMessages(ResponseList<DirectMessage> messages) {
                super.gotDirectMessages(messages);
                loadedDirects.addAll(messages);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        asyncTwitter.getSentDirectMessages(paging);
                    }
                });
            }
        });

        asyncTwitter.getDirectMessages(paging);
    }

    public void clear() {
        this.chatModels = new ArrayList<>();
        this.userId = -1;
        this.topId = 0;
        this.screenName = "";
        this.userName = "";
        this.updateAddHandler = null;
    }

    public void addNewItem(final DirectMessage directMessage) {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ChatModel chatModel = createDirect(directMessage, chatModels.size());
                if (chatModel != null) {
                    chatModels.add(chatModel);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (updateAddHandler != null)
                                updateAddHandler.onAdd();
                        }
                    });
                }
            }
        }).start();
    }

    private ChatModel createDirect(DirectMessage directMessage, int count) {
        boolean isMine = directMessage.getRecipientId() == AppData.ME.getId();
        boolean hasId = directMessage.getSenderId() == userId || directMessage.getRecipientId() == userId;

        boolean hasScreenName = !TextUtils.isEmpty(screenName) &&
                (TextUtils.equals(directMessage.getSenderScreenName().toLowerCase(), screenName.toLowerCase())
                        || TextUtils.equals(directMessage.getRecipientScreenName().toLowerCase(), screenName.toLowerCase()));

        boolean hasName = !TextUtils.isEmpty(userName) &&
                (TextUtils.equals(directMessage.getSender().getName().toLowerCase(),
                        userName.toLowerCase()) ||
                        TextUtils.equals(directMessage.getRecipient().getName().toLowerCase(),
                                userName.toLowerCase()));

        boolean showArrow;

        try {
           showArrow = count == 0 || chatModels.size() == 0 || (chatModels.get(count - 1).getType()
                    != (isMine ? AppData.CHAT_ME : AppData.CHAT_NOT_ME));
        } catch (Exception e) {
            showArrow = true;
        }

        if (directMessage.getMediaEntities().length > 0) showArrow = false;

        if (hasId || hasScreenName || hasName) {
            String text = directMessage.getText();
            for (URLEntity urlEntity : directMessage.getURLEntities()) {
                text = text.replace(urlEntity.getURL(), urlEntity.getDisplayURL());
            }

            userName = isMine ? directMessage.getSender().getName() : directMessage.getRecipient().getName();
            screenName = isMine ? directMessage.getSenderScreenName() : directMessage.getRecipientScreenName();
            userId = isMine ? directMessage.getSenderId() : directMessage.getRecipientId();

            ChatModel chatModel;
            if (directMessage.getMediaEntities().length > 0) {
                chatModel = new ChatModel(
                        directMessage.getId(),
                        isMine ? AppData.CHAT_ME : AppData.CHAT_NOT_ME, "",
                        directMessage.getMediaEntities()[0].getMediaURL(),
                        directMessage.getSender().getOriginalProfileImageURL(),
                        new LocalDateTime(directMessage.getCreatedAt()).toString("HH:mm"),
                        showArrow, showArrow);
            } else {
                chatModel = new ChatModel(
                        directMessage.getId(),
                        isMine ? AppData.CHAT_ME : AppData.CHAT_NOT_ME, text, "",
                        directMessage.getSender().getOriginalProfileImageURL(),
                        new LocalDateTime(directMessage.getCreatedAt()).toString("HH:mm"),
                        showArrow, showArrow);
            }

            return chatModel;
        }

        return null;
    }

    public Gson getGson() {
        return gson;
    }

    public ArrayList<ChatModel> getChatModels() {
        return chatModels;
    }

    public void setChatModels(ArrayList<ChatModel> chatModels) {
        this.chatModels = chatModels;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public UpdateAddHandler getUpdateAddHandler() {
        return updateAddHandler;
    }

    public void setUpdateAddHandler(UpdateAddHandler updateAddHandler) {
        this.updateAddHandler = updateAddHandler;
    }
}
