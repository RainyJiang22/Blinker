package com.blinker.video.navigation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.blinker.video.R
import com.blinker.video.databinding.LayoutFragmentCategoryBinding
import com.blinker.video.navigation.base.BaseFragment

/**
 * @author jiangshiyu
 * @date 2024/12/9
 */
class CategoryFragment : BaseFragment() {

    private lateinit var categoryBinding: LayoutFragmentCategoryBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        categoryBinding = LayoutFragmentCategoryBinding.inflate(inflater,container,false)
        return categoryBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoryBinding.navigateToTagsFragment.setOnClickListener {
            findNavController().navigate(R.id.tags_fragment)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("outState","我是categoryFragment")
    }
}