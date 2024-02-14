package xyz.emlyn.pulsar

import android.Manifest
import android.app.ActivityManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.jivesoftware.smack.AbstractXMPPConnection
import org.jivesoftware.smack.ConnectionConfiguration
import org.jivesoftware.smack.ConnectionListener
import org.jivesoftware.smack.chat.ChatManager
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.json.JSONObject
import org.jxmpp.jid.impl.JidCreate
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import java.util.UUID


class BackgroundNotificationService : Service(), ConnectionListener {

    private val msgIcons = hashMapOf(
        Pair("server", R.drawable.mask_circle), // general server events
        Pair("nzpvt", R.drawable.mask_rounded_rect), // nzpvt subdomain
        Pair("exyz", R.drawable.mask_circle), // main website
        Pair("pulsar", R.drawable.mask_circle), // pulsar alert service
    )

    private lateinit var conn1 : AbstractXMPPConnection
    private val reconnectThread = HandlerThread("xmppreconnectthread")
    private var shouldTryReconnect = true

    private var serviceStarted = false


    private fun sendMessageToActivity(msg : String) {
        val msgIntent = Intent("xmpp-service-msg")
        msgIntent.putExtra("msgBody", msg)
        LocalBroadcastManager.getInstance(this).sendBroadcast(msgIntent)
    }

    // connection closed normally
    override fun connectionClosed() {
        connectionClosedOnError(null)
        super.connectionClosed()
    }

    // connection closed with error - retry
    override fun connectionClosedOnError(e: java.lang.Exception?) {

        sendMessageToActivity("disconnected")

        // check known good sites
        try {
            val gurl = URL("https://google.com")
            val gconnection = gurl.openConnection() as HttpURLConnection
            val gcode = gconnection.responseCode

            if (gcode == 200) {
                // reachable
            } else { throw RuntimeException("") }

            val wurl = URL("https://wikipedia.com")
            val wconnection = wurl.openConnection() as HttpURLConnection
            val wcode = wconnection.responseCode

            if (wcode == 200) {
                // reachable
            } else { throw RuntimeException("") }

            val curl = URL("https://cloudflare.com")
            val cconnection = curl.openConnection() as HttpURLConnection
            val ccode = cconnection.responseCode

            if (ccode == 200) {
                // reachable
            } else { throw RuntimeException("") }

            // all servers reachable - throw alert
            onAlertReceived("{\"class\":\"pulsar\", \"sev\":0, \"body\":\"XMPP connection closed!\", \"timestamp\":" + System.currentTimeMillis() / 1000 + "}")
        } catch (e : Exception) {
            // test servers not reachable - nothing wrong with server
            //      if this is reached, either the device has lost internet connectivity
            //      or the apocalypse is happening
            Log.d("pulsar.xmpp", "XMPP DC but no servers reachable - client error")
        }

        if (!shouldTryReconnect) { return }
        shouldTryReconnect = false
        reconnectThread.start()

        if (!xmppSetup()) {
            Log.w("pulsar.xmpp", "Intial reconnect failed!")
            Handler(reconnectThread.looper).postDelayed({
                if (!xmppSetup()) {
                    Log.w("pulsar.xmpp", "5m reconnect failed!")
                    Handler(reconnectThread.looper).postDelayed({
                        if (!xmppSetup()) {
                            Log.w("pulsar.xmpp", "15m reconnect failed!")
                            Handler(reconnectThread.looper).postDelayed({
                                if (xmppSetup()) { shouldTryReconnect = true }
                            }, 15 * 60 * 1000) // 30m total delay
                        } else {
                            Log.w("pulsar.xmpp", "10m reconnect succeed")
                            shouldTryReconnect = true
                        }
                    }, 10 * 60 * 1000) // 10m total delay
                } else {
                    Log.w("pulsar.xmpp", "5m reconnect succeed")
                    shouldTryReconnect = true
                }
            }, 5 * 60 * 1000) //5m total delay
        } else {
            shouldTryReconnect = true
        }


        super.connectionClosedOnError(e)
    }


    private fun onAlertReceived(message : String) {
        Log.d(
            "pulsar.xmpp",
            "Received message: " + message
        )

        // do not show notification if app is in foreground
        var appInForeground = false
        val appProcesses = (applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).runningAppProcesses
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager . RunningAppProcessInfo . IMPORTANCE_FOREGROUND && appProcess . processName . equals (packageName)) {
                appInForeground = true
            }
        }

        // parse notification json
        var msgSev : Int
        var msgBody : String
        var msgIconId : Int
        var msgTimestamp : Long
        try {
            val msgJObj = JSONObject(message)
            msgSev = msgJObj.getInt("sev")
            msgBody = msgJObj.getString("body")
            msgTimestamp = msgJObj.getLong("timestamp")
            msgIconId = msgIcons[msgJObj.getString("class")]!!
        } catch (e : org.json.JSONException) {
            msgBody = "RECEIVED MALFORMED INPUT: " + message
            msgSev = 0
            msgIconId = msgIcons["pulsar"]!!
            msgTimestamp = System.currentTimeMillis() / 1000
            Log.e("pulsar.xmpp", e.toString())
        }


        // create notification
        // i know i know this approach to msgId sucks but fuck it we ball todo make this better
        val msgId : Int = UUID.randomUUID().hashCode()

        val openAppIntent = Intent(this, Home::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openPendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE)

        if (!appInForeground && msgSev < 5) {
            val builder = NotificationCompat.Builder(this, "pulsar_hud")
                .setSmallIcon(R.drawable.mask_circle)
                .setContentTitle(msgBody)
                .setContentIntent(openPendingIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)

            with(NotificationManagerCompat.from(applicationContext)) {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    notify(msgId, builder.build())
                }
            }
        }

        // add message to database
        AlertDB.getInstance(applicationContext).alertDao().insertAll(Alert(msgId, msgIconId, msgSev, msgBody, msgTimestamp, 1))

    }

    fun xmppSetup() : Boolean {

        try {
            val config: XMPPTCPConnectionConfiguration = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(BuildConfig.XMPP_USER, BuildConfig.XMPP_PASS)
                .setHostAddress(InetAddress.getByName("emlyn.xyz"))
                .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
                .setXmppDomain(JidCreate.domainBareFrom("emlyn.xyz"))
                .setPort(5222)
                .build()


            conn1 = XMPPTCPConnection(config)
            conn1.addConnectionListener(this)


            conn1.connect()
            if (!conn1.isConnected) {
                sendMessageToActivity("connFailed")
                return false
            }
            conn1.login()
            if (conn1.isAuthenticated) {
                Log.d("pulsar.xmpp", "Auth done")
                sendMessageToActivity("connected")
                val chatManager = ChatManager.getInstanceFor(conn1)
                chatManager.addChatListener { chat, _ ->
                    chat.addMessageListener { _, message -> onAlertReceived(message.body!!)}
                }
                shouldTryReconnect = true
                return true
            } else {
                onAlertReceived("{\"sev\":0, \"class\":\"pulsar\", \"timestamp\":" + (System.currentTimeMillis() / 1000) + ", \"body\": \"XMPP authentication failed!!\"}" )
                sendMessageToActivity("connFailed")
                return false
            }
        } catch (e: Exception) {
            Log.e("pulsar.xmpp", "200 "+e.toString())

            // network issue - use checks for known sites
            if (e is java.net.UnknownHostException) {
                connectionClosedOnError(null)
            }
            sendMessageToActivity("connFailed")

        }
        return false
    }

    override fun onCreate() {
        val thread = Thread {
            xmppSetup()
        }
        thread.start()

        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (!serviceStarted) {
            // notification as required for foreground service
            val backgroundServiceNotification = NotificationCompat.Builder(this, "pulsar_quiet")
                .setSmallIcon(R.drawable.mask_circle)
                .setContentTitle("Pulsar XMPP running in background")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build()


            startForeground(1, backgroundServiceNotification)
            serviceStarted = true
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(msgReceiver, IntentFilter("pulsar-activity-msg"))

        return START_STICKY
    }

    private val msgReceiver : BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context : Context, intent : Intent) {
            val msg : String = intent.getStringExtra("msgBody") ?: ""

            if (msg == "forceconnect") {
                Thread { xmppSetup() }.start()
                reconnectThread.quit()

            }

        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(msgReceiver)

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}