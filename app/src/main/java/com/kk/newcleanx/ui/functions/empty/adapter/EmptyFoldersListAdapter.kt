package com.kk.newcleanx.ui.functions.empty.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kk.newcleanx.databinding.ItemEmptyFoldersBinding

class EmptyFoldersListAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val emptyFolderList = mutableListOf<String>()

    @SuppressLint("NotifyDataSetChanged")
    fun initData(list: MutableList<String>) {
        emptyFolderList.clear()
        emptyFolderList.addAll(list)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemEmptyFoldersBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemEmptyFoldersBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int {
        return emptyFolderList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val item = emptyFolderList[holder.layoutPosition]
            holder.binding.tvItem.text = item
        }
    }
}