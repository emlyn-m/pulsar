package xyz.emlyn.pulsar

import android.content.Context
import android.content.res.ColorStateList
import android.icu.text.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
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

    public var showingDismissed = false

    private fun discardAlert(position: Int) {

        try {
            dataSet.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, dataSet.size)
        } catch (e : IndexOutOfBoundsException) {
            // this occurs when a dismiss is attempted while the dataset is refreshing - can safely be ignored
        }
    }

    fun setNewAlerts(newAlertList : ArrayList<Alert>) {
        dataSet.removeAll { true; }
        notifyDataSetChanged()
        dataSet.addAll(newAlertList)
        notifyItemRangeChanged(0, newAlertList.size)
        notifyDataSetChanged()
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.layout_alert, viewGroup, false)

            return ViewHolder(view)
    }

    fun min(v1: Int, v2: Int): Int {
        if (v1 < v2) { return v1 }
        return v2
    }
    fun max(v1: Int, v2: Int): Int {
        if (v1 > v2) { return v1 }
        return v2
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.id.text = dataSet[position].uid.toString()
        viewHolder.msg.text = dataSet[position].msg
        var sev = dataSet[position].sev
        sev = max(sev, 0)
        sev = min(sev, ctxt.resources.getStringArray(R.array.sevString).size - 1)
        sev = min(sev, ctxt.resources.getIntArray(R.array.sev_color).size - 1)

        viewHolder.sev.text = ctxt.resources.getStringArray(R.array.sevString)[sev]

        val sevColor = ColorStateList.valueOf(ctxt.resources.getIntArray(R.array.sev_color)[sev])
        viewHolder.timestamp.setTextColor(sevColor)
        viewHolder.sev.setTextColor(sevColor)

        viewHolder.icon.background = AppCompatResources.getDrawable(viewHolder.icon.context, dataSet[position].icon)


        // multiply timestamp by 1000 as Date constructor expects time to ms, but we send it to second precision
        viewHolder.timestamp.text =
            DateFormat.getPatternInstance(DateFormat.NUM_MONTH_DAY).format(Date(dataSet[position].timestamp * 1000)) + " @ " +
            DateFormat.getPatternInstance(DateFormat.HOUR24_MINUTE).format(Date(dataSet[position].timestamp * 1000))


        if (!showingDismissed || dataSet[position].visible == 1) {
            viewHolder.discard.text = ctxt.getString(R.string.discard)
            viewHolder.discard.setOnClickListener {
                run {
                    discardAlert(viewHolder.adapterPosition)

                    // remove item from database
                    Thread {
                        val db = AlertDB.getInstance(viewHolder.icon.context)

                        db.alertDao().discardAlert(Integer.parseInt(viewHolder.id.text as String))
                    }.start()

                }
            }
        } else {
            viewHolder.discard.text = ctxt.getString(R.string.restore)
            viewHolder.discard.setOnClickListener {
                run {
                    Thread {
                        val db = AlertDB.getInstance(viewHolder.icon.context)
                        db.alertDao().restoreAlert(Integer.parseInt(viewHolder.id.text as String))
                    }.start()

                }
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}
