package com.cbi.gis.apps.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cbi.gis.apps.R
import com.cbi.gis.apps.data.model.DataDailyModel
import com.cbi.gis.apps.ui.viewModel.DataJobTypeViewModel
import com.cbi.gis.apps.utils.AppUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton

class UploadAdapter(
    private val context: Context,
    private val jobTypeViewModel: DataJobTypeViewModel,
    private val onDeleteClickListener: OnDeleteClickListener
) : ListAdapter<DataDailyModel, UploadAdapter.ViewHolder>(ItemDiffCallback()) {

    private var isDescendingOrder = true

    class ViewHolder(itemView: View, onDeleteClickListener: OnDeleteClickListener) :
        RecyclerView.ViewHolder(itemView) {
        val itemTitle: TextView = itemView.findViewById(R.id.tvTitleListUpload)
        val itemDate: TextView = itemView.findViewById(R.id.tvDateListUpload)
        val itemJob: TextView = itemView.findViewById(R.id.tvJobListUpload)
        val iconList: ImageView = itemView.findViewById(R.id.ivIconList)
        val viewList: View = itemView.findViewById(R.id.viewListUpload)
        val deleteButton: FloatingActionButton = itemView.findViewById(R.id.fbDelData)

        private lateinit var currentItem: DataDailyModel

        init {
            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClickListener.onDeleteClick(currentItem)
                }
            }
        }

        fun bind(item: DataDailyModel) {
            currentItem = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_list_upload, parent, false)
        return ViewHolder(view, onDeleteClickListener)
    }

    @SuppressLint("ResourceType", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.deleteButton.visibility = if (item.archive == 0) View.VISIBLE else View.GONE
        holder.bind(item)

        when (position % 5) {
            0 -> {
                holder.viewList.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.colorPrimary)
                holder.iconList.setColorFilter(
                    context.resources.getColor(
                        R.color.colorPrimary,
                        null
                    ), PorterDuff.Mode.SRC_IN
                )
            }
            1 -> {
                holder.viewList.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.list1)
                holder.iconList.setColorFilter(
                    context.resources.getColor(
                        R.color.list1,
                        null
                    ), PorterDuff.Mode.SRC_IN
                )
            }
            2 -> {
                holder.viewList.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.list2)
                holder.iconList.setColorFilter(
                    context.resources.getColor(
                        R.color.list2,
                        null
                    ), PorterDuff.Mode.SRC_IN
                )
            }
            3 -> {
                holder.viewList.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.list3)
                holder.iconList.setColorFilter(
                    context.resources.getColor(
                        R.color.list3,
                        null
                    ), PorterDuff.Mode.SRC_IN
                )
            }
            4 -> {
                holder.viewList.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.list4)
                holder.iconList.setColorFilter(
                    context.resources.getColor(
                        R.color.list4,
                        null
                    ), PorterDuff.Mode.SRC_IN
                )
            }
        }

        holder.itemTitle.text = item.no_daily
        holder.itemDate.text = AppUtils.formatDate(item.no_daily.substring(0, 11))

        jobTypeViewModel.dataJobTypeList.observe(context as LifecycleOwner) { data ->
            for (dataWl in data) {
                if (dataWl.id == item.id_jnsdr) {
                    holder.itemJob.text = dataWl.nama.ifEmpty { "" }
                    break
                }
            }
        }
        jobTypeViewModel.loadDataJobType()

        val sortedList = currentList.sortedWith(
            if (isDescendingOrder) compareByDescending<DataDailyModel> {
                it.id
            } else compareBy<DataDailyModel> {
                it.id
            }
        )
        val isLastItemInSorted = sortedList.indexOf(item) == sortedList.size - 1

        val marginBottom = if (isLastItemInSorted) {
            50
        } else {
            0
        }

        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.bottomMargin = marginBottom
        holder.itemView.layoutParams = layoutParams

        val animation = AnimationUtils.loadAnimation(holder.itemView.context, android.R.anim.slide_in_left)
        holder.itemView.startAnimation(animation)
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<DataDailyModel>() {
        override fun areItemsTheSame(
            oldItem: DataDailyModel,
            newItem: DataDailyModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: DataDailyModel,
            newItem: DataDailyModel
        ): Boolean {
            return oldItem == newItem
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun toggleSortingOrder() {
        isDescendingOrder = !isDescendingOrder
        submitList(
            currentList.sortedWith(
                if (isDescendingOrder) compareByDescending<DataDailyModel> {
                    it.id
                } else compareBy<DataDailyModel> {
                    it.id
                }
            )
        )
        notifyDataSetChanged()
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(item: DataDailyModel)
    }
}