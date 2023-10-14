package com.godjango.godjangonotify20.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("interval")
data class Interval(
    @PrimaryKey(autoGenerate = false) val id:Int = 1,
    val value:Int,
)
