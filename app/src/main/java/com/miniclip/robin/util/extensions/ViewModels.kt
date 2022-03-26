package com.miniclip.robin.util.extensions

import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.miniclip.robin.SimApplication
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Property delegate to bind a ViewModel with a single property.
 * Usage:
 *
 *     private val viewModel: MainViewModel by fragmentViewModel()
 *
 * @throws IllegalStateException if viewModel is accessed before Fragment is added to an Activity
 * @return ViewModel of type T
 */
inline fun <reified T: ViewModel> Fragment.fragmentViewModel(): ReadOnlyProperty<Fragment, T> {
    val clazz = T::class.java
    return FragmentViewModelDelegate(clazz)
}

class FragmentViewModelDelegate<T: ViewModel>(private val clazz: Class<T>) : ReadOnlyProperty<Fragment, T> {

    private var vm: T? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        vm?.let { return it }

        if (!thisRef.isAdded) {
            error("Attempt to access ViewModel [${clazz.name}] while Fragment is not attached.")
        }

        return ViewModelProvider(thisRef)[clazz].also {
            vm = it
        }
    }
}

/**
 * Gets the [SimApplication] context.
 */
val AndroidViewModel.application get() = getApplication<SimApplication>()