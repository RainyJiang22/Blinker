package com.blinker.video.ui.utils

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * @author jiangshiyu
 * @date 2024/12/14
 */
inline fun <reified VB : ViewBinding> invokeViewBinding() =
    FragmentInflateBindingProperty(VB::class.java)


class FragmentInflateBindingProperty<VB : ViewBinding>(private val clazz: Class<VB>) :
    ReadOnlyProperty<Fragment, VB> {
    private var binding: VB? = null
    override fun getValue(thisRef: Fragment, property: KProperty<*>): VB {
        if (binding == null) {
            try {
                binding = (clazz.getMethod("inflate", LayoutInflater::class.java)
                    .invoke(null, thisRef.layoutInflater) as VB)
            } catch (e: java.lang.IllegalStateException) {
                e.printStackTrace()
                throw e
            }
            thisRef.viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    super.onDestroy(owner)
                    binding = null
                }
            })
        }
        return binding!!
    }
}

inline fun <reified VM : ViewModel> invokeViewModel() = FragmentViewModelProperty(VM::class.java)


class FragmentViewModelProperty<VM : ViewModel>(private val clazz: Class<VM>) :
    ReadOnlyProperty<Fragment, VM> {
    private var vm: VM? = null
    override fun getValue(thisRef: Fragment, property: KProperty<*>): VM {
        if (vm == null) {
            vm = ViewModelProvider(thisRef, ViewModelProvider.NewInstanceFactory())[clazz]
        }
        return vm!!
    }
}

