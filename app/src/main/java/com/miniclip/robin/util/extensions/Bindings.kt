package com.miniclip.robin.util.extensions

import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Utility to automatically bind views in a Fragment
 * Example usage:
 *
 *     val views: MyViewBinding by viewBinding()
 */
inline fun <reified T : ViewBinding> Fragment.viewBinding(): FragmentViewBindingDelegate<T> {
    val method = T::class.java.getDeclaredMethod("bind", View::class.java)
    return FragmentViewBindingDelegate(this) { view -> method.invoke(null, view) as T }
}

/**
 * Utility to lazy inflate and bind views in an Activity
 * Example usage:
 *
 *     val views: ActivityMainBinding by viewBinding()
 *
 *     override fun onCreate() {
 *         setContentView(views.root)
 *     }
 */
inline fun <reified T : ViewBinding> AppCompatActivity.viewBinding(): Lazy<T> {
    val method = T::class.java.getDeclaredMethod("inflate", LayoutInflater::class.java)
    return lazy(LazyThreadSafetyMode.NONE) { method.invoke(null, LayoutInflater.from(this)) as T }
}

/**
 * Property delegate to auto bind ViewBindings with the Fragment views lifecycle.
 */
@Keep
class FragmentViewBindingDelegate<T : ViewBinding>(
    fragment: Fragment,
    val viewBindingFactory: (View) -> T
) : ReadOnlyProperty<Fragment, T> {

    private var binding: T? = null

    init {
        fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
            if (viewLifecycleOwner == null) {
                // view destroyed
                binding = null
            }
        }
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        binding?.let { return it }

        val lifecycle = thisRef.viewLifecycleOwnerLiveData.value?.lifecycle
        if (lifecycle == null || !lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException("Should not attempt to get bindings when Fragment views are not initialized.")
        }

        return viewBindingFactory(thisRef.requireView()).also { this.binding = it }
    }
}

/**
 * Simple ViewHolder that takes and exposes a ViewBinding object.
 *
 * Example usage:
 *
 *      override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<MenuBinding> {
 *           val views = MenuBinding.inflate(LayoutInflater.from(parent.context))
 *           return BindingHolder(views)
 *       }
 */
class BindingHolder<T : ViewBinding>(val views: T) : RecyclerView.ViewHolder(views.root)