package solonsky.signal.twitter.room.converters

import solonsky.signal.twitter.models.ConfigurationUserModel
import solonsky.signal.twitter.room.models.ConfigurationEntity

/**
 * Created by agladkov on 05.02.18.
 */
class ConfigurationConverterImpl: ConfigurationConverter {
    override fun dbToModel(configurationEntity: ConfigurationEntity): ConfigurationUserModel {
            return ConfigurationUserModel.createFromEntity(configurationEntity)
    }

    override fun modelToDb(configurationUserModel: ConfigurationUserModel): ConfigurationEntity =
            ConfigurationEntity.createFromModel(configurationUserModel)
}