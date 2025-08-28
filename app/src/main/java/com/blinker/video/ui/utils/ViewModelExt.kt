package com.blinker.video.ui.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * @author jiangshiyu
 * @date 2024/12/14
 */


inline fun <reified VM : ViewModel> invokeViewModel() = FragmentViewModelProperty(VM::class.java)

class FragmentViewModelProperty<VM : ViewModel>(private val clazz: Class<VM>) :
    ReadOnlyProperty<Any, VM> {
    private var vm: VM? = null
    override fun getValue(thisRef: Any, property: KProperty<*>): VM {
        if (thisRef !is ViewModelStoreOwner) {
            throw java.lang.IllegalStateException("invokeViewModel can only be used in ViewModelStoreOwner instance")
        }
        if (vm == null) {
            vm = ViewModelProvider(thisRef, ViewModelProvider.NewInstanceFactory())[clazz]
        }
        return vm!!
    }
}


