package com.kk.newcleanx.ui.functions.recentapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kk.newcleanx.data.local.ScreenTimeInfo
import com.kk.newcleanx.databinding.ItemRecentAppScreenTimeBinding
import com.kk.newcleanx.utils.formatDuration

class ScreenTimeListAdapter(private val context: Context, var max: Long = 0L, private val callback: (ScreenTimeInfo) -> Unit) :
    RecyclerView.Adapter<ScreenTimeListAdapter.ScreenTimeViewHolder>() {

    private val listData = mutableListOf<ScreenTimeInfo>()

    inner class ScreenTimeViewHolder(val listBinding: ItemRecentAppScreenTimeBinding) : RecyclerView.ViewHolder(listBinding.root)

    fun setNewData(newList: List<ScreenTimeInfo>) = runCatching {
        val diffCallback = ScreenTimeDiffCallback(listData, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        listData.clear()
        listData.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScreenTimeViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemRecentAppScreenTimeBinding.inflate(inflater, parent, false)
        return ScreenTimeViewHolder(binding)
    }

    override fun getItemCount(): Int = listData.size

    override fun onBindViewHolder(holder: ScreenTimeViewHolder, position: Int) {
        val data = listData[position]
        holder.listBinding.apply {
            Glide.with(context).load(data.icon).into(image)
            tvName.text = data.appName
            tvTime.text = data.duration.formatDuration()
            itemProgressbar.progress = ((data.duration * 100.0) / max).toInt()
            tvBtn.setOnClickListener { callback(data) }
        }
    }

    class ScreenTimeDiffCallback(
        private val oldList: List<ScreenTimeInfo>,
        private val newList: List<ScreenTimeInfo>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].packageName == newList[newItemPosition].packageName
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
