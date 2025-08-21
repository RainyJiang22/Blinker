package com.blinker.video.ui.pages.publish

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blinker.video.databinding.ActivityLayoutPublishBinding
import com.blinker.video.ui.utils.invokeViewBinding

/**
 * @author jiangshiyu
 * @date 2025/8/20
 */
class PublishActivity : AppCompatActivity() {


    private val viewBinding: ActivityLayoutPublishBinding by invokeViewBinding()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
    }
}