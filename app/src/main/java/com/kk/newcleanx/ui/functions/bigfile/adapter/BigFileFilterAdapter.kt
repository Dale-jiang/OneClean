package com.kk.newcleanx.ui.functions.bigfile.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.BigFileFilter
import com.kk.newcleanx.databinding.ItemBigFileFilterBinding

class BigFileFilterAdapter(private val context: Context, private val click: (BigFileFilter) -> Unit) : RecyclerView.Adapter<BigFileFilterAdapter.ViewHolder>() {

    private var mList: MutableList<BigFileFilter> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun initData(list: MutableList<BigFileFilter>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemBigFileFilterBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BigFileFilterAdapter.ViewHolder {
        return ViewHolder(
            ItemBigFileFilterBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: BigFileFilterAdapter.ViewHolder, position: Int) {
        holder.binding.apply {

            val data = mList[holder.layoutPosition]
            tvItem.text = ContextCompat.getString(context, data.nameId)
            if (data.select) {
                ivSelect.isVisible = true
                tvItem.setTextColor(ContextCompat.getColor(context, R.color.primary))
            } else {
                ivSelect.isVisible = false
                tvItem.setTextColor(ContextCompat.getColor(context, R.color.main_text_color))
            }

            root.setOnClickListener {
                if (data.select) return@setOnClickListener
                mList.forEach { it.select = false }
                data.select = true
                click.invoke(data)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }
}