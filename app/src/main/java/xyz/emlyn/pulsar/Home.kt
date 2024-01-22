package xyz.emlyn.pulsar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Dummy data for testing alerts
        val alertDataset = arrayListOf(
            AlertStruct(0, 0, "Server unresponsive!!", 1705841593000),
            AlertStruct(0, 0, "Server unresponsive!!", 1705841593000),
            AlertStruct(0, 0, "Server unresponsive!!", 1705841593000),
            AlertStruct(0, 0, "Server unresponsive!!", 1705841593000),
        )

        // Setup alert RecyclerView
        val alertAdaptor = AlertAdaptor(alertDataset, applicationContext)
        val alertRecyclerView = findViewById<RecyclerView>(R.id.currentAlerts)
        alertRecyclerView.layoutManager = LinearLayoutManager(this)
        alertRecyclerView.adapter = alertAdaptor


    }
}