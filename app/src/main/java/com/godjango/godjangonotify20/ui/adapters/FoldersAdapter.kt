package com.godjango.godjangonotify20.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.godjango.godjangonotify20.databinding.CvFolderBinding

class FoldersAdapter(
    private val folders: MutableList<Pair<String,String>>,
    private val onDelete: (Int) -> Unit,
    private val onUpdateFirst:(Int,String) ->Unit,
    private val onUpdateSecond:(Int,String) ->Unit
):RecyclerView.Adapter<FoldersAdapter.FoldersVH>() {
    inner class FoldersVH(val binding: CvFolderBinding):ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoldersVH = FoldersVH(
        CvFolderBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: FoldersVH, position: Int) {
        with(holder){
            with(holder.binding) {
                with(folders[position]) {
                    etFolderName.setText(first)
                    etRealFolderName.setText(second)
                    btnDelete.setOnClickListener {
                        onDelete(adapterPosition)
                        notifyItemRemoved(adapterPosition)
                    }
                    etFolderName.addTextChangedListener {
                        onUpdateFirst(adapterPosition, it.toString())
                    }
                    etRealFolderName.addTextChangedListener {
                        onUpdateSecond(adapterPosition, it.toString())
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = folders.size


}