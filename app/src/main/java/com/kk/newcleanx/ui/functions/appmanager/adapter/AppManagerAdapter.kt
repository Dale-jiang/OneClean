package com.kk.newcleanx.ui.functions.appmanager.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kk.newcleanx.databinding.ItemAppManagerBinding

class AppManagerAdapter(private val context: Context, private val uninstallClick: (String) -> Unit) : RecyclerView.Adapter<AppManagerAdapter.ViewHolder>() {

    private var mList: MutableList<String> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun initData(list: MutableList<String>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemAppManagerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppManagerAdapter.ViewHolder {
        return ViewHolder(
            ItemAppManagerBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: AppManagerAdapter.ViewHolder, position: Int) {
        holder.binding.apply {

//            val data = mList[holder.layoutPosition]
//            tvName.text = data

            tvUninstall.setOnClickListener {

            }

            root.setOnClickListener {

            }
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }
}