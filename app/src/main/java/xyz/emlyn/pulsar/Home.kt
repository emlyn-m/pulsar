package xyz.emlyn.pulsar

import android.Manifest
import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.graphics.red
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.jivesoftware.smack.android.AndroidSmackInitializer
import kotlin.math.roundToInt


class Home : AppCompatActivity(), View.OnClickListener {

    private var alertAdaptor : AlertAdaptor? = null

    private var xmppConnected = false
    private var currentlyConnecting = false

    private val layoutTransitionDuration = 500L // milliseconds

    private lateinit var filterPrefs : FilterPrefs

    private var minSev = 6
    private var showDismissed = false
    private var latestAlertData : ArrayList<Alert>? = null


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
        LocalBroadcastManager.getInstance(this).registerReceiver(msgReceiver, IntentFilter("xmpp-service-msg"))

        // todo: clear all notifs

        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = getColor(R.color.black)

        setContentView(R.layout.activity_home)

        filterPrefs = FilterPrefs(applicationContext)


        findViewById<ConstraintLayout>(R.id.homeRootLayout).layoutTransition = LayoutTransition()

        findViewById<ConstraintLayout>(R.id.homeRootLayout).layoutTransition.enableTransitionType(
            LayoutTransition.APPEARING)
        findViewById<ConstraintLayout>(R.id.homeRootLayout).layoutTransition.enableTransitionType(
            LayoutTransition.DISAPPEARING)

        findViewById<ConstraintLayout>(R.id.homeRootLayout).layoutTransition.setDuration(layoutTransitionDuration)



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
                val viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
                viewModel.alertLiveData.observe(this) {
                    Log.d("pulsar.xmpp", "New data observed!!")

                    latestAlertData = (it as ArrayList<Alert>)
                    updateAlertAdaptor()

                }
            }

            // Setup alert RecyclerView + onclick listeners
            runOnUiThread {
                alertAdaptor = AlertAdaptor(alertDataset, applicationContext)
                val alertRecyclerView = findViewById<RecyclerView>(R.id.currentAlerts)
                alertRecyclerView.layoutManager = LinearLayoutManager(this)
                alertRecyclerView.adapter = alertAdaptor

                findViewById<View>(R.id.serverStatusWrapper).setOnClickListener(this)
                findViewById<View>(R.id.filterOverlay).setOnClickListener(this)
                findViewById<View>(R.id.actionBarFilter).setOnClickListener(this)
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
        notificationManager.cancelAll() // clear all pending notifications


        // start notification service
        // note: this requires running as a foreground service, due to Android O background
        //      exec limits (background service killed 10s after app enters idle)
        startForegroundService(Intent(this, BackgroundNotificationService::class.java))


        initializeFilterUI()


    }

    private fun updateAlertAdaptor() {
        if (latestAlertData == null) { return }

        val filteredData = ArrayList<Alert>()
        for (i in latestAlertData!!.indices) {
            if (
                (latestAlertData!![i].visible == 1 || showDismissed) &&
                (latestAlertData!![i].sev <= minSev)
            ) {
                filteredData.add(latestAlertData!![i])
            }
        }

        alertAdaptor?.setNewAlerts(filteredData)
    }

    private fun initializeFilterUI() {
        filterPrefs.getShowDismissed.distinctUntilChanged().asLiveData().observe(this) {
            findViewById<CheckBox>(R.id.discardedEntries).isChecked = it
            showDismissed = it
            alertAdaptor?.showingDismissed = it
            updateAlertAdaptor()
        }

        val radioButtonIds = arrayOf(
            R.id.minSev0,
            R.id.minSev1,
            0,
            R.id.minSev3,
            R.id.minSev4,
            0,
            R.id.minSev6
        )

        filterPrefs.getMinSev.distinctUntilChanged().asLiveData().observe(this) {
            findViewById<RadioGroup>(R.id.logLevel).check(radioButtonIds[it])
            minSev = it
            updateAlertAdaptor()
        }


        findViewById<CheckBox>(R.id.discardedEntries).setOnClickListener {
            lifecycleScope.launch {
                filterPrefs.setShowDismissed((it as CheckBox).isChecked)
            }
        }

        findViewById<RadioGroup>(R.id.logLevel).setOnCheckedChangeListener { _, checkedId ->
            lifecycleScope.launch {
                filterPrefs.setMinSev(radioButtonIds.indexOf(checkedId))
            }
        }


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

        if (v?.id == R.id.serverStatusWrapper) {

            //check if xmpp connection down, if so, try trigger connection
            if (xmppConnected) { return; }
            if (currentlyConnecting) { return; }

            currentlyConnecting = true
            findViewById<TextView>(R.id.serverStatus).text = getString(R.string.status_connecting)
            findViewById<TextView>(R.id.serverStatus).setTextColor(getColor(R.color.status_connecting))
            findViewById<ImageView>(R.id.serverStatusIcon).backgroundTintList =
                ColorStateList.valueOf(getColor(R.color.status_connecting))


            sendMessageToService("forceconnect")

        } else if (v?.id == R.id.actionBarFilter) {

            val backgroundColor = getColor(R.color.background).red
            val mColorAnimator = ValueAnimator.ofFloat(0f, 0.53333336f)
            mColorAnimator.duration = layoutTransitionDuration
            mColorAnimator.addUpdateListener {
                val cVal : Int = ((1 - it.animatedValue as Float) * backgroundColor).roundToInt()
                window.navigationBarColor = Color.rgb(cVal, cVal, cVal)
                window.statusBarColor = Color.rgb(cVal, cVal, cVal)
            }
            mColorAnimator.interpolator = findViewById<ConstraintLayout>(R.id.homeRootLayout).layoutTransition.getInterpolator(LayoutTransition.APPEARING)

            findViewById<ConstraintLayout>(R.id.filterLayout).visibility = View.VISIBLE

            mColorAnimator.start()

        } else if (v?.id == R.id.filterOverlay) {

            val backgroundColor = getColor(R.color.background).red
            val mColorAnimator = ValueAnimator.ofFloat(0.53333336f, 0f)
            mColorAnimator.duration = layoutTransitionDuration
            mColorAnimator.addUpdateListener {
                val cVal : Int = ((1 - it.animatedValue as Float) * backgroundColor).roundToInt()
                window.navigationBarColor = Color.rgb(cVal, cVal, cVal)
                window.statusBarColor = Color.rgb(cVal, cVal, cVal)
            }
            mColorAnimator.interpolator = findViewById<ConstraintLayout>(R.id.homeRootLayout).layoutTransition.getInterpolator(LayoutTransition.DISAPPEARING)

            findViewById<ConstraintLayout>(R.id.filterLayout).visibility = View.GONE

            mColorAnimator.start()
        }
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(msgReceiver);
        super.onPause();
    }
}