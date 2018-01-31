package solonsky.signal.twitter.room.converters

import solonsky.signal.twitter.models.ConfigurationModel
import solonsky.signal.twitter.room.models.SettingsEntity

/**
 * Created by agladkov on 31.01.18.
 */
interface SettingsConverter {
    fun dbToModel(settingsEntity: SettingsEntity): ConfigurationModel
    fun modelToDb(configurationModel: ConfigurationModel): SettingsEntity
}