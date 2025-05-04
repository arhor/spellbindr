package com.github.arhor.spellbindr.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.arhor.spellbindr.data.dao.CharacterDao
import com.github.arhor.spellbindr.data.model.Character

@Database(entities = [Character::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun characterDao(): CharacterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "dnd_db")
                .build()
                .also { INSTANCE = it }
        }
    }
}