@file:Suppress("NOTHING_TO_INLINE")

import android.content.Context
import androidx.annotation.BoolRes
import androidx.annotation.IntegerRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

sealed interface UiText

@JvmInline
private value class DynamicString(val value: String) : UiText

private data class StringResource(
	@StringRes
	val resId: Int,
	val args: List<UiTextArg?> = emptyList(),
) : UiText

private data class PluralsResource(
	@PluralsRes
	val resId: Int,
	val quantity: Int,
	val args: List<UiTextArg?> = emptyList(),
) : UiText


private val EmptyUiText: UiText = DynamicString("")

fun emptyUiText(): UiText = EmptyUiText

fun UiText(value: String?): UiText = when {
	value.isNullOrEmpty() -> emptyUiText()
	else                  -> DynamicString(value)
}

fun UiText(
	@StringRes resId: Int,
	args: List<UiTextArg?> = emptyList(),
): UiText = StringResource(
	resId = resId,
	args = args,
)

fun UiText(
	@PluralsRes resId: Int,
	quantity: Int,
	args: List<UiTextArg?> = emptyList(),
): UiText = PluralsResource(
	resId = resId,
	quantity = quantity,
	args = args,
)


fun UiText?.toString(context: Context?): String {
	if (context == null) return ""

	return when (this) {
		is DynamicString   -> value
		is StringResource  -> {
			val arguments = args.map { arg -> arg.getValue(context) }.toTypedArray()

			context.getString(resId, *arguments)
		}
		is PluralsResource -> {
			val arguments = args.map { arg -> arg.getValue(context) }.toTypedArray()

			context.resources.getQuantityString(resId, quantity, *arguments)
		}
		else               -> ""
	}
}

inline fun List<UiText>.concatToString(context: Context?, separator: CharSequence = "\n"): String {
	if (isEmpty() || context == null) return ""

	return joinToString(separator) { it.toString(context) }
}


sealed interface UiTextArg

@JvmInline
private value class ValueArg(val value: Any) : UiTextArg

@JvmInline
private value class BooleanResourceArg(@BoolRes val resId: Int) : UiTextArg

@JvmInline
private value class IntegerResourceArg(@IntegerRes val resId: Int) : UiTextArg

private data class StringResourceArg(
	@StringRes
	val resId: Int,
	val args: List<UiTextArg?> = emptyList(),
) : UiTextArg

private data class PluralsResourceArg(
	@PluralsRes
	val resId: Int,
	val quantity: Int,
	val args: List<UiTextArg?> = emptyList(),
) : UiTextArg


private inline fun valueAsArg(value: Any?): UiTextArg? = when (value) {
	null         -> null
	is UiTextArg -> value
	is String,
	is Boolean,
	is Char,
	is Byte,
	is Short,
	is Int,
	is Long,
	is Float,
	is Double    -> ValueArg(value)
	else         -> ValueArg(value.toString())
}

private fun UiTextArg?.getValue(context: Context): Any? {
	if (this == null) return null

	return when (this) {
		is ValueArg           -> value
		is BooleanResourceArg -> context.resources.getBoolean(resId)
		is IntegerResourceArg -> context.resources.getInteger(resId)
		is StringResourceArg  -> {
			context.getString(resId).format(
				*args.map { arg -> arg.getValue(context) }.toTypedArray()
			)
		}
		is PluralsResourceArg -> {
			context.resources.getQuantityString(
				resId,
				quantity,
				*args.map { arg -> arg.getValue(context) }.toTypedArray(),
			)
		}
	}
}

fun booleanResArg(@BoolRes resId: Int): UiTextArg = BooleanResourceArg(resId)

fun integerResArg(@IntegerRes resId: Int): UiTextArg = IntegerResourceArg(resId)

fun stringResArg(
	@StringRes
	resId: Int,
	args: List<UiTextArg?> = emptyList(),
): UiTextArg = StringResourceArg(resId, args)

fun pluralsResArg(
	@PluralsRes
	resId: Int,
	quantity: Int,
	args: List<UiTextArg?> = emptyList(),
): UiTextArg = PluralsResourceArg(resId, quantity, args)

fun uiArgsOf(vararg args: Any?): List<UiTextArg?> = args.map { arg -> valueAsArg(arg) }
