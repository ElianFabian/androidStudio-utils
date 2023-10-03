import android.view.View.OnClickListener

private val ON_CLICK_LISTENERS_KEY = R.id.on_click_listeners

private val View.onClickListeners: MutableList<OnClickListener>
	get() {
		val listeners = getTag(ON_CLICK_LISTENERS_KEY) as? MutableList<OnClickListener>

		setTag(ON_CLICK_LISTENERS_KEY, listeners)

		return listeners ?: mutableListOf()
	}

fun View.addOnClickListener(listener: OnClickListener) {
	// Do nothing if setOnClickListener was used to prevent weird behaviors.
	// This function is not designed to be used with setOnClickListener.
	if (getTag(ON_CLICK_LISTENERS_KEY) == null && hasOnClickListeners()) {
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
