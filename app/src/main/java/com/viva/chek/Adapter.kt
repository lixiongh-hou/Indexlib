package com.viva.chek

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView

/**
 * @author 李雄厚
 *
 *
 */
class Adapter(
    private val context: Context
) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    private val mData = mutableListOf<DataEntity>()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<DataEntity>) {
        mData.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = mData[position]
        holder.bind(data)
    }

    override fun getItemCount(): Int = mData.size


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text = itemView.findViewById<AppCompatTextView>(R.id.text)
        fun bind(data: DataEntity) {
            text.text = data.name
        }
    }
}