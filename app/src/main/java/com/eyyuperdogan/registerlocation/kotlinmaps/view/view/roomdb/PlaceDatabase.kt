package com.eyyuperdogan.registerlocation.kotlinmaps.view.view.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.eyyuperdogan.registerlocation.kotlinmaps.view.view.model.Place

@Database(entities = [Place::class], version = 1)
abstract class PlaceDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
}