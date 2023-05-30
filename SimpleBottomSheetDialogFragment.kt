@file:Suppress("NOTHING_TO_INLINE")

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * A BottomSheetDialogFragment that allows type safe and lifecycle safe handling of arguments and events,
 * even during process death and configuration changes.
 *
 * When defining `TArgs`, it should be a data class that implements Parcelable with the @Parcelize annotation.
 * Alternatively, it can be a sealed class/interface depending on your use case requirements.
 *
 * The `TEvent` should implement Parcelable with the @Parcelize annotation (remember to add the kotlin-parcelize plugin in the gradle).
 * In most cases, it is recommended to use a sealed class/interface to handle different types of events.
 *
 * By default, you can send your arguments using the `show()` function. However, if you need or prefer to
 * send them when initializing the dialog, you can also define a custom `newInstance()` function.
 *
 * This could be an example to show a dialog inside a Fragment:
 *
 * ```
 * // Define the dialog class (if you don't require an Args class use the Nothing class instead)
 * class MyBottomSheetDialogFragment : SimpleBottomSheetDialogFragment<MyBottomSheetDialogFragment.Args, MyBottomSheetDialogFragment.Event>() {
 * 	[...]
 * }
 *
 *
 * //// Inside the MyBottomSheetDialogFragment class
 *
 * // Args class
 * @Parcelize
 * data class Args(
 * 	val firstArg: String,
 * 	val secondArg: Int,
 * ) : Parcelable
 *
 * // Event class
 * sealed interface Event : Parcelable {
 * 	@Parcelize
 * 	class OnAccept(val data: String) : Event
 *
 * 	@Parcelize
 * 	object OnReject : Event
 * }
 *
 * // Get the args (for example in onViewCreated())
 * val args: Args = dialogArguments
 *
 * // Send an event from the dialog
 * btnAccept.setOnClickListener {
 * 	sendDialogEvent(Event.OnAccept(data = "important data"))
 * }
 * btnReject.setOnClickListener {
 * 	sendDialogEvent(Event.OnReject)
 * }
 *
 * // Define a custom newInstance() function
 * companion object {
 * 	fun newInstance(id: String? = null, args: Args? = null) = baseNewInstance(id, args) {
 * 		MyBottomSheetDialogFragment()
 * 	}
 * }
 *
 *
 * //// Inside MyFragment class
 *
 * // Instantiate dialog
 * val dialog = ChooseOrDeleteProfilePictureBottomDialog.newInstance(
 * 	MyBottomSheetDialogFragment.Args(
 * 		firstArg = "some data",
 * 		secondArg = "more data",
 * 	)
 * )
 *
 * // Show the dialog
 * dialog.show(this@MyBottomSheetDialogFragment)
 * // If you need to pass arguments when showing the dialog instead of when instantiating it
 * dialog.show(this@MyBottomSheetDialogFragment, args = Args(firstArg = "some data", secondArg = "more data")
 *
 * // Set the event listener
 * dialog.setOnEventListener(this@MyFrgment) { event ->
 * 	when (event) {
 * 		is MyBottomSheetDialogFragment.Event.OnAccept -> {
 * 			// Do something
 * 		}
 * 		is MyBottomSheetDialogFragment.Event.OnReject -> {
 * 			// Do something
 * 		}
 * 	}
 * }
 * ```
 */
abstract class SimpleBottomSheetDialogFragment<TArgs : Parcelable, TEvent : Parcelable> : BottomSheetDialogFragment {

    init {
        dialogId = this::class.qualifiedName
    }

    constructor()

    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)


    var dialogId: String? = null
        protected set


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dialogId = dialogId ?: savedInstanceState?.getString("dialogId") ?: this::class.qualifiedName
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("dialogId", dialogId)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        dialogId = savedInstanceState?.getString("dialogId") ?: return
    }


    /**
     * Shows the dialog with the specified arguments.
     *
     * @param fragment The fragment that shows the dialog.
     * @param args The arguments to be passed to the dialog.
     *
     * @throws IllegalStateException if the specified fragment is the same as the dialog instance.
     */
    inline fun show(fragment: Fragment, args: TArgs? = null) {

        if (fragment == this) {
            throw IllegalStateException("Cannot use the same dialog instance to show itself: ${fragment::class.qualifiedName}.")
        }

        show(fragment.childFragmentManager, args)
    }

    /**
     * Shows the dialog with the specified arguments.
     *
     * @param activity The activity that shows the dialog.
     * @param args The arguments to be passed to the dialog.
     */
    inline fun show(activity: FragmentActivity, args: TArgs? = null) {

        show(activity.supportFragmentManager, args)
    }

    /**
     * Shows the dialog with the specified arguments.
     *
     * @param manager The FragmentManager instance to show the dialog.
     * @param args The arguments to be passed to the dialog.
     */
    fun show(manager: FragmentManager, args: TArgs? = null) {

        if (args != null) {
            arguments = createBundleFromDialogArgs(args)
        }

        show(manager, dialogId)
    }

    /**
     * Sets an event listener for the specified fragment.
     * The `onEvent` lambda will be executed whenever the `sendDialogEvent()` function is called.
     *
     * @param fragment The fragment instance on which to set the event listener.
     * @param onEvent The lambda that will be invoked when an event is received.
     *
     * @throws IllegalArgumentException if attempting to set an event listener on the same dialog fragment instance.
     */
    inline fun setEventListener(
        fragment: Fragment,
        crossinline onEvent: (event: TEvent) -> Unit,
    ) {
        if (fragment == this) {
            throw IllegalStateException("Cannot set event listener on the same dialog fragment instance: ${fragment::class.qualifiedName}.")
        }

        setEventListener(fragment.childFragmentManager, fragment.viewLifecycleOwner, onEvent)
    }

    /**
     * Sets an event listener for the specified activity.
     * The `onEvent` lambda will be executed whenever the `sendDialogEvent()` function is called.
     *
     * @param activity The FragmentActivity instance on which to set the event listener.
     * @param onEvent The lambda that will be invoked when an event is received.
     */
    inline fun setEventListener(
        activity: FragmentActivity,
        crossinline onEvent: (event: TEvent) -> Unit,
    ) {
        setEventListener(activity.supportFragmentManager, activity, onEvent)
    }

    /**
     * Sets an event listener for the specified fragment manager and lifecycle owner.
     * The `onEvent` lambda will be executed whenever the `sendDialogEvent()` function is called.
     *
     * @param fragmentManager The fragment manager instance.
     * @param lifecycleOwner The lifecycle owner for the event listener.
     * @param onEvent The lambda that will be invoked when an event is received.
     */
    inline fun setEventListener(
        fragmentManager: FragmentManager,
        lifecycleOwner: LifecycleOwner,
        crossinline onEvent: (event: TEvent) -> Unit,
    ) {
        fragmentManager.setFragmentResultListener(
            dialogId!!,
            lifecycleOwner,
        ) { _, bundle ->
            onEvent(bundle.getParcelable("event")!!)
        }
    }

    /**
     * Clears the previously set dialog event.
     */
    inline fun clearDialogEvent() = clearFragmentResult(dialogId!!)

    /**
     * Clears the previously set dialog event listener.
     */
    inline fun clearDialogEventListener() = clearFragmentResultListener(dialogId!!)


    /**
     * Sends an event that will be received by the `onEvent` lambda parameter of the `setOnEventListener()` function.
     *
     * @param event The event object to send.
     */
    protected inline fun sendDialogEvent(event: TEvent) = setFragmentResult(dialogId!!, bundleOf("event" to event))

    /**
     * Retrieves the arguments (`TArgs`) that were passed when the dialog it's shown or instantiated using a custom `newInstance()` function.
     *
     * @return The dialog arguments if available, or null if no arguments were provided.
     */
    protected inline val dialogArguments: TArgs? get() = arguments?.getParcelable("args")

    /**
     * Creates a bundle with the specified `TArgs`.
     *
     * @param args The arguments object to include in the bundle.
     * @return A bundle containing the specified arguments.
     */
    protected inline fun createBundleFromDialogArgs(args: TArgs): Bundle = bundleOf("args" to args)


    protected companion object {

        /**
         * Returns a new instance of the given SimpleBottomSheetDialog in getNewInstance
         * with the specified id and args.
         *
         * @param id Should identify a single `SimpleBottomSheetDialogFragment` instance. Use it when multiple instances
         * are required in the same fragment or activity to allow a correct behavior.
         * @param args The data to pass when instantiating the dialog.
         * @param getNewInstance A lambda function that creates the instance of the desired `SimpleBottomSheetDialogFragment`.
         *
         * @return A new instance of the specified `SimpleBottomSheetDialogFragment`.
         */
        inline fun <TArgs : Parcelable,TEvent : Parcelable, T : SimpleBottomSheetDialogFragment<TArgs, TEvent>> baseNewInstance(
            id: String? = null,
            args: TArgs? = null,
            getNewInstance: () -> T,
        ): T {
            return getNewInstance().apply {

                dialogId = id ?: this::class.qualifiedName

                if (args != null) {
                    arguments = createBundleFromDialogArgs(args)
                }
            }
        }
    }
}
