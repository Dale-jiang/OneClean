package com.kk.newcleanx.ui.functions.antivirus.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.VirusBean
import com.kk.newcleanx.databinding.ItemVirusListBinding
import com.kk.newcleanx.utils.CommonUtils

class AntivirusListAdapter(private val context: Context, private val clickListener: (VirusBean) -> Unit) :
    RecyclerView.Adapter<AntivirusListAdapter.ViewHolder>() {

    private var dataList: MutableList<VirusBean> = mutableListOf()

    class ViewHolder(val binding: ItemVirusListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int = dataList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemVirusListBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            runCatching {
                val item = dataList[holder.layoutPosition]
                tvName.text = item.appName.ifEmpty { CommonUtils.getFileNameFromPath(item.path) }
                tvLevelName.isVisible = item.packageName.isEmpty() || !CommonUtils.isPackageInstalled(item.packageName)
                tvLevelName.text = if (item.category.isNotEmpty()) "(${item.category})" else ""
                tvVirusName.text = item.virusName
                tvPath.isVisible = item.packageName.isEmpty() || !CommonUtils.isPackageInstalled(item.packageName)
                tvPath.text = item.path

                if (item.icon == null) {
                    image.setImageResource(R.drawable.item_big_file_clean_icon)
                } else {
                    image.setImageDrawable(item.icon)
                }

                if (item.packageName.isNotEmpty() && CommonUtils.isPackageInstalled(item.packageName)) {
                    tvBtn.text = context.getString(R.string.string_uninstall)
                } else {
                    tvBtn.text = context.getString(R.string.string_remove)
                }

                tvBtn.setOnClickListener {
                    clickListener.invoke(item)
                }
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDataList(list: MutableList<VirusBean>) {
        this.dataList.clear()
        this.dataList.addAll(list)
        notifyDataSetChanged()
    }

    fun getDataList(): MutableList<VirusBean> {
        return dataList
    }


}