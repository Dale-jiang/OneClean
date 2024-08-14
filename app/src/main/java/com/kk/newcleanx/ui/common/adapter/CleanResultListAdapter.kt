package com.kk.newcleanx.ui.common.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.APP_MANAGER
import com.kk.newcleanx.data.local.BIG_FILE_CLEAN
import com.kk.newcleanx.data.local.DEVICE_STATUS
import com.kk.newcleanx.data.local.EMPTY_FOLDER
import com.kk.newcleanx.data.local.MainFunction
import com.kk.newcleanx.databinding.ItemMainLayoutBinding
import com.kk.newcleanx.databinding.ItemMainTitleLayoutBinding

class CleanResultListAdapter(private val context: Context, private var onItemClick: (MainFunction) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val listData = arrayListOf(
        MainFunction(R.string.free_up_space, -1, ""),
        MainFunction(R.string.big_file_clean, R.drawable.big_file_clean, BIG_FILE_CLEAN),
        MainFunction(R.string.app_manager, R.drawable.app_manager, APP_MANAGER),
        MainFunction(R.string.device_status, R.drawable.device_status, DEVICE_STATUS),
        MainFunction(R.string.empty_folder, R.drawable.empty_folder, EMPTY_FOLDER)
    )

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (listData[position].iconId == -1) {
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

        val data = listData[position]
        if (holder is TitleHolder) {
            holder.binding.tvMessage.text = context.getString(data.title)
        } else if (holder is ItemHolder) {
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