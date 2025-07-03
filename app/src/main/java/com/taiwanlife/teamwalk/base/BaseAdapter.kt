package com.taiwanlife.teamwalk.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseAdapter<T, VB : ViewBinding>(
    private var models: MutableList<T> = emptyList<T>().toMutableList(),
) : RecyclerView.Adapter<BaseViewHolder<VB>>() {

    abstract fun getViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup): VB
    abstract fun onBindViewBinding(viewBinding: VB, position: Int)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        val layoutInflater = LayoutInflater.from(parent.context)
        return BaseViewHolder(getViewBinding(layoutInflater, parent))
    }

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) {
        onBindViewBinding(holder.viewBinding, position)
    }

    override fun getItemCount(): Int {
        return models.size
    }

    open fun add(model: T) {
        models.add(model)
        notifyItemChanged(models.size - 1)
    }

    open fun setModels(models: MutableList<T>) {
        this.models = models
        notifyDataSetChanged()
    }

    fun getModels(): MutableList<T> {
        return models
    }
}

class BaseViewHolder<VB : ViewBinding>(val viewBinding: VB) :
    RecyclerView.ViewHolder(viewBinding.root)