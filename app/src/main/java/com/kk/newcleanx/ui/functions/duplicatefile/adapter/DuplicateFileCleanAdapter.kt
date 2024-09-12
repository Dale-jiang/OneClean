package com.kk.newcleanx.ui.functions.duplicatefile.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.DuplicateFile
import com.kk.newcleanx.databinding.ItemDuplicateFileCleanBinding
import com.kk.newcleanx.utils.CommonUtils
import com.kk.newcleanx.utils.formatStorageSize
import java.io.File

class DuplicateFileCleanAdapter(
    private val context: Context,
    private var list: MutableList<DuplicateFile>,
    private val click: (DuplicateFile) -> Unit,
    private val change: () -> Unit
) : RecyclerView.Adapter<DuplicateFileCleanAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemDuplicateFileCleanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DuplicateFileCleanAdapter.ViewHolder {
        return ViewHolder(
            ItemDuplicateFileCleanBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: DuplicateFileCleanAdapter.ViewHolder, position: Int) {
        holder.binding.apply {

            val data = list[holder.layoutPosition]

            when (data.itemType) {
                0 -> {
                    itemLayout.setBackgroundResource(R.drawable.shape_fcfcfc_r11_top)
                    space.isVisible = false
                }

                2 -> {
                    itemLayout.setBackgroundResource(R.drawable.shape_fcfcfc_r11_bottom)
                    space.isVisible = true
                }

                else -> {
                    space.isVisible = false
                    itemLayout.setBackgroundResource(R.drawable.shape_fcfcfc_r11)
                }
            }

            tvName.text = data.name
            tvSize.text = data.size.formatStorageSize()
            ivSelect.setImageResource(if (data.select) R.drawable.scanning_item_complete else R.drawable.scanning_item_unselect_deep)

            if (data.mimeType.startsWith("image/", true) || data.mimeType.startsWith("video/", true)) {
                Glide.with(context).load(File(data.path)).centerCrop().placeholder(R.drawable.svg_duplicate_file_clean_icon).into(ivItem)
            } else if (data.mimeType.startsWith("application/", true)) {
                Glide.with(context).load(CommonUtils.getApkIcon(data.path)).placeholder(R.drawable.svg_duplicate_file_clean_icon).into(ivItem)
            } else {
                Glide.with(context).load(R.drawable.svg_duplicate_file_clean_icon).into(ivItem)
            }

            ivSelect.setOnClickListener {
                data.select = !data.select
                change.invoke()
                notifyItemChanged(holder.layoutPosition)
            }

            root.setOnClickListener {
                click.invoke(data)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getList(): MutableList<DuplicateFile> {
        return list
    }
}