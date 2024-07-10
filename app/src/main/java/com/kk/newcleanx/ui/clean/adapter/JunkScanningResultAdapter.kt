package com.kk.newcleanx.ui.clean.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.JunkDetails
import com.kk.newcleanx.data.local.JunkDetailsParent
import com.kk.newcleanx.data.local.JunkDetailsType
import com.kk.newcleanx.data.local.JunkType
import com.kk.newcleanx.databinding.ItemJunkScanningResultBinding
import com.kk.newcleanx.ui.common.dialog.CustomAlertDialog
import com.kk.newcleanx.utils.CommonUtils
import com.kk.newcleanx.utils.formatStorageSize
import com.kk.newcleanx.utils.imageIcon
import com.kk.newcleanx.utils.nameString

class JunkScanningResultAdapter(private val context: Context, private val selectListener: () -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val junkDataList = mutableListOf<JunkDetailsType>()

    @SuppressLint("NotifyDataSetChanged")
    fun initData(list: MutableList<JunkDetailsType>) {
        junkDataList.clear()
        junkDataList.addAll(list)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemJunkScanningResultBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemJunkScanningResultBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int {
        return junkDataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val item = junkDataList[holder.layoutPosition]
            holder.binding.run {

                if (item is JunkDetailsParent) {
                    viewSpace.isVisible = true
                    clContainer.setBackgroundResource(if (item.isOpen) R.drawable.shape_no_bottom_border else R.drawable.shape_scanning_item_bg)
                    ivApkLoading.setImageResource(if (item.select) R.drawable.scanning_item_complete else R.drawable.scanning_item_unselect)
                    itemApkIcon.isVisible = true
                    itemApkIcon.setImageResource(item.imageIcon())
                    tvApk.text = item.nameString()
                    tvApkSize.text = item.fileSize.formatStorageSize()

                    holder.itemView.setOnClickListener {
                        if (item.isOpen) {
                            val childList = junkDataList.filterIsInstance<JunkDetails>().filter { it.junkType == item.junkType }
                            item.isOpen = false
                            junkDataList.removeAll(childList)
                            notifyItemRangeRemoved(holder.layoutPosition + 1, item.subItems.size)
                            notifyItemChanged(holder.layoutPosition)
                        } else {
                            item.isOpen = true
                            junkDataList.addAll(holder.layoutPosition + 1, item.subItems)
                            notifyItemRangeInserted(holder.layoutPosition + 1, item.subItems.size)
                            notifyItemChanged(holder.layoutPosition)
                        }
                    }

                    ivApkLoading.setOnClickListener {
                        item.select = !item.select
                        item.subItems.forEach { it.select = item.select }
                        notifyItemRangeChanged(holder.layoutPosition, item.subItems.size + 1)
                        selectListener.invoke()
                    }


                } else if (item is JunkDetails) {

                    viewSpace.isVisible = false
                    itemApkIcon.isInvisible = true
                    tvApk.text = item.fileName
                    tvApkSize.text = item.fileSize.formatStorageSize()

                    if (item.junkType == JunkType.APK_FILES && CommonUtils.getApkIcon(item.filePath) != null) {
                        itemApkIcon.isInvisible = false
                        itemApkIcon.setImageDrawable(CommonUtils.getApkIcon(item.filePath))
                    }

                    if ((holder.layoutPosition == junkDataList.size - 1) || (junkDataList.getOrNull(holder.layoutPosition + 1) is JunkDetailsParent)) {
                        clContainer.setBackgroundResource(R.drawable.shape_no_top_border)
                    } else {
                        clContainer.setBackgroundResource(R.drawable.shape_no_top_bottom_border)
                    }
                    ivApkLoading.setImageResource(if (item.select) R.drawable.scanning_item_complete else R.drawable.scanning_item_unselect)

                    root.setOnClickListener {
                        CustomAlertDialog(context).showDialog(title = item.fileName,
                                                              message = item.filePath,
                                                              positiveButtonText = context.getString(R.string.string_ok),
                                                              negativeButtonText = "",
                                                              onPositiveButtonClick = {
                                                                  it.dismiss()
                                                              },
                                                              onNegativeButtonClick = {})
                    }

                    ivApkLoading.setOnClickListener { _ ->
                        item.select = !item.select
                        val parentPosition = junkDataList.indexOfFirst { it.junkType == item.junkType }
                        if (-1 == parentPosition) return@setOnClickListener
                        val parent = junkDataList.getOrNull(parentPosition) as? JunkDetailsParent ?: return@setOnClickListener
                        parent.select = parent.subItems.all { it.select }
                        notifyItemChanged(parentPosition)
                        notifyItemChanged(holder.layoutPosition)
                        selectListener.invoke()
                    }

                }

            }
        }
    }

}