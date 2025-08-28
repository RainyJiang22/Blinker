package com.blinker.video.ui.pages.detail

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import com.blinker.video.R
import com.blinker.video.databinding.LayoutFeedDetailCommentDialogBinding
import com.blinker.video.model.TopComment
import com.blinker.video.ui.pages.publish.CaptureActivity
import com.blinker.video.ui.pages.publish.UploadFileManager
import com.blinker.video.ui.utils.invokeViewBinding
import com.blinker.video.ui.utils.invokeViewModel
import com.blinker.video.ui.utils.setImageUrl
import com.blinker.video.ui.utils.setVisibility
import com.google.android.exoplayer2.util.MimeTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommentDialog : AppCompatDialogFragment() {
    private var itemId: Long = 0
    private var mListener: ICommentListener? = null
    private var width: Int = 0
    private var height: Int = 0
    private var filePath: String? = null
    private var mimeType: String? = null
    private val viewModel: FeedCommentViewModel by invokeViewModel()
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // it.data
            if (it.resultCode == RESULT_OK && it.data != null) {
                val data = it.data ?: return@registerForActivityResult
                width = data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH, 0)
                height = data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT, 0)
                filePath = data.getStringExtra(CaptureActivity.RESULT_FILE_PATH)
                mimeType = data.getStringExtra(CaptureActivity.RESULT_FILE_TYPE)
                if (!TextUtils.isEmpty(filePath)) {
                    viewBinding.commentExtLayout.setVisibility(false)
                    viewBinding.commentCover.setImageUrl(filePath)
                    viewBinding.commentIconVideo.setVisibility(MimeTypes.isVideo(mimeType))
                    viewBinding.commentVideo.isEnabled = false
                    viewBinding.commentVideo.imageAlpha = 80
                }
            }
        }
    private val viewBinding: LayoutFeedDetailCommentDialogBinding by invokeViewBinding()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val window = dialog.window ?: return dialog
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.decorView.setPadding(0, 0, 0, 0)
        window.setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        val attributes = window.attributes
        attributes.gravity = Gravity.BOTTOM
        attributes.horizontalMargin = 0f
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT

        window.attributes = attributes
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return viewBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.itemId = arguments?.getLong(KEY_ITEM_ID) ?: 0L
        if (itemId <= 0) {
            dismiss()
            return
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setItemId(itemId)
        viewBinding.commentVideo.setOnClickListener {
            activityResultLauncher.launch(Intent(context, CaptureActivity::class.java))
        }

        viewBinding.commentDelete.setOnClickListener {
            filePath = null
            mimeType = null
            width = 0
            height = 0
            viewBinding.commentCover.setImageDrawable(null)
            viewBinding.commentExtLayout.setVisibility(false)
            viewBinding.commentVideo.isEnabled = true
            viewBinding.commentVideo.imageAlpha = 255
        }

        viewBinding.commentSend.setOnClickListener {
            publish()
        }

        view.post {
            viewBinding.inputView.isFocusable = true
            viewBinding.inputView.isFocusableInTouchMode = true
            viewBinding.inputView.requestFocus()

            val manager =
                this.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    ?: return@post
            manager.showSoftInput(viewBinding.inputView, 0)
        }
    }

    private fun publish() {
        if (TextUtils.isEmpty(viewBinding.inputView.editableText.toString())) {
            return
        }
        updatePublishUI(true)
        lifecycleScope.launch {
            if (!TextUtils.isEmpty(filePath)) {
                UploadFileManager.upload(
                    requireContext(),
                    filePath!!,
                    mimeType!!
                ) { coverFileUploadUrl, originalFileUploadUrl ->
                    if (TextUtils.isEmpty(originalFileUploadUrl) || MimeTypes.isVideo(mimeType) && TextUtils.isEmpty(
                            coverFileUploadUrl
                        )
                    ) {
                        updatePublishUI(true)
                        return@upload
                    }
                    lifecycleScope.launch {
                        publishComment(coverFileUploadUrl, originalFileUploadUrl)
                    }
                }
            } else {
                publishComment()
            }
        }
    }

    private suspend fun publishComment(
        coverFileUploadUrl: String? = null,
        originalFileUploadUrl: String? = null,
    ) {
        val topComment = viewModel.publishComment(
            viewBinding.inputView.editableText.toString(),
            originalFileUploadUrl,
            coverFileUploadUrl,
            width,
            height
        )
        if (topComment == null) {
            withContext(Dispatchers.Main) {
                updatePublishUI(false)
                Toast.makeText(requireContext(), R.string.comment_publish_fail, Toast.LENGTH_SHORT)
                    .show()
            }
            return
        }
        mListener?.onAddComment(topComment)
        dismiss()
    }

    override fun dismiss() {
        super.dismiss()
        filePath = null
        width = 0
        height = 0
        mimeType = null
    }

    private fun updatePublishUI(publishing: Boolean) {
        viewBinding.commentSend.setVisibility(!publishing)
        viewBinding.actionPublish.setVisibility(publishing)
        if (publishing) viewBinding.actionPublish.show() else viewBinding.actionPublish.hide()
    }

    interface ICommentListener {
        fun onAddComment(comment: TopComment)
    }

    fun setCommentAddListener(listener: ICommentListener) {
        this.mListener = listener
    }

    companion object {
        private const val KEY_ITEM_ID = "key_item_id"

        @JvmStatic
        fun newInstance(itemId: Long): CommentDialog {
            val args = Bundle()
            args.putLong(KEY_ITEM_ID, itemId)
            val fragment = CommentDialog()
            fragment.arguments = args
            return fragment
        }
    }
}