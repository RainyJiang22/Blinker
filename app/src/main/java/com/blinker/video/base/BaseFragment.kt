package com.blinker.video.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * @author jiangshiyu
 * @date 2024/12/9
 */
open class BaseFragment : Fragment() {

    private val TAG = this::class.java.simpleName + "-" + this.hashCode()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("fragmentLife", "${TAG}-onCreate: ")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.e("fragmentLife", "${TAG}-onCreateView: ")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        Log.e("fragmentLife", "${TAG}-onResume: ")
    }

    override fun onPause() {
        super.onPause()
        Log.e("fragmentLife", "${TAG}-onPause: ")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e("fragmentLife", "${TAG}-onDestroyView: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("fragmentLife", "${TAG}-onDestroy: ")
    }
}