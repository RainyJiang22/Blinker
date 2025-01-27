package com.blinker.video.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType


abstract class BaseActivity<V : ViewBinding, VM : AndroidViewModel> : FragmentActivity() {


    var binding: V? = null

    open val viewModel: VM by lazy {
        val types = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments
        val clazz = types[1] as Class<VM>
        ViewModelProvider(
            viewModelStore,
            ViewModelProvider.AndroidViewModelFactory(application)
        ).get(clazz)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initContentView()
        intent.apply {
            extras?.apply {
                onBundle(this)
            }
        }
        init(savedInstanceState)
    }

    override fun setContentView(layoutResID: Int) {
    }

    open fun initContentView() {
        val types = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments
        val aClass = types[0] as Class<V>
        try {
            binding =
                aClass.getDeclaredMethod("inflate", LayoutInflater::class.java).invoke(null, layoutInflater) as V?
            super.setContentView(binding?.root)
        } catch (e: Error) {
            e.printStackTrace();
        }
    }

    abstract fun onBundle(bundle: Bundle)

    abstract fun init(savedInstanceState: Bundle?)


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }


    fun binding(block: V?.() -> Unit) {
        block.invoke(binding)
    }


}