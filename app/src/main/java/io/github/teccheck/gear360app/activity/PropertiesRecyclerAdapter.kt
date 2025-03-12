package io.github.teccheck.gear360app.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.teccheck.gear360app.R

class PropertiesRecyclerAdapter(private val dataSet: Array<Property>) :
    RecyclerView.Adapter<PropertiesRecyclerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_entry_hardware, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val view = viewHolder.itemView
        val property = dataSet[position]

        view.findViewById<ImageView>(R.id.icon).setImageResource(property.iconResource)
        view.findViewById<TextView>(R.id.name).setText(property.nameResource)
        view.findViewById<TextView>(R.id.value).text = property.value

        property.action?.let { action ->
            view.setOnClickListener { action.execute() }
            view.isClickable = true
        }
    }

    override fun getItemCount() = dataSet.size
}

data class Property(val iconResource: Int, val nameResource: Int, val value: String, val action: Action? = null)

fun interface Action {
    fun execute()
}
