package xyz.emlyn.pulsar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.icu.text.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date

class AlertAdaptor(private val dataSet: ArrayList<Alert>, private val ctxt: Context)
    : RecyclerView.Adapter<AlertAdaptor.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val id : TextView
        val msg: TextView
        val icon: ImageView
        val sev: TextView
        val timestamp: TextView
        val discard: TextView

        init {
            id = view.findViewById(R.id.alertUUID)
            msg = view.findViewById(R.id.alertMsg)
            icon = view.findViewById(R.id.clusterIcon)
            sev = view.findViewById(R.id.alertSev)
            timestamp = view.findViewById(R.id.alertTimestamp)
            discard = view.findViewById(R.id.discardAlert)
        }
    }


    fun discardAlert(position: Int) {

        dataSet.removeAt(position)
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, dataSet.size);
    }

    fun setNewAlerts(newAlertList : ArrayList<Alert>) {
        Log.d("pulsar.xmpp", "setNewAlerts called")
        dataSet.removeAll { true; }
        dataSet.addAll(newAlertList)
        notifyItemRangeChanged(0, newAlertList.size)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.layout_alert, viewGroup, false)

            return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.id.text = dataSet[position].uid.toString()
        viewHolder.msg.text = dataSet[position].msg
        viewHolder.sev.text = ctxt.resources.getStringArray(R.array.sevString)[dataSet[position].sev]

        val sevColor = ColorStateList.valueOf(ctxt.resources.getIntArray(R.array.sev_color)[dataSet[position].sev])
        viewHolder.timestamp.setTextColor(sevColor)
        viewHolder.sev.setTextColor(sevColor)

        viewHolder.icon.background = AppCompatResources.getDrawable(viewHolder.icon.context, dataSet[position].icon)


        viewHolder.timestamp.text =
            DateFormat.getPatternInstance(DateFormat.NUM_MONTH_DAY).format(Date(dataSet[position].timestamp)) + " @ " +
            DateFormat.getPatternInstance(DateFormat.HOUR24_MINUTE).format(Date(dataSet[position].timestamp))


        viewHolder.discard.setOnClickListener {run {
            discardAlert(viewHolder.adapterPosition)

            // remove item from database
            Thread {
                val db = AlertDB.getInstance(viewHolder.icon.context)

                db.alertDao().deleteById(Integer.parseInt(viewHolder.id.text as String))
            }.start()

        }}
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}
