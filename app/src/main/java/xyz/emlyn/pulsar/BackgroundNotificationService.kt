package xyz.emlyn.pulsar

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Process.THREAD_PRIORITY_DEFAULT
import android.util.Log
import androidx.core.app.NotificationCompat

class BackgroundNotificationService : Service() {


    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    // Handler that receives messages from the thread
    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            Log.d("pulsar.bgservice", "TS="+msg.arg1)
            val newMsg = Message()
            newMsg.arg1 = msg.arg1 + 1000
            serviceHandler?.postDelayed({ this.handleMessage(newMsg) }, 1000)
        }
    }

    override fun onCreate() {

        HandlerThread("ServiceStartArguments", THREAD_PRIORITY_DEFAULT).apply {
            start()

            // Get the HandlerThread's Looper and use it for our Handler
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)

            Log.d("pulsar.bgservice", "TS="+0)

        }

        serviceHandler!!.handleMessage(Message())


        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        // setup notification
        val notif = NotificationCompat.Builder(this, "bg_notif")
            .setSmallIcon(R.drawable.mask_circle)
            .setContentTitle("Pulsar")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()


        startForeground(1, notif)
        return START_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}