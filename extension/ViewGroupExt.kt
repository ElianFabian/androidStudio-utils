import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes

// https://stackoverflow.com/questions/8817377/android-how-to-find-multiple-views-with-common-attribute

private fun findViewsWithTag(tag: String, root: ViewGroup): List<View>
{
	val views = mutableListOf<View>()
	val childCount = root.childCount

	for (i in 0 until childCount)
	{
		val child = root.getChildAt(i)

		if (child.tag != tag) continue

		when (child)
		{
			is ViewGroup -> views.addAll(findViewsWithTag(tag, child))
			else         -> views.add(child)
		}
	}
	return views
}

fun ViewGroup.findViewsWithTag(tag: String) = findViewsWithTag(tag, this)

fun ViewGroup.findViewsWithTag(@StringRes tag: Int) = findViewsWithTag(context.getString(tag), this)

// It's public because of inline limitations
inline fun <reified T : View> findViewsOfTypeFromRoot(root: ViewGroup): List<T>
{
	val views = mutableListOf<View>()

	var currentParent: ViewGroup? = root
	val viewGroupList = ArrayDeque<ViewGroup>()

	var childIndex = 0

	while (currentParent != null)
	{
		val child = currentParent.getChildAt(childIndex)

		if (child == null)
		{
			currentParent = viewGroupList.removeFirstOrNull()
			childIndex = 0
			continue
		}
		if (child !is T && child !is ViewGroup)
		{
			childIndex++
			continue
		}
		if (child is ViewGroup) viewGroupList.add(child)

		childIndex++
	}
	@Suppress("UNCHECKED_CAST")
	return views as List<T>
}

inline fun <reified T : View> ViewGroup.findViewsOfType() = findViewsOfTypeFromRoot<T>(this)

inline fun <reified T : View> ViewGroup.findViewsWithTagOfType(tag: String) = findViewsWithTagOfType<T>(tag, this)

inline fun <reified T : View> ViewGroup.findViewsWithTagOfType(@StringRes tag: Int) = findViewsWithTagOfType<T>(context.getString(tag), this)

// It's public because of inline limitations
inline fun <reified T : View> findViewsWithTagOfType(tag: String, root: ViewGroup): List<T>
{
	val views = mutableListOf<View>()

	var currentParent: ViewGroup? = root
	val parentList = ArrayDeque<ViewGroup>()

	var childIndex = 0

	while (currentParent != null)
	{
		val child = currentParent.getChildAt(childIndex)

		if (child == null)
		{
			currentParent = parentList.removeFirstOrNull()
			childIndex = 0
			continue
		}
		if ((child !is T || child.tag != tag) && child !is ViewGroup)
		{
			childIndex++
			continue
		}

		when (child)
		{
			is ViewGroup -> parentList.add(child)
			else         -> views.add(child)
		}


		childIndex++
	}
	@Suppress("UNCHECKED_CAST")
	return views as List<T>
}