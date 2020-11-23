package com.king.app.jgallery.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/17 9:33
 */
abstract class HeadChildBindingAdapter<VH : ViewDataBinding, VI : ViewDataBinding, H, I> :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    protected val TYPE_HEAD = 0
    protected val TYPE_ITEM = 1

    var list: MutableList<Any>? = null

    var onHeadClickListener: OnHeadClickListener<H>? = null

    var onItemClickListener: OnItemClickListener<I>? = null

    var onItemLongClickListener: OnItemLongClickListener<I>? = null

    var onHeadLongClickListener: OnHeadLongClickListener<H>? = null

    protected abstract val itemClass: Class<*>

    fun setData(list: MutableList<Any>) {
        this.list = list
    }

    override fun getItemViewType(position: Int): Int {
        return if (list!![position].javaClass == itemClass) {
            TYPE_ITEM
        } else TYPE_HEAD
    }

    fun isHead(position: Int): Boolean {
        return getItemViewType(position) == TYPE_HEAD
    }

    fun isItem(position: Int): Boolean {
        return getItemViewType(position) == TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_HEAD) {
            val binding = onCreateHeadBind(LayoutInflater.from(parent.context), parent)
            val holder = BindingHolder(binding.root)
            binding.root.setOnClickListener { v ->
                onClickHead(
                    binding.root,
                    holder.layoutPosition,
                    list!![holder.layoutPosition] as H
                )
            }
            onHeadLongClickListener?.let {
                binding.root.setOnLongClickListener { v ->
                    val position = holder.layoutPosition
                    it.onLongClickHead(v, position, list!![holder.layoutPosition] as H)
                    true
                }
            }
            return holder
        } else {
            val binding = onCreateItemBind(LayoutInflater.from(parent.context), parent)
            val holder = BindingHolder(binding.root)
            binding.root.setOnClickListener { v ->
                onClickItem(
                    binding.root,
                    holder.layoutPosition,
                    list!![holder.layoutPosition] as I
                )
            }
            onItemLongClickListener?.let {
                holder.itemView.setOnLongClickListener { v ->
                    val position = holder.layoutPosition
                    it.onLongClickItem(v, position, list!![holder.layoutPosition] as I)
                    true
                }
            }
            return holder
        }
    }

    abstract fun onCreateHeadBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): VH

    abstract fun onCreateItemBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): VI

    open fun onClickHead(view: View, position: Int, data: H) {
        if (onHeadClickListener != null) {
            onHeadClickListener!!.onClickHead(view, position, data)
        }
    }

    open fun onClickItem(view: View, position: Int, data: I) {
        if (onItemClickListener != null) {
            onItemClickListener!!.onClickItem(view, position, data)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_HEAD) {
            val binding = DataBindingUtil.getBinding<VH>(holder.itemView)
            onBindHead(binding, position, list!![position] as H)
            binding!!.executePendingBindings()
        } else {
            val binding = DataBindingUtil.getBinding<VI>(holder.itemView)
            onBindItem(binding, position, list!![position] as I)
            binding!!.executePendingBindings()
        }
    }

    protected abstract fun onBindHead(binding: VH?, position: Int, head: H)

    protected abstract fun onBindItem(binding: VI?, position: Int, item: I)

    override fun getItemCount(): Int {
        return if (list == null) 0 else list!!.size// 首尾分别为header和footer
    }

    class BindingHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface OnHeadClickListener<H> {
        fun onClickHead(view: View, position: Int, data: H)
    }

    interface OnItemClickListener<I> {
        fun onClickItem(view: View, position: Int, data: I)
    }

    interface OnItemLongClickListener<T> {
        fun onLongClickItem(view: View, position: Int, data: T)
    }

    interface OnHeadLongClickListener<H> {
        fun onLongClickHead(view: View, position: Int, data: H)
    }

}
