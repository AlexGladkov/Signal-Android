package solonsky.signal.twitter.room.converters

import solonsky.signal.twitter.models.UserModel
import solonsky.signal.twitter.room.models.UserEntity
import twitter4j.User

/**
 * Created by sunwi on 23.01.2018.
 */
interface UsersConverter {
    fun apiToDb(apiUser: User): UserEntity
    fun dbToModel(userEntity: UserEntity): UserModel
}