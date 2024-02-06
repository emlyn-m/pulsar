package xyz.emlyn.pulsar

import android.Manifest
import android.app.ActivityManager
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.jivesoftware.smack.AbstractXMPPConnection
import org.jivesoftware.smack.ConnectionConfiguration
import org.jivesoftware.smack.chat.ChatManager
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.jxmpp.jid.impl.JidCreate
import java.net.InetAddress


class BackgroundNotificationService : Service() {


    private fun XMPPSetup() {
        val config: XMPPTCPConnectionConfiguration = XMPPTCPConnectionConfiguration.builder()
            .setUsernameAndPassword(BuildConfig.XMPP_USER, BuildConfig.XMPP_PASS)
            .setHostAddress(InetAddress.getByName("emlyn.xyz"))
            .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
            .setXmppDomain(JidCreate.domainBareFrom("emlyn.xyz"))
            .setPort(5222)
            .build()
        val conn1: AbstractXMPPConnection = XMPPTCPConnection(config)
        try {
            conn1.connect()
            if (!conn1.isConnected) { Log.e("pulsar.xmpp", "Conn fail") }
            conn1.login()
            if (conn1.isAuthenticated) {
                Log.d("pulsar.xmpp", "Auth done")
                val chatManager = ChatManager.getInstanceFor(conn1)
                chatManager.addChatListener { chat, _ ->
                    chat.addMessageListener { _, message ->
                        Log.d(
                            "pulsar.xmpp",
                            "Received message: " + if (message != null) message.body else "NULL"
                        )

                        // do not show notification if app is in foreground
                        var appInForeground = false
                        val appProcesses = (applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).runningAppProcesses
                        for (appProcess in appProcesses) {
                            if (appProcess.importance == ActivityManager . RunningAppProcessInfo . IMPORTANCE_FOREGROUND && appProcess . processName . equals (packageName)) {
                                appInForeground = true
                            }
                        }
                        // create notification
                        if (!appInForeground) {
                            val builder = NotificationCompat.Builder(this, "pulsar_hud")
                                .setSmallIcon(R.drawable.mask_circle)
                                .setContentTitle(message.body)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setPriority(NotificationCompat.PRIORITY_MAX)

                            with(NotificationManagerCompat.from(applicationContext)) {
                                if (ActivityCompat.checkSelfPermission(
                                        applicationContext,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    notify(NotificationIDManager.getNewID(), builder.build())
                                }
                            }
                        }




                        // todo: add message to room api, likely also do some kind of IPC (maybe via prefs datastore) to alert that the list in home.kt needs to be refreshed
                    }
                    Log.w("app", chat.toString())
                }
            }
        } catch (e: Exception) {
            Log.e("pulsar.xmpp", e.toString())
        }
    }

    override fun onCreate() {
        val thread = Thread {
            XMPPSetup()
        }
        thread.start()

        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // notification as required for foreground service
        val backgroundServiceNotification = NotificationCompat.Builder(this, "pulsar_quiet")
            .setSmallIcon(R.drawable.mask_circle)
            .setContentTitle("Pulsar XMPP running in background")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()


        startForeground(NotificationIDManager.getNewID(), backgroundServiceNotification)
        return START_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}