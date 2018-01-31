package solonsky.signal.twitter.room.converters

import solonsky.signal.twitter.models.ConfigurationModel
import solonsky.signal.twitter.room.models.SettingsEntity

/**
 * Created by agladkov on 31.01.18.
 */
class SettingsConverterImpl: SettingsConverter {
    override fun dbToModel(settingsEntity: SettingsEntity): ConfigurationModel =
            ConfigurationModel(settingsEntity)

    override fun modelToDb(configurationModel: ConfigurationModel): SettingsEntity =
            SettingsEntity(configurationModel, 0)
}