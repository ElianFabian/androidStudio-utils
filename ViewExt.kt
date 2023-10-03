import android.view.View.OnClickListener

private val ON_CLICK_LISTENERS_KEY = R.id.on_click_listeners

private val View.onClickListeners: MutableList<OnClickListener>
	get() {
		val listeners = getTag(ON_CLICK_LISTENERS_KEY) as? MutableList<OnClickListener> ?: mutableListOf()

		setTag(ON_CLICK_LISTENERS_KEY, listeners)

		return listeners
	}

fun View.addOnClickListener(listener: OnClickListener) {
	if (getTag(ON_CLICK_LISTENERS_KEY) == null && hasOnClickListeners()) {
		Log.w(
			"ViewExt.addOnClickListener",
			"addOnClickListener() should not be used if you have already used setOnClickListener()",
		)
		return
	}

	val listeners = onClickListeners

	listeners.add(listener)

	setOnClickListener {
		listeners.forEach { it.onClick(this) }
	}
}

fun View.removeOnClickListener(listener: OnClickListener) {
	val listeners = onClickListeners

	if (listeners.isEmpty()) {
		return
	}

	listeners.remove(listener)

	if (listeners.isEmpty()) {
		setOnClickListener(null)
	}
}

suspend fun View.awaitClick() {
	suspendCancellableCoroutine { continuation ->
		val listener = object : OnClickListener {
			override fun onClick(v: View) {
				removeOnClickListener(this)
				continuation.resume(Unit)
			}
		}

		addOnClickListener(listener)

		continuation.invokeOnCancellation {
			removeOnClickListener(listener)
		}
	}
}
