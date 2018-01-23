package solonsky.signal.twitter.room.converters

import solonsky.signal.twitter.room.models.UserEntity
import twitter4j.User

/**
 * Created by sunwi on 23.01.2018.
 */

class UsersConverterImpl: UsersConverter {
    override fun apiToDb(apiUser: User): UserEntity =
            UserEntity.createInstance(apiUser)
}