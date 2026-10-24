package com.lifeguard.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val durationSeconds: Int,
    val completed: Boolean,
)

@Entity(tableName = "restriction_rules")
data class RestrictionRuleEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val dailyLimitMinutes: Int,
    val enabled: Boolean = true,
)

@Dao
interface LifeGuardDao {
    @Insert suspend fun insertFocusSession(session: FocusSessionEntity)
    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    fun observeFocusSessions(): Flow<List<FocusSessionEntity>>
    @Insert suspend fun insertRule(rule: RestrictionRuleEntity)
    @Query("SELECT * FROM restriction_rules WHERE enabled = 1")
    fun observeEnabledRules(): Flow<List<RestrictionRuleEntity>>
}

@Database(entities = [FocusSessionEntity::class, RestrictionRuleEntity::class], version = 1, exportSchema = true)
abstract class LifeGuardDatabase : RoomDatabase() {
    abstract fun dao(): LifeGuardDao

    companion object {
        @Volatile private var instance: LifeGuardDatabase? = null
        fun get(context: Context): LifeGuardDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context, LifeGuardDatabase::class.java, "lifeguard.db").build().also { instance = it }
        }
    }
}
