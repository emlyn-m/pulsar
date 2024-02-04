package xyz.emlyn.pulsar

import android.text.Html
import android.util.Log
import androidx.core.text.HtmlCompat
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.charset.UnsupportedCharsetException

class AlertStruct(val icon: Int, val sev: Int, val msg: String, val timestamp: Long) {
    companion object {
        fun exportStructs(mData : ArrayList<AlertStruct>) : String {
            val outputBuilder = StringBuilder()
            for (i in 0..<mData.size) {
                val alert = mData[i]
                val alertBuilder = StringBuilder()
                alertBuilder.append(alert.icon)
                alertBuilder.append("&")
                alertBuilder.append(alert.sev)
                alertBuilder.append("&")
                alertBuilder.append(java.net.URLEncoder.encode(alert.msg))

                alertBuilder.append("&")
                alertBuilder.append(alert.timestamp)
                outputBuilder.append(alertBuilder.toString())
                outputBuilder.append("\n")
            }
            outputBuilder.setLength(outputBuilder.length - 1) // remove trailing newline

            return outputBuilder.toString()
        }

        fun importStructs(data : String) : ArrayList<AlertStruct> {
            val alertStructs = ArrayList<AlertStruct>()
            if (data.isEmpty()) { return alertStructs; }
            val entries = data.split("\n")

            for (i in entries.indices) {
                Log.d("exyz", "i="+i+" entries[i]="+entries[i])
                val components = entries[i].split('&')
                val icon = Integer.parseInt(components[0])
                val sev = Integer.parseInt(components[1])
                val msg = java.net.URLDecoder.decode(components[2])
                val timestamp = components[3].toLong()
                alertStructs.add(AlertStruct(icon, sev, msg, timestamp))
            }

            return alertStructs

        }
    }
}