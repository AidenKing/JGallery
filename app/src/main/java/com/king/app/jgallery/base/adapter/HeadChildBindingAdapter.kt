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

    private var onHeadClickListener: OnHeadClickListener<H>? = null

    private var onItemClickListener: OnItemClickListener<I>? = null

    protected abstract val itemClass: Class<*>

    fun setData(list: MutableList<Any>) {
        this.list = list
    }

    fun setOnHeadClickListener(onHeadClickListener: OnHeadClickListener<H>) {
        this.onHeadClickListener = onHeadClickListener
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener<I>) {
        this.onItemClickListener = onItemClickListener
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

    protected fun onClickHead(view: View, position: Int, data: H) {
        if (onHeadClickListener != null) {
            onHeadClickListener!!.onClickHead(view, position, data)
        }
    }

    protected fun onClickItem(view: View, position: Int, data: I) {
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

}
