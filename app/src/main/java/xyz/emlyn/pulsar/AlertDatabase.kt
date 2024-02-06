package xyz.emlyn.pulsar

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity
data class Alert(
    @PrimaryKey val uid : Int,
    @ColumnInfo(name = "icon") val icon : Int,
    @ColumnInfo(name = "sev") val sev : Int,
    @ColumnInfo(name = "msg") val msg : String,
    @ColumnInfo(name = "timestamp") val timestamp : Long
)

@Dao
interface AlertDAO {
    @Query("SELECT * FROM Alert ORDER BY timestamp ASC")
    fun getAll(): List<Alert>

    @Insert
    fun insertAll(vararg alerts : Alert)

    @Delete
    fun delete(alert : Alert)
}

@Database(entities = [Alert::class], version = 1)
abstract class AlertDB : RoomDatabase() {
    abstract fun alertDao() : AlertDAO
}