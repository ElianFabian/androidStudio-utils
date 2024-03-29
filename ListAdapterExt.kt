import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.ListAdapter
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun <T : Any> ListAdapter<T, *>.awaitSubmitListCompletion(
    predicate: (previousList: List<T>, currentList: List<T>) -> Boolean = { _, _ -> true },
) {
    @Suppress("UNCHECKED_CAST")
    val differ = javaClass.superclass.getDeclaredField("mDiffer")
        .apply { isAccessible = true }
        .get(this) as AsyncListDiffer<T>

    suspendCancellableCoroutine<Unit> { continuation ->
        val listener = object : AsyncListDiffer.ListListener<T> {
            override fun onCurrentListChanged(previousList: List<T>, currentList: List<T>) {
                if (predicate(previousList, currentList)) {
                    differ.removeListListener(this)
                    continuation.resume(Unit)
                }
            }
        }

        differ.addListListener(listener)

        continuation.invokeOnCancellation {
            differ.removeListListener(listener)
        }
    }
}
