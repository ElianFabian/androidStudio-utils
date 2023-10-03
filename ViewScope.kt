import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * <item name="view_coroutine_scope" type="id" />
 */
private val JOB_KEY = R.id.view_coroutine_scope


val View.viewScope: CoroutineScope
	get() {
		val scope = getTag(JOB_KEY) as? ViewCoroutineScope

		if (scope != null) {
			return scope
		}

		val newScope = ViewCoroutineScopeImpl(
			view = this,
			coroutineContext = SupervisorJob() + Dispatchers.Main.immediate,
		)

		newScope.register()

		return newScope
	}

abstract class ViewCoroutineScope : CoroutineScope {
	protected abstract val view: View
}

private class ViewCoroutineScopeImpl(
	override val view: View,
	override val coroutineContext: CoroutineContext,
) : ViewCoroutineScope(),
	View.OnAttachStateChangeListener {

	init {
		if (!view.isAttachedToWindow) {
			coroutineContext.cancel()
		}
	}

	fun register() {
		launch(Dispatchers.Main.immediate) {
			if (!view.isAttachedToWindow) {
				coroutineContext.cancel()
				return@launch
			}

			view.addOnAttachStateChangeListener(this@ViewCoroutineScopeImpl)
		}
	}

	override fun onViewAttachedToWindow(v: View) {
		view.setTag(JOB_KEY, this)
	}

	override fun onViewDetachedFromWindow(v: View) {
		view.removeOnAttachStateChangeListener(this)
		coroutineContext.cancel()
		view.setTag(JOB_KEY, null)
	}
}
