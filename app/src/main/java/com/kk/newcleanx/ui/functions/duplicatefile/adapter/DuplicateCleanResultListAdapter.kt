package com.kk.newcleanx.ui.functions.duplicatefile.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.BIG_FILE_CLEAN
import com.kk.newcleanx.data.local.EMPTY_FOLDER
import com.kk.newcleanx.data.local.JUNK_CLEAN
import com.kk.newcleanx.data.local.MainFunction
import com.kk.newcleanx.data.local.SCAN_ANTIVIRUS
import com.kk.newcleanx.databinding.ItemMainLayoutBinding
import com.kk.newcleanx.databinding.ItemMainTitleLayoutBinding

class DuplicateCleanResultListAdapter(private val context: Context, private var onItemClick: (MainFunction) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val listData = arrayListOf(
        MainFunction(R.string.free_up_space, -1, ""),
        MainFunction(R.string.string_clean, R.drawable.big_file_clean, JUNK_CLEAN),
        MainFunction(R.string.string_antivirus, R.drawable.scan_antivirus, SCAN_ANTIVIRUS),
        MainFunction(R.string.big_file_clean, R.drawable.big_file_clean, BIG_FILE_CLEAN),
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