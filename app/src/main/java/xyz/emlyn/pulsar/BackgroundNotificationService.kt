package xyz.emlyn.pulsar

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
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
        return null
    }
}