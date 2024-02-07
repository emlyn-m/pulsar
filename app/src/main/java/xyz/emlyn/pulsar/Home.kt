package xyz.emlyn.pulsar

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import org.jivesoftware.smack.android.AndroidSmackInitializer


class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // initialize smack
        AndroidSmackInitializer.initialize(applicationContext);

        // Pull existing data from Room API
        Thread {
            val alertDatasetRaw = AlertDB.getInstance(this).alertDao().getAll()
            val alertDataset = ArrayList<Alert>()
            for (i in alertDatasetRaw.indices) {
                alertDataset.add(alertDatasetRaw[i])
            }

            // Setup alert RecyclerView
            runOnUiThread {
                val alertAdaptor = AlertAdaptor(alertDataset, applicationContext)
                val alertRecyclerView = findViewById<RecyclerView>(R.id.currentAlerts)
                alertRecyclerView.layoutManager = LinearLayoutManager(this)
                alertRecyclerView.adapter = alertAdaptor
            }
        }.start()

        // check notif permissions
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 17)
        }


        // create notification channels

        val fgServiceChannel = NotificationChannel("pulsar_quiet", "Pulsar XMPP Service", NotificationManager.IMPORTANCE_MIN)
        val alertChannel = NotificationChannel("pulsar_hud", "Pulsar Alerts", NotificationManager.IMPORTANCE_HIGH)

        // Register the channel with the system.
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(fgServiceChannel)
        notificationManager.createNotificationChannel(alertChannel)


        // start notification service
        // note: this requires running as a foreground service, due to Android O background
        //      exec limits (background service killed 10s after app enters idle)
        startForegroundService(Intent(this, BackgroundNotificationService::class.java))


    }
}