import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DimenRes
import androidx.annotation.Dimension
import androidx.annotation.Px
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

open class RecyclerView2 @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr)
{
	private val spacingDecorator = EpoxyItemSpacingDecorator()


	init
	{
		if (attrs != null)
		{
			val a = context.obtainStyledAttributes(
				attrs, R.styleable.RecyclerView2,
				defStyleAttr, 0
			)
			setItemSpacingPx(
				a.getDimensionPixelSize(
					R.styleable.RecyclerView2_itemSpacing,
					0
				)
			)
			a.recycle()
		}

		init()
	}


	private fun init()
	{
		clipToPadding = false
	}


	override fun setLayoutParams(params: ViewGroup.LayoutParams)
	{
		val isFirstParams = layoutParams == null
		super.setLayoutParams(params)

		if (isFirstParams)
		{
			// Set a default layout manager if one was not set via xml
			// We need layout params for this to guess at the right size and type
			if (layoutManager == null)
			{
				layoutManager = createLayoutManager()
			}
		}
	}

	/**
	 * Create a new [androidx.recyclerview.widget.RecyclerView.LayoutManager]
	 * instance to use for this RecyclerView.
	 *
	 * By default a LinearLayoutManager is used, and a reasonable default is chosen for scrolling
	 * direction based on layout params.
	 *
	 * If the RecyclerView is set to match parent size then the scrolling orientation is set to
	 * vertical and [.setHasFixedSize] is set to true.
	 *
	 * If the height is set to wrap_content then the scrolling orientation is set to horizontal, and
	 * [.setClipToPadding] is set to false.
	 */
	protected open fun createLayoutManager(): RecyclerView.LayoutManager
	{
		val layoutParams = layoutParams

		// 0 represents matching constraints in a LinearLayout or ConstraintLayout
		if (layoutParams.height == RecyclerView.LayoutParams.MATCH_PARENT || layoutParams.height == 0)
		{

			if (layoutParams.width == RecyclerView.LayoutParams.MATCH_PARENT || layoutParams.width == 0)
			{
				// If we are filling as much space as possible then we usually are fixed size
				setHasFixedSize(true)
			}

			// A sane default is a vertically scrolling linear layout
			return LinearLayoutManager(context)
		}
		else
		{
			// This is usually the case for horizontally scrolling carousels and should be a sane
			// default
			return LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
		}
	}


	fun setItemSpacingRes(@DimenRes itemSpacingRes: Int)
	{
		setItemSpacingPx(resToPx(itemSpacingRes))
	}

	fun setItemSpacingDp(@Dimension(unit = Dimension.DP) dp: Int)
	{
		setItemSpacingPx(dpToPx(dp))
	}

	/**
	 * Set a pixel value to use as spacing between items. If this is a positive number an item
	 * decoration will be added to space all items this far apart from each other. If the value is 0
	 * or negative no extra spacing will be used, and any previous spacing will be removed.
	 *
	 * This only works if a [LinearLayoutManager] or [GridLayoutManager] is used with this
	 * RecyclerView.
	 *
	 * This can also be set via the `app:itemSpacing` styleable attribute.
	 *
	 * @see .setItemSpacingDp
	 * @see .setItemSpacingRes
	 */
	open fun setItemSpacingPx(@Px spacingPx: Int)
	{
		removeItemDecoration(spacingDecorator)
		spacingDecorator.pxBetweenItems = spacingPx

		addItemDecoration(spacingDecorator)
	}


	@Px
	protected fun dpToPx(@Dimension(unit = Dimension.DP) dp: Int): Int
	{
		return TypedValue
			.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
				resources.displayMetrics
			).toInt()
	}

	@Px
	protected fun resToPx(@DimenRes itemSpacingRes: Int): Int
	{
		return resources.getDimensionPixelOffset(itemSpacingRes)
	}
}


/**
 * Modifies item spacing in a recycler view so that items are equally spaced no matter where they
 * are on the grid. Only designed to work with standard linear or grid layout managers.
 */
class EpoxyItemSpacingDecorator @JvmOverloads constructor(
	@Px var pxBetweenItems: Int = 0
) : ItemDecoration()
{
	private var verticallyScrolling = false
	private var horizontallyScrolling = false
	private var firstItem = false
	private var lastItem = false
	private var grid = false
	private var isFirstItemInRow = false
	private var fillsLastSpan = false
	private var isInFirstRow = false
	private var isInLastRow = false


	override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State)
	{
		// Zero everything out for the common case
		outRect.setEmpty()
		val position = parent.getChildAdapterPosition(view)
		if (position == RecyclerView.NO_POSITION)
		{
			// View is not shown
			return
		}
		val layout = parent.layoutManager
		calculatePositionDetails(parent, position, layout)
		var left = useLeftPadding()
		var right = useRightPadding()
		var top = useTopPadding()
		var bottom = useBottomPadding()
		if (shouldReverseLayout(layout, horizontallyScrolling))
		{
			if (horizontallyScrolling)
			{
				val temp = left
				left = right
				right = temp
			}
			else
			{
				val temp = top
				top = bottom
				bottom = temp
			}
		}

		// Divided by two because it is applied to the left side of one item and the right of another
		// to add up to the total desired space
		val padding = pxBetweenItems / 2
		outRect.right = if (right) padding else 0
		outRect.left = if (left) padding else 0
		outRect.top = if (top) padding else 0
		outRect.bottom = if (bottom) padding else 0
	}

	private fun calculatePositionDetails(parent: RecyclerView, position: Int, layout: RecyclerView.LayoutManager?)
	{
		val itemCount = parent.adapter!!.itemCount
		firstItem = position == 0
		lastItem = position == itemCount - 1
		horizontallyScrolling = layout!!.canScrollHorizontally()
		verticallyScrolling = layout.canScrollVertically()
		grid = layout is GridLayoutManager
		if (grid)
		{
			val grid = layout as GridLayoutManager?
			val spanSizeLookup = grid!!.spanSizeLookup
			val spanSize = spanSizeLookup.getSpanSize(position)
			val spanCount = grid.spanCount
			val spanIndex = spanSizeLookup.getSpanIndex(position, spanCount)
			isFirstItemInRow = spanIndex == 0
			fillsLastSpan = spanIndex + spanSize == spanCount
			isInFirstRow = isInFirstRow(position, spanSizeLookup, spanCount)
			isInLastRow = !isInFirstRow && isInLastRow(position, itemCount, spanSizeLookup, spanCount)
		}
	}

	private fun useBottomPadding(): Boolean
	{
		return if (grid)
		{
			(horizontallyScrolling && !fillsLastSpan
				|| verticallyScrolling && !isInLastRow)
		}
		else verticallyScrolling && !lastItem
	}

	private fun useTopPadding(): Boolean
	{
		return if (grid)
		{
			(horizontallyScrolling && !isFirstItemInRow
				|| verticallyScrolling && !isInFirstRow)
		}
		else verticallyScrolling && !firstItem
	}

	private fun useRightPadding(): Boolean
	{
		return if (grid)
		{
			(horizontallyScrolling && !isInLastRow
				|| verticallyScrolling && !fillsLastSpan)
		}
		else horizontallyScrolling && !lastItem
	}

	private fun useLeftPadding(): Boolean
	{
		return if (grid)
		{
			(horizontallyScrolling && !isInFirstRow
				|| verticallyScrolling && !isFirstItemInRow)
		}
		else horizontallyScrolling && !firstItem
	}

	companion object
	{
		private fun shouldReverseLayout(layout: RecyclerView.LayoutManager?, horizontallyScrolling: Boolean): Boolean
		{
			var reverseLayout = layout is LinearLayoutManager && layout.reverseLayout
			val rtl = layout!!.layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL
			if (horizontallyScrolling && rtl)
			{
				// This is how linearlayout checks if it should reverse layout in #resolveShouldLayoutReverse
				reverseLayout = !reverseLayout
			}
			return reverseLayout
		}

		private fun isInFirstRow(position: Int, spanSizeLookup: SpanSizeLookup, spanCount: Int): Boolean
		{
			var totalSpan = 0
			for (i in 0..position)
			{
				totalSpan += spanSizeLookup.getSpanSize(i)
				if (totalSpan > spanCount)
				{
					return false
				}
			}
			return true
		}

		private fun isInLastRow(
			position: Int, itemCount: Int, spanSizeLookup: SpanSizeLookup,
			spanCount: Int
		): Boolean
		{
			var totalSpan = 0
			for (i in itemCount - 1 downTo position)
			{
				totalSpan += spanSizeLookup.getSpanSize(i)
				if (totalSpan > spanCount)
				{
					return false
				}
			}
			return true
		}
	}
}
