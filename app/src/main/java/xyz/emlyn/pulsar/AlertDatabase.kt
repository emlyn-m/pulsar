package xyz.emlyn.pulsar

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity
data class Alert(
    @PrimaryKey @ColumnInfo(name = "uid") val uid : Int,
    @ColumnInfo(name = "icon") val icon : Int,
    @ColumnInfo(name = "sev") val sev : Int,
    @ColumnInfo(name = "msg") val msg : String,
    @ColumnInfo(name = "timestamp") val timestamp : Long,
    @ColumnInfo(name = "visible") val visible : Int, // effectively a boolean - just using for API28 compat
)

@Dao
interface AlertDAO {
    @Query("SELECT * FROM Alert WHERE visible = 1 ORDER BY timestamp DESC")
    fun getAll(): List<Alert>

    @Query("SELECT * FROM Alert WHERE visible = 1 ORDER BY timestamp DESC")
    fun getAllObservable() : LiveData<List<Alert>>

    @Insert
    fun insertAll(vararg alerts : Alert)

    @Delete
    fun delete(alert : Alert)

    @Query("UPDATE Alert SET visible = 0 WHERE uid = :uid")
    fun discardAlert(uid : Int)
}

@Database(entities = [Alert::class], version = 2)
abstract class AlertDB : RoomDatabase() {
    abstract fun alertDao() : AlertDAO

    companion object {
        private var instance : AlertDB? = null

        fun getInstance(context : Context) : AlertDB {
            if (this.instance == null) {
                this.instance = Room.databaseBuilder(context, AlertDB::class.java, "alerts").build()
            }
            return this.instance!!
        }

        fun getInstance() : AlertDB? {
            return this.instance
        }

        // todo: use this
        fun destroyInstance() {
            this.instance = null
        }

    }

}