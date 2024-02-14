package xyz.emlyn.pulsar

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jivesoftware.smack.android.AndroidSmackInitializer


class Home : AppCompatActivity(), View.OnClickListener {

    private var alertAdaptor : AlertAdaptor? = null

    private var xmppConnected = false
    private var currentlyConnecting = false


    private val msgReceiver : BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context : Context, intent : Intent) {
            val msg : String = intent.getStringExtra("msgBody") ?: ""
            if (msg == "disconnected" || msg == "connFailed") { xmppConnected = false; currentlyConnecting = false; setXmppStatus() }
            if (msg == "connected") { xmppConnected = true; currentlyConnecting = false; setXmppStatus() }
        }
    }

    private fun sendMessageToService(msg : String) {
        val msgIntent = Intent("pulsar-activity-msg")
        msgIntent.putExtra("msgBody", msg)
        LocalBroadcastManager.getInstance(this).sendBroadcast(msgIntent)

    }

    override fun onResume() {
        // todo: pull from room api here instead of in oncreate
        LocalBroadcastManager.getInstance(this).registerReceiver(msgReceiver, IntentFilter("xmpp-service-msg"))

        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // initialize smack
        AndroidSmackInitializer.initialize(applicationContext);


        // Initialize data from Room API
        Thread {
            val alertDatasetRaw = AlertDB.getInstance(this).alertDao().getAll()
            val alertDataset = ArrayList<Alert>()
            for (i in alertDatasetRaw.indices) {
                alertDataset.add(alertDatasetRaw[i])
            }

            // setup viewmodel here - must ENSURE occurs after first initilization of AlertDB as HomeViewModel does not have context
            // Room API observer
            runOnUiThread {
                val viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
                viewModel.alertLiveData.observe(this) {
                    Log.d("pulsar.xmpp", "New data observed!!")
                    alertAdaptor?.setNewAlerts(it as ArrayList<Alert>);

                }
            }

            // Setup alert RecyclerView + onclick listeners
            runOnUiThread {
                alertAdaptor = AlertAdaptor(alertDataset, applicationContext)
                val alertRecyclerView = findViewById<RecyclerView>(R.id.currentAlerts)
                alertRecyclerView.layoutManager = LinearLayoutManager(this)
                alertRecyclerView.adapter = alertAdaptor

                findViewById<View>(R.id.serverStatusWrapper).setOnClickListener(this)
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

    private fun setXmppStatus() {
        var xmppStatusMsg = getString(R.string.server_offline)
        if (xmppConnected) { xmppStatusMsg = getString(R.string.server_online) }

        var xmppStatusColor = getColor(R.color.status_offline)
        if (xmppConnected) { xmppStatusColor = getColor(R.color.status_online) }

        findViewById<TextView>(R.id.serverStatus).text = xmppStatusMsg
        findViewById<TextView>(R.id.serverStatus).setTextColor(xmppStatusColor)
        findViewById<ImageView>(R.id.serverStatusIcon).backgroundTintList = ColorStateList.valueOf(xmppStatusColor)

    }



    override fun onClick(v: View?) {
        //check if xmpp connection down, if so, try trigger connection
        if (xmppConnected) { return; }
        if (currentlyConnecting) { return; }

        currentlyConnecting = true
        findViewById<TextView>(R.id.serverStatus).text = getString(R.string.status_connecting)
        findViewById<TextView>(R.id.serverStatus).setTextColor(getColor(R.color.status_connecting))
        findViewById<ImageView>(R.id.serverStatusIcon).backgroundTintList = ColorStateList.valueOf(getColor(R.color.status_connecting))


        sendMessageToService("forceconnect")
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(msgReceiver);
        super.onPause();
    }
}