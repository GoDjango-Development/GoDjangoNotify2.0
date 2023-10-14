package com.godjango.godjangonotify20.data.db.database

import android.util.Log
import androidx.room.TypeConverter


class ConverterListFiles {
    @TypeConverter
    fun fromPairList(value: List<Pair<String,String>>?): String? {
        return if(value != null) {
            var string = ""
            for (i in value.indices) {
                string+= value[i].first + " :: " + value[i].second
                if(i!=value.size-1){
                    string+=" -> "
                }
            }
            string
        }else{
            null
        }
    }


    @TypeConverter
    fun toPairList(string: String?): MutableList<Pair<String,String>> {
        return if (string.isNullOrEmpty())
            mutableListOf()
        else {
            val list = string.split(" -> ")
            val mutableList = mutableListOf<Pair<String,String>>()
            list.forEach {
                mutableList.add(Pair(it.substringBefore(" :: "),it.substringAfter(" :: ")))
            }
            mutableList
        }
    }

    @TypeConverter
    fun fromList(value: List<String>?): String? = value?.joinToString ( "::" )


    @TypeConverter
    fun toList(string: String?): MutableList<String> = if(string.isNullOrEmpty()) mutableListOf() else string.split("::").toMutableList()


}