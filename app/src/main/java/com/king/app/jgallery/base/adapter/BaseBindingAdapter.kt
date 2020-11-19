package com.king.app.jgallery.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * Desc:RecyclerView.Adapter的databinding方式的基类
 *
 * @author：Jing Yang
 * @date: 2018/6/7 16:50
 */
abstract class BaseBindingAdapter<V : ViewDataBinding, T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var list: List<T>? = null

    protected var listenerClick: OnItemClickListener<T>? = null

    protected var listenerLongClick: OnItemLongClickListener<T>? = null

    fun setOnItemClickListener(listener: OnItemClickListener<T>) {
        listenerClick = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener<T>) {
        listenerLongClick = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: V = onCreateBind(LayoutInflater.from(parent.context), parent)
        val holder = BindingHolder(binding.root)
        holder.itemView.setOnClickListener { v ->
            val position = holder.layoutPosition
            onClickItem(v, position, list!![position])
        }
        if (listenerLongClick != null) {
            holder.itemView.setOnLongClickListener { v ->
                val position = holder.layoutPosition
                listenerLongClick!!.onLongClickItem(v, position, list!![position])
                true
            }
        }
        return holder
    }

    abstract fun onCreateBind(inflater: LayoutInflater, parent: ViewGroup): V

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = getBindingFromHolder(holder)
        onBindItem(binding, position, list!![position])
        binding.executePendingBindings()
    }

    protected fun getBindingFromHolder(holder: RecyclerView.ViewHolder): V {
        return DataBindingUtil.getBinding(holder.itemView)!!
    }

    protected abstract fun onBindItem(binding: V, position: Int, bean: T)

    open fun onClickItem(v: View, position: Int, bean: T) {
        if (listenerClick != null) {
            listenerClick!!.onClickItem(v, position, list!![position])
        }
    }

    override fun getItemCount(): Int {
        return if (list == null) 0 else list!!.size
    }

    fun getItem(position: Int): T {
        return list!![position]
    }

    interface OnItemClickListener<T> {
        fun onClickItem(view: View, position: Int, data: T)
    }

    interface OnItemLongClickListener<T> {
        fun onLongClickItem(view: View, position: Int, data: T)
    }
}
