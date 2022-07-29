package com.elian.taskproject.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

open class RecyclerViewAdapter<T : Any>(
    @LayoutRes private val itemLayout: Int,
    private val list: MutableList<T> = mutableListOf(),
) :
    RecyclerView.Adapter<RecyclerViewAdapter<T>.ViewHolder>()
{
    val itemList get() = list
    
    private var onItemClickListener: OnItemClickListener<T>? = null
    private var onItemLongClickListener: OnItemLongClickListener<T>? = null
    private var onBindViewHolderListener: OnBindViewHolderListener<T>? = null

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

    val isEmpty get() = itemCount == 0

    fun clearList()
    {
        list.clear()
        notifyDataSetChanged()
    }

    fun replaceList(newList: List<T>)
    {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
    
    fun getItem(position: Int): T = list[position]

    fun addItem(item: T)
    {
        list.add(item)
        notifyItemInserted(itemCount - 1)
    }

    fun insertItem(position: Int, item: T)
    {
        list.add(position, item)
        notifyItemInserted(position)
    }

    fun insertItemList(insertPosition: Int, itemList: List<T>)
    {
        list.addAll(insertPosition, itemList)
        notifyItemRangeInserted(insertPosition, itemList.size)
    }

    fun updateItem(position: Int, updatedItem: T)
    {
        list[position] = updatedItem
        notifyItemChanged(position)
    }

    fun moveItem(fromPosition: Int, toPosition: Int, item: T)
    {
        list.removeAt(fromPosition)
        list.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
    }

    /**
     * @return False if the list doesn't contains the item.
     */
    fun removeItem(item: T): Boolean
    {
        val position = list.indexOf(item)

        if (position == -1) return false

        list.removeAt(position)
        notifyItemRemoved(position)

        return true
    }

    fun removeItemAt(position: Int)
    {
        list.removeAt(position)
        notifyItemRemoved(position)
    }

    fun removeItemRange(fromPosition: Int, count: Int)
    {
        val toPosition = fromPosition + count

        list.subList(fromPosition, toPosition).clear()
        notifyItemRangeRemoved(fromPosition, toPosition)
    }

    fun setOnItemClickListener(listener: OnItemClickListener<T>)
    {
        onItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener<T>)
    {
        onItemLongClickListener = listener
    }

    fun setOnBindViewHolderListener(listener: OnBindViewHolderListener<T>)
    {
        onBindViewHolderListener = listener
    }

    //region RecyclerView.Adapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)

        val view = inflater.inflate(itemLayout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val item = list[position]

        holder.itemView.setOnClickListener(holder)
        holder.itemView.setOnLongClickListener(holder)

        onBindViewHolderListener?.onBindViewHolder(holder.itemView, item, position)
    }

    override fun getItemCount(): Int = list.size

    //endregion

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener,
        View.OnLongClickListener
    {
        override fun onClick(v: View?)
        {
            onItemClickListener?.onItemClick(v, list[layoutPosition], layoutPosition)
        }

        override fun onLongClick(v: View?): Boolean
        {
            return onItemLongClickListener?.onItemLongClick(v, list[layoutPosition], layoutPosition) ?: false
        }
    }
}
