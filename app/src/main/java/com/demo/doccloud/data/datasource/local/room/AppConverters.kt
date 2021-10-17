package com.demo.doccloud.data.datasource.local.room

import androidx.room.TypeConverter
import com.demo.doccloud.domain.entities.DocStatus
import com.demo.doccloud.domain.entities.Photo
import com.google.gson.Gson

class AppConverters {
    @TypeConverter
    fun photoListToJson(value: List<Photo>): String = Gson().toJson(value)

    @TypeConverter
    fun jsonToPhotoList(value: String) = Gson().fromJson(value, Array<Photo>::class.java).toList()

    //convert UserType into Int and vice-versa
    @TypeConverter
    fun intToDocStatus(value: Int): DocStatus {
        return when (value) {
            1 -> DocStatus.NOT_SENT
            2 -> DocStatus.SENDING
            else -> DocStatus.SENT
        }
    }

    @TypeConverter
    fun docStatusToInt(status: DocStatus): Int {
        return when (status) {
            DocStatus.NOT_SENT -> 1
            DocStatus.SENDING ->2
            else -> 3
        }
    }



}