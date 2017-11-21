package solonsky.signal.twitter.helpers;

import java.util.ArrayList;

import solonsky.signal.twitter.models.NotificationDetailModel;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.models.User;

/**
 * Created by neura on 11.09.17.
 */

public class Validators {

    public static boolean hasStatusModel(long id, ArrayList<StatusModel> statusModels) {
        boolean hasStatus = false;

        for (StatusModel statusModel : statusModels) {
            if (statusModel.getId() == id) {
                hasStatus = true;
                break;
            }
        }

        return hasStatus;
    }

    public static boolean hasDetail(long id, ArrayList<NotificationDetailModel> notificationDetailModels) {
        boolean hasModel = false;

        for (NotificationDetailModel notificationDetailModel : notificationDetailModels) {
            if (notificationDetailModel.getId() == id) {
                hasModel = true;
                break;
            }
        }

        return hasModel;
    }

    public static boolean hasUser(long id, ArrayList<User> users) {
        boolean hasUser = false;

        for (User user : users) {
            if (user.getId() == id) {
                hasUser = true;
                break;
            }
        }

        return hasUser;
    }
}
