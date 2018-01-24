package solonsky.signal.twitter.room.converters

import solonsky.signal.twitter.models.UserModel
import solonsky.signal.twitter.room.models.UserEntity
import twitter4j.User

/**
 * Created by sunwi on 23.01.2018.
 */

class UsersConverterImpl: UsersConverter {
    override fun dbToModel(userEntity: UserEntity): UserModel {
        return UserModel(userEntity.id, userEntity.biggerProfileImageURL, userEntity.name,
                "@${userEntity.screenName}", userEntity.isFollowRequestSent, false, false)
    }

    override fun apiToDb(apiUser: User): UserEntity =
            UserEntity.createInstance(apiUser)

}