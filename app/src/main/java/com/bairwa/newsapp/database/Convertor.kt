package com.bairwa.newsapp.database

import androidx.room.TypeConverter
import com.bairwa.newsapp.models.Source

class Convertor {
@TypeConverter
    fun fromSource(source: Source):String{ //convert source to string
    return source.name
}
    @TypeConverter
    fun toSource(name:String): Source { //convert string to source
        return Source(name, name)
    }
}