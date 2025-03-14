package com.firestormsw.listflow.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.firestormsw.listflow.data.dao.ListDao
import com.firestormsw.listflow.data.dao.ListItemDao
import com.firestormsw.listflow.data.dao.PeerDao
import com.firestormsw.listflow.data.entity.ListEntity
import com.firestormsw.listflow.data.entity.ListItemEntity
import com.firestormsw.listflow.data.entity.PeerEntity

@Database(
    entities = [ListEntity::class, ListItemEntity::class, PeerEntity::class],
    exportSchema = true,
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ]
)
@TypeConverters(com.firestormsw.listflow.data.database.TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getListsDao(): ListDao
    abstract fun getListItemsDao(): ListItemDao
    abstract fun getPeersDao(): PeerDao
}