package com.godjango.godjangonotify20.data.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.godjango.godjangonotify20.data.models.Configuration
import com.godjango.godjangonotify20.data.models.Interval
import com.godjango.godjangonotify20.data.models.Message

@Database(entities = [Configuration::class,Message::class,Interval::class], version = 1)
@TypeConverters(ConverterListFiles::class)
abstract class ConfigurationDatabase:RoomDatabase() {
  abstract fun getConfigurationDao(): ConfigurationDao

}