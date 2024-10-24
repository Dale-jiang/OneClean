package com.kk.newcleanx.ui.common.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.kk.newcleanx.data.local.DUPLICATE_FILES_CLEAN
import com.kk.newcleanx.data.local.MainFunction
import com.kk.newcleanx.data.local.SCAN_ANTIVIRUS
import com.kk.newcleanx.data.local.antivirusRedPointLastShowTime
import com.kk.newcleanx.data.local.duplicateFilesRedPointLastShowTime
import com.kk.newcleanx.data.local.main_list_data
import com.kk.newcleanx.databinding.ItemMainLayoutBinding
import com.kk.newcleanx.databinding.ItemMainTitleLayoutBinding
import com.kk.newcleanx.utils.CommonUtils.getCurrentDateTimeInMillis

class MainListAdapter(private val context: Context, private var onItemClick: (MainFunction) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return main_list_data.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (main_list_data[position].iconId == -1) {
            0
        } else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                TitleHolder(
                    ItemMainTitleLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }

            1 -> {
                ItemHolder(
                    ItemMainLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }

            else -> {
                ItemHolder(
                    ItemMainLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val data = main_list_data[position]
        if (holder is TitleHolder) {
            holder.binding.tvMessage.text = context.getString(data.title)
        } else if (holder is ItemHolder) {

            holder.binding.pointRed.isVisible = false

            if (data.type == SCAN_ANTIVIRUS) {
                val currentTime = getCurrentDateTimeInMillis()
                holder.binding.pointRed.isVisible = currentTime > antivirusRedPointLastShowTime
            }

            if (data.type == DUPLICATE_FILES_CLEAN) {
                val currentTime = getCurrentDateTimeInMillis()
                holder.binding.pointRed.isVisible = currentTime > duplicateFilesRedPointLastShowTime
            }

            holder.binding.ivIcon.setImageResource(data.iconId)
            holder.binding.tvMessage.text = context.getString(data.title)
            holder.binding.itemLayout.setOnClickListener {
                onItemClick.invoke(data)
            }
        }
    }

    class ItemHolder(val binding: ItemMainLayoutBinding) : RecyclerView.ViewHolder(binding.root)
    class TitleHolder(val binding: ItemMainTitleLayoutBinding) : RecyclerView.ViewHolder(binding.root)


}