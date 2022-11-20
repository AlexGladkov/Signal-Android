package solonsky.signal.twitter.helpers

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import solonsky.signal.twitter.models.ConfigurationUserModel
import solonsky.signal.twitter.models.User
import java.util.*

/**
 * Created by agladkov on 06.02.18.
 */
class Converter {

    @TypeConverter
    fun fromJsonArray(value: JsonArray?): String? {
        return if (value == null) value else
            value.toString()
    }

    @TypeConverter
    fun toJsonArray(value: String?): JsonArray? {
        return if (value == null) value else
            Gson().fromJson(value, JsonArray::class.java)
    }

    @TypeConverter
    fun fromMention(value: ConfigurationUserModel.Mentions): Int = when (value) {
        ConfigurationUserModel.Mentions.FROM_ALL -> 0
        ConfigurationUserModel.Mentions.FROM_FOLLOW -> 1
        ConfigurationUserModel.Mentions.OFF -> 2
    }

    @TypeConverter
    fun toMention(value: Int): ConfigurationUserModel.Mentions = when (value) {
        0 -> ConfigurationUserModel.Mentions.FROM_ALL
        1 -> ConfigurationUserModel.Mentions.FROM_FOLLOW
        2 -> ConfigurationUserModel.Mentions.OFF
        else -> ConfigurationUserModel.Mentions.FROM_ALL
    }

    @TypeConverter
    fun fromUser(value: User): String = Gson().toJsonTree(value).toString()

    @TypeConverter
    fun toUser(value: String): User =
            Gson().fromJson(JsonParser().parse(value), User::class.java)

    @TypeConverter
    fun fromArrayListInt(value: ArrayList<Int>): String {
        val builder = StringBuilder()
        value.forEach({
            builder.append(it)
            builder.append(",")
        })

        return builder.toString()
    }

    @TypeConverter
    fun toArrayListInt(value: String): ArrayList<Int> {
        val values = value.split(",")
        val array: ArrayList<Int> = ArrayList()
        values.forEach {
            try {
                array.add(it.toInt())
            } catch (e: Exception) {

            }
        }

        return array
    }
}