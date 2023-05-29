import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.view.descendants
import androidx.core.view.isGone
import com.padelmanager.padelmanager.R

class HintArrayAdapter<T>(
    context: Context,
    @LayoutRes
    private val layoutRes: Int = R.layout.item_spinner_city_white,
    private val hint: String,
    private inline val getTextValue: (item: T) -> String = { "$it" },
) : ArrayAdapter<SpinnerSelection<T?>>(
    context,
    layoutRes,
    mutableListOf<SpinnerSelection<T?>>(hintItem),
) {
    private companion object {
        val hintItem = SpinnerSelection(null)
    }

    private val hiddenView by lazy { View(context).apply { isGone = true } }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater = LayoutInflater.from(context)

        val view = when (convertView) {
            null, hiddenView -> inflater.inflate(layoutRes, parent, false)
            else -> convertView
        }

        val item = getItem(position)!!

        val textView: TextView = when (view) {
            is TextView -> view
            is ViewGroup -> {
                view.descendants.filterIsInstance<TextView>().firstOrNull() ?: error("Couldn't find any TextView at position $position")
            }
            else -> view as TextView
        }

        textView.text = item.value?.let { getTextValue(it) } ?: hint

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        if (position == count - 1) return hiddenView

        return getView(position, convertView, parent)
    }

    override fun getCount() = super.getCount() - 1


    fun addItem(item: T) {
        super.remove(hintItem)
        super.add(SpinnerSelection(item))
        super.add(hintItem)
    }

    fun addAllItems(items: Collection<T>) {
        super.remove(hintItem)
        super.addAll(items.map { SpinnerSelection(it) })
        super.add(hintItem)
    }

    fun addAllItems(vararg items: T) {
        super.remove(hintItem)
        super.addAll(*items.map { SpinnerSelection(it) }.toTypedArray())
        super.add(hintItem)
    }

    fun insertItem(position: Int, item: T) {
        super.remove(hintItem)
        super.insert(SpinnerSelection(item), position)
        super.add(hintItem)
    }

    fun removeItem(item: T) {
        super.remove(SpinnerSelection(item))
    }

    override fun clear() {
        super.clear()
        super.add(hintItem)
    }

    fun setList(items: List<T>) {
        super.clear()
        addAllItems(items)
        super.add(hintItem)
    }


    @Deprecated("Use addItem instead.", ReplaceWith("addItem(item)"))
    override fun add(`object`: SpinnerSelection<T?>?) = super.add(`object`)

    @Deprecated("Use addAllItems instead.", ReplaceWith("addAllItems(items)"))
    override fun addAll(collection: MutableCollection<out SpinnerSelection<T?>>) = super.addAll(collection)

    @Deprecated("Use addItem instead.", ReplaceWith("addItem(item)"))
    override fun addAll(vararg items: SpinnerSelection<T?>?) = super.addAll(*items)

    @Deprecated("Use insertItem instead.", ReplaceWith("insertItem(item, 0)"))
    override fun insert(`object`: SpinnerSelection<T?>?, index: Int) = super.insert(`object`, index)

    @Deprecated("Use removeItem instead.", ReplaceWith("removeItem(item)"))
    override fun remove(`object`: SpinnerSelection<T?>?) = super.remove(`object`)
}

fun <T> Spinner.setHintAdapter(hintAdapter: HintArrayAdapter<T>) {
    adapter = hintAdapter
    clearSelectionForHintAdapter()
}

fun Spinner.clearSelectionForHintAdapter() {
    if (adapter != null && adapter !is HintArrayAdapter<*>) error("Adapter must be of type ${HintArrayAdapter::class.qualifiedName}")
    setSelection(adapter.count)
}

fun Spinner.setSelectionForHintAdapter(position: Int?) {
    if (position == null || position == -1) clearSelectionForHintAdapter()
    else setSelection(position)
}

inline fun <T> Spinner.setOnItemSelectedListenerForHintAdapter(
    crossinline onNothingSelected: () -> Unit = { },
    crossinline onItemSelected: (item: T, position: Int) -> Unit = { _, _ -> },
) {
    if (adapter != null && adapter !is HintArrayAdapter<*>) error("Adapter must be of type ${HintArrayAdapter::class.qualifiedName}")

    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            @Suppress("UNCHECKED_CAST")
            val item = parent.getItemAtPosition(position) as SpinnerSelection<T>

            if (item.value == null) return

            onItemSelected(item.value, position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            onNothingSelected()
        }
    }
}


class SpinnerSelection<out T>(val value: T?) {
    override fun toString(): String = value.toString()

    override fun equals(other: Any?) = other == value

    override fun hashCode() = value.hashCode()
}
