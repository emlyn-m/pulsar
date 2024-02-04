package xyz.emlyn.pulsar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service.START_STICKY
import android.app.job.JobInfo
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        // Pull existing data from Room API

        val db = Room.databaseBuilder(applicationContext, AlertDB::class.java, "alerts").build()
        val alertDataset = db.alertDao().getAll()

        // Setup alert RecyclerView
        val alertAdaptor = AlertAdaptor(alertDataset, applicationContext)
        val alertRecyclerView = findViewById<RecyclerView>(R.id.currentAlerts)
        alertRecyclerView.layoutManager = LinearLayoutManager(this)
        alertRecyclerView.adapter = alertAdaptor

        // create notification channel

        val name = getString(R.string.notif_channel_name)
        val descriptionText = getString(R.string.notif_channel_desc)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("bg_notif", name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system.
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)


        // setup notification service
        // note: this requires running as a foreground service, due to Android O background
        //      exec limits (background service killed 10s after app enters idle)

        startForegroundService(Intent(this, BackgroundNotificationService::class.java))


    }
}