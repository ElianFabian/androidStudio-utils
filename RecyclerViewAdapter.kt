package com.elian.efficientneuron.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import kotlin.collections.ArrayList

class RecyclerViewAdapter<T>(
    recyclerView: RecyclerView,
    @LayoutRes private val itemLayout: Int,
    layoutManager: RecyclerView.LayoutManager,
    private val list: ArrayList<T> = arrayListOf(),
) :
    RecyclerView.Adapter<RecyclerViewAdapter<T>.ViewHolder>()
{
    init
    {
        recyclerView.adapter = this
        recyclerView.layoutManager = layoutManager
    }

    private var onItemCLickListener = OnItemClickListener<T> { _, _, _ -> }
    private var onItemLongCLickListener = OnItemLongClickListener<T> { _, _, _ -> false }
    private var onBindViewHolderListener = OnBindViewHolderListener<T> { _, _, _ -> }

    fun interface OnItemClickListener<T>
    {
        fun onItemClick(v: View?, selectedItem: T, position: Int)
    }

    fun interface OnItemLongClickListener<T>
    {
        fun onItemLongClick(v: View?, selectedItem: T, position: Int): Boolean
    }

    fun interface OnBindViewHolderListener<T>
    {
        fun onBindViewHolder(view: View, item: T, position: Int)
    }

    fun loadList(list: List<T>)
    {
        this.list.clear()
        this.list.addAll(list)
    }

    fun setOnItemClickListener(listener: OnItemClickListener<T>)
    {
        onItemCLickListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener<T>)
    {
        onItemLongCLickListener = listener
    }

    fun setOnBindViewHolderListener(listener: OnBindViewHolderListener<T>)
    {
        onBindViewHolderListener = listener
    }

    //region RecyclerView.Adapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val layoutInflater = LayoutInflater.from(parent.context)

        val view = layoutInflater.inflate(itemLayout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val item = list[position]

        holder.itemView.setOnClickListener(holder)
        holder.itemView.setOnLongClickListener(holder)

        onBindViewHolderListener.onBindViewHolder(holder.itemView, item, position)
    }

    override fun getItemCount(): Int = list.size

    //endregion

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener,
        View.OnLongClickListener
    {
        override fun onClick(v: View?)
        {
            onItemCLickListener.onItemClick(v, list[layoutPosition], layoutPosition)
        }

        override fun onLongClick(v: View?): Boolean
        {
            return onItemLongCLickListener.onItemLongClick(v, list[layoutPosition], layoutPosition)
        }
    }
}
