package com.hoangminh.drivinglogger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LogAdapter(private val logEntries: List<String>) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.log_item, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val logEntry = logEntries[position].split("\n")
        holder.timestampTextView.text = logEntry[0] // First line: timestamp
        holder.locationTextView.text = logEntry[1] // Second line: location
        holder.logtypeTextView.text = logEntry[2] // Third line: log type
    }

    override fun getItemCount(): Int = logEntries.size

    class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)
        val logtypeTextView: TextView = itemView.findViewById(R.id.logtypeTextView)
    }
}
