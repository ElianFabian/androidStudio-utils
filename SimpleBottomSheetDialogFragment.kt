@file:Suppress("NOTHING_TO_INLINE")

import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


/**
 * A BottomSheetDialogFragment that allows type safe and lifecycle safe handling of arguments and events,
 * even during process death and configuration changes.
 *
 * When defining `TArgs`, it should be a data class that implements Parcelable with the @Parcelize annotation.
 * Alternatively, it can be a sealed class or interface depending on your use case requirements.
 *
 * The `TEvent` should implement Parcelable with the @Parcelize annotation.
 * In most cases, it is recommended to use a sealed class or interface to handle different types of events.
 *
 * By default, you can send your arguments using the `show()` function. However, if you need or prefer to
 * send them when initializing the dialog, you can also define a custom `newInstance()` function.
 */
abstract class SimpleBottomSheetDialogFragment<TArgs : Parcelable, TEvent : Parcelable> : BottomSheetDialogFragment() {

    /**
     * Shows the dialog with the specified arguments.
     *
     * @param manager The FragmentManager instance to show the dialog.
     * @param args The arguments to be passed to the dialog.
     */
    fun show(manager: FragmentManager, args: TArgs) {

        arguments = createDialogArgsBundle(args)

        show(manager)
    }

    /**
     * Shows the dialog without any arguments.
     *
     * @param manager The FragmentManager instance to show the dialog.
     */
    inline fun show(manager: FragmentManager) = show(manager, "")

    /**
     * Clears the previously set dialog event.
     */
    inline fun clearDialogEvent() = clearFragmentResult("$id")

    /**
     * Clears the previously set dialog event listener.
     */
    inline fun clearDialogEventListener() = clearFragmentResultListener("$id")

    /**
     * Sets an event listener for the specified fragment.
     * The `onEvent` lambda will be executed whenever the `sendDialogEvent()` function is called.
     *
     * @param fragment The fragment instance on which to set the event listener.
     * @param onEvent The lambda that will be invoked when an event is received.
     * @throws IllegalArgumentException if attempting to set an event listener on the same dialog fragment instance.
     */
    inline fun setOnEventListener(
        fragment: Fragment,
        crossinline onEvent: (event: TEvent) -> Unit,
    ) {
        if (fragment == this) {
            throw IllegalStateException("Cannot set event listener on the same dialog fragment instance: ${fragment::class.qualifiedName}.")
        }

        setOnEventListener(fragment.parentFragmentManager, fragment.viewLifecycleOwner, onEvent)
    }

    /**
     * Sets an event listener for the specified activity.
     * The `onEvent` lambda will be executed whenever the `sendDialogEvent()` function is called.
     *
     * @param activity The FragmentActivity instance on which to set the event listener.
     * @param onEvent The lambda that will be invoked when an event is received.
     */
    inline fun setOnEventListener(
        activity: FragmentActivity,
        crossinline onEvent: (event: TEvent) -> Unit,
    ) {
        setOnEventListener(activity.supportFragmentManager, activity, onEvent)
    }

    /**
     * Sets an event listener for the specified fragment manager and lifecycle owner.
     * The `onEvent` lambda will be executed whenever the `sendDialogEvent()` function is called.
     *
     * @param fragmentManager The fragment manager instance.
     * @param lifecycleOwner The lifecycle owner for the event listener.
     * @param onEvent The lambda that will be invoked when an event is received.
     * @throws IllegalArgumentException if attempting to set an event listener using the same dialog fragment lifecycle owner.
     */
    inline fun setOnEventListener(
        fragmentManager: FragmentManager,
        lifecycleOwner: LifecycleOwner,
        crossinline onEvent: (event: TEvent) -> Unit,
    ) {
        if (lifecycleOwner == this.viewLifecycleOwner) {
            throw IllegalArgumentException("Cannot use the lifecycle owner of the same dialog.")
        }

        fragmentManager.setFragmentResultListener(
            "$id",
            lifecycleOwner,
        ) { _, bundle ->
            onEvent(bundle.getParcelable("event")!!)
        }
    }


    /**
     * Sends an event that will be received by the `onEvent` lambda parameter of the `setOnEventListener()` function.
     *
     * @param event The event object to send.
     */
    protected inline fun sendDialogEvent(event: TEvent) = setFragmentResult("$id", bundleOf("event" to event))

    /**
     * Retrieves the arguments (`TArgs`) that were passed when the dialog was shown or instantiated using a custom `newInstance()` function.
     *
     * @return The dialog arguments if available, or null if no arguments were provided.
     */
    protected inline fun getDialogArgs(): TArgs? = arguments?.getParcelable("args")

    /**
     * Creates a bundle with the specified `TArgs`.
     * This function should be used when defining a `newInstance()` function.
     *
     * @param args The arguments object to include in the bundle.
     * @return A bundle containing the specified arguments.
     */
    protected inline fun createDialogArgsBundle(args: TArgs): Bundle = bundleOf("args" to args)
}
