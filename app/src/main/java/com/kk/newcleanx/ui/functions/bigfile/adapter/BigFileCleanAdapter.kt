package com.kk.newcleanx.ui.functions.bigfile.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.BigFile
import com.kk.newcleanx.databinding.ItemBigFileCleanBinding
import com.kk.newcleanx.utils.formatStorageSize

class BigFileCleanAdapter(private val context: Context, private val click: (BigFile) -> Unit, private val change: () -> Unit) :
    RecyclerView.Adapter<BigFileCleanAdapter.ViewHolder>() {

    private var mList: MutableList<BigFile> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun initData(list: MutableList<BigFile>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemBigFileCleanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BigFileCleanAdapter.ViewHolder {
        return ViewHolder(
            ItemBigFileCleanBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: BigFileCleanAdapter.ViewHolder, position: Int) {
        holder.binding.apply {

            val data = mList[holder.layoutPosition]
            tvName.text = data.name
            tvSize.text = data.size.formatStorageSize()
            ivSelect.setImageResource(if (data.select) R.drawable.scanning_item_complete else R.drawable.scanning_item_unselect)

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
        return mList.size
    }
}