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
    @ColumnInfo(name = "timestamp") val timestamp : Long
)

@Dao
interface AlertDAO {
    @Query("SELECT * FROM Alert ORDER BY timestamp DESC")
    fun getAll(): List<Alert>

    @Query("SELECT * FROM Alert ORDER BY timestamp DESC")
    fun getAllObservable() : LiveData<List<Alert>>

    @Insert
    fun insertAll(vararg alerts : Alert)

    @Delete
    fun delete(alert : Alert)

    @Query("DELETE FROM Alert WHERE uid = :uid")
    fun deleteById(uid : Int)
}

@Database(entities = [Alert::class], version = 1)
abstract class AlertDB : RoomDatabase() {
    abstract fun alertDao() : AlertDAO

    companion object {
        private var instance : AlertDB? = null;

        fun getInstance(ctxt : Context) : AlertDB {
            if (this.instance == null) {
                this.instance = Room.databaseBuilder(ctxt, AlertDB::class.java, "alerts").build()
            }
            return this.instance!!
        }

        fun getInstance() : AlertDB? {
            return this.instance
        }

        fun destroyInstance() {
            this.instance = null
        }

    }

}