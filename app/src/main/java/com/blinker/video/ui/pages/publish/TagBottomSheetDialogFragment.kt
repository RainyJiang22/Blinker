package com.blinker.video.ui.pages.publish

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blinker.video.R
import com.blinker.video.databinding.LayoutTagBottomSheetDialogBinding
import com.blinker.video.http.ApiService
import com.blinker.video.model.TagList
import com.blinker.video.ui.pages.login.UserManager
import com.blinker.video.ui.utils.PixUtil
import com.blinker.video.ui.utils.invokeViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author jiangshiyu
 * @date 2025/8/22
 */
class TagBottomSheetDialogFragment : BottomSheetDialogFragment() {
    private var listener: OnTagItemSelectedListener? = null
    private val viewBinding: LayoutTagBottomSheetDialogBinding by invokeViewBinding()
    private val mTagLists = mutableListOf<TagList>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // dialog.setContentView(viewBinding.root)
        val parent = viewBinding.root.parent as ViewGroup
        val behavior = BottomSheetBehavior.from(parent)
        behavior.peekHeight = PixUtil.getScreenHeight() / 3
        behavior.isHideable = false

        val layoutParams = parent.layoutParams
        layoutParams.height = PixUtil.getScreenHeight() / 3 * 2
        parent.layoutParams = layoutParams

        viewBinding.recyclerView.layoutManager = LinearLayoutManager(context)
        viewBinding.recyclerView.adapter =TagsAdapter()

        queryTagList()
    }

    private fun queryTagList() {
        lifecycleScope.launch {
            kotlin.runCatching {
                ApiService.getService().getTagList(UserManager.userId())
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    mTagLists.addAll(it.body!!)
                    viewBinding.recyclerView.adapter?.notifyDataSetChanged()
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "标签数据获取失败,请重试", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    inner class TagsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val textView = TextView(parent.context)
            textView.textSize = 13f
            textView.typeface = Typeface.DEFAULT_BOLD
            textView.setTextColor(ContextCompat.getColor(parent.context, R.color.color_000))
            textView.gravity = Gravity.CENTER_VERTICAL
            textView.layoutParams = RecyclerView.LayoutParams(-1, PixUtil.dp2px(45))
            return object : RecyclerView.ViewHolder(textView) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val textView = holder.itemView as TextView
            val tagList: TagList = mTagLists[position]
            textView.text = tagList.title
            holder.itemView.setOnClickListener {
                listener?.onTagItemSelected(tagList)
                dismiss()
            }
        }

        override fun getItemCount(): Int {
            return mTagLists.size
        }
    }

    fun setOnTagItemSelectedListener(listener: OnTagItemSelectedListener) {
        this.listener = listener
    }

    interface OnTagItemSelectedListener {
        fun onTagItemSelected(tagList: TagList)
    }
}