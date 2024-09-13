package com.kk.newcleanx.ui.functions.recentapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.LaunchesItem
import com.kk.newcleanx.databinding.ItemRecentAppLaunchesBinding
import com.kk.newcleanx.utils.CommonUtils

class AppLaunchesListAdapter(private val context: Context, private val callback: (LaunchesItem) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val listData = mutableListOf<LaunchesItem>()
    private var selectorIndex = 0

    @SuppressLint("NotifyDataSetChanged")
    fun initData(list: MutableList<LaunchesItem>, selectorIndex: Int) = runCatching {
        listData.clear()
        listData.addAll(list)
        this.selectorIndex = selectorIndex
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemRecentAppLaunchesBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemRecentAppLaunchesBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val data = listData[holder.layoutPosition]
            val launchCount: Int
            val launchString: String

            when (selectorIndex) {
                2 -> {
                    launchCount = data.background
                    launchString = if (data.background <= 1) context.getString(R.string.string_launch) else context.getString(R.string.string_launches)
                }

                1 -> {
                    launchCount = data.foreground
                    launchString = if (data.foreground <= 1) context.getString(R.string.string_launch) else context.getString(R.string.string_launches)
                }

                else -> {
                    launchCount = data.totalCount
                    launchString = if (data.totalCount <= 1) context.getString(R.string.string_launch) else context.getString(R.string.string_launches)
                }
            }

            holder.binding.apply {
                Glide.with(context)
                    .load(data.icon)
                    .placeholder(R.drawable.app_manager)
                    .into(image)

                tvName.text = data.appName
                tvPath.text = "$launchCount $launchString"

                tvBtn.isEnabled = CommonUtils.isEnableStop(data.packageName)
                tvBtn.setOnClickListener { callback(data) }
            }
        }

    }
}