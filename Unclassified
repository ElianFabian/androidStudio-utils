import android.annotation.SuppressLint
import android.content.Context
import androidx.viewbinding.ViewBinding

@SuppressLint("DiscouragedApi")
inline fun <reified T : ViewBinding> Context.getLayoutId(): Int
{
	val layoutName = T::class.simpleName
		?.replace("Binding", "")
		?.pascalCaseToSnakeCase()
		?: error("Couldn't get simple name from ${T::class} class.")

	return resources.getIdentifier(layoutName, "layout", packageName)
}

fun String.pascalCaseToSnakeCase(): String
{
	val regex = Regex("([a-z])([A-Z]+)")
	val replacement = "$1_$2"
	return this.replace(regex, replacement)
		.lowercase()
}
