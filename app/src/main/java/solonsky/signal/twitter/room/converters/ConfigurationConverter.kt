package solonsky.signal.twitter.room.converters

import solonsky.signal.twitter.models.ConfigurationUserModel
import solonsky.signal.twitter.room.models.ConfigurationEntity

/**
 * Created by agladkov on 05.02.18.
 */
interface ConfigurationConverter {
    fun dbToModel(configurationEntity: ConfigurationEntity): ConfigurationUserModel
    fun modelToDb(configurationUserModel: ConfigurationUserModel): ConfigurationEntity
}