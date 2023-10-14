package com.godjango.godjangonotify20.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.godjango.godjangonotify20.R
import com.godjango.godjangonotify20.data.models.Configuration
import com.godjango.godjangonotify20.databinding.ItemConfigBinding

class ConfigAdapter(
    private val configs:List<Configuration>,
    private val onDelete:(Int)->Unit,
    private val onPress:(Configuration)->Unit
): Adapter<ConfigAdapter.ConfigVH>() {
    inner class ConfigVH(val binding:ItemConfigBinding): RecyclerView.ViewHolder(binding.root)
    lateinit var ctx:Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfigVH {
        ctx = parent.context
        return ConfigVH(ItemConfigBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int = configs.size

    override fun onBindViewHolder(holder: ConfigVH, position: Int) {
        with(holder){
            with(configs[position]){
                binding.cvConfig.setOnClickListener {
                    onPress(this)
                }
                binding.ivDelete.setOnClickListener {
                    onDelete(id)
                }
                binding.tvName.text = name ?: (ctx.getString(R.string.new_configuration) + " $id")
            }
        }
    }

}