

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DimenRes
import androidx.annotation.Dimension
import androidx.annotation.Px
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.padelmanager.padelmanager.R


/**
 * Base code is from: https://github.com/airbnb/epoxy/blob/master/epoxy-adapter/src/main/java/com/airbnb/epoxy/EpoxyRecyclerView.kt
 */
class ExtendedRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RecyclerView(context, attrs, defStyleAttr) {

    private val spacingDecorator = SpacingItemDecoration(Spacing(0, 0))


    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(
                attrs, R.styleable.ExtendedRecyclerView,
                defStyleAttr, 0
            )

            val itemSpacing = a.getDimensionPixelSize(
                R.styleable.ExtendedRecyclerView_itemSpacing,
                0
            )
            val itemHorizontalSpacing = a.getDimensionPixelSize(
                R.styleable.ExtendedRecyclerView_horizontalItemSpacing,
                0
            )
            val itemVerticalSpacing = a.getDimensionPixelSize(
                R.styleable.ExtendedRecyclerView_verticalItemSpacing,
                0
            )

            if (itemSpacing != 0) {
                setItemSpacingPx(itemSpacing)
            } else {
                setItemHorizontalSpacingPx(itemHorizontalSpacing)
                setItemVerticalSpacingPx(itemVerticalSpacing)
            }

            a.recycle()
        }

        init()
    }


    private fun init() {
        clipToPadding = false
    }


    override fun setLayoutParams(params: ViewGroup.LayoutParams) {
        val isFirstParams = layoutParams == null
        super.setLayoutParams(params)

        if (isFirstParams) {
            // Set a default layout manager if one was not set via xml
            // We need layout params for this to guess at the right size and type
            if (layoutManager == null) {
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
    protected fun createLayoutManager(): RecyclerView.LayoutManager {
        val layoutParams = layoutParams

        // 0 represents matching constraints in a LinearLayout or ConstraintLayout
        if (layoutParams.height == RecyclerView.LayoutParams.MATCH_PARENT || layoutParams.height == 0) {

            if (layoutParams.width == RecyclerView.LayoutParams.MATCH_PARENT || layoutParams.width == 0) {
                // If we are filling as much space as possible then we usually are fixed size
                setHasFixedSize(true)
            }

            // A sane default is a vertically scrolling linear layout
            return LinearLayoutManager(context)
        } else {
            // This is usually the case for horizontally scrolling carousels and should be a sane
            // default
            return LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
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
    fun setItemSpacingPx(@Px spacingPx: Int) {
        removeItemDecoration(spacingDecorator)
        spacingDecorator.spacing.horizontal = spacingPx
        spacingDecorator.spacing.vertical = spacingPx
        addItemDecoration(spacingDecorator)
    }

    fun setItemHorizontalSpacingPx(@Px spacingPx: Int) {
        removeItemDecoration(spacingDecorator)
        spacingDecorator.spacing.horizontal = spacingPx
        addItemDecoration(spacingDecorator)
    }

    fun setItemVerticalSpacingPx(@Px spacingPx: Int) {
        removeItemDecoration(spacingDecorator)
        spacingDecorator.spacing.vertical = spacingPx
        addItemDecoration(spacingDecorator)
    }

    fun setItemSpacingDp(@Dimension(unit = Dimension.DP) dp: Int) {
        setItemSpacingPx(dpToPx(dp))
    }

    fun setItemHorizontalSpacingDp(@Dimension(unit = Dimension.DP) dp: Int) {
        setItemHorizontalSpacingPx(dpToPx(dp))
    }

    fun setItemVerticalSpacingDp(@Dimension(unit = Dimension.DP) dp: Int) {
        setItemVerticalSpacingPx(dpToPx(dp))
    }

    fun setItemSpacingRes(@DimenRes itemSpacingRes: Int) {
        setItemSpacingPx(resToPx(itemSpacingRes))
    }

    fun setItemHorizontalSpacingRes(@DimenRes itemSpacingRes: Int) {
        setItemHorizontalSpacingPx(resToPx(itemSpacingRes))
    }

    fun setItemVerticalSpacingRes(@DimenRes itemSpacingRes: Int) {
        setItemVerticalSpacingPx(resToPx(itemSpacingRes))
    }

    @Px
    protected fun dpToPx(@Dimension(unit = Dimension.DP) dp: Int): Int {
        return TypedValue
            .applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
                resources.displayMetrics
            ).toInt()
    }

    @Px
    protected fun resToPx(@DimenRes itemSpacingRes: Int): Int {
        return resources.getDimensionPixelOffset(itemSpacingRes)
    }
}


/**
 * [ItemDecoration] implementation that adds specified spacing to [RecyclerView]s items elements.
 * For flexibility, there are several offsets to define. Each of them can be used separately, or
 * together, depending on desired effect.
 * 
 * Source: https://github.com/grzegorzojdana/SpacingItemDecoration/tree/master
 */
private class SpacingItemDecoration(
    /**
     * Desired offsets of RecyclerView items. See [Spacing].
     */
    spacing: Spacing,
) : ItemDecoration() {

    var itemOffsetsCalculator = ItemOffsetsCalculator(spacing)
    var itemOffsetsRequestBuilder = ItemOffsetsRequestBuilder()

    private val itemOffsetsParams = ItemOffsetsRequestBuilder.ItemOffsetsParams()
    private val offsetsRequest = ItemOffsetsCalculator.OffsetsRequest()

    private var cachedGroupCount: Int = -1

    private val drawing: Drawing by lazy(LazyThreadSafetyMode.NONE) { Drawing() }


    /**
     * Desired offsets of RecyclerView items. See [Spacing].
     * Returned object can be modified, but [invalidateSpacing] need to be called to apply changes.
     */
    var spacing: Spacing
        get() = itemOffsetsCalculator.spacing
        set(value) {
            itemOffsetsCalculator.spacing = value
        }

    /**
     * If you use `GridLayoutManager` with non-default `SpanSizeLookup` but its
     * `getSpanSize(position)` method always returns `1`, you can set this property to `true`
     * to improve performance.
     */
    var hintSpanSizeAlwaysOne: Boolean = false

    /**
     * Enable to improve performance for GridLayoutManager with relatively big number of list items
     * (thousands). This will make once determined items group count held to use for items offsets
     * calculations. If items count or layout manager specification (orientation, span size lookup,
     * spans) are changed, group count will also change, so if you set [cachedGroupCount] to `true`,
     * remember to call [invalidate] in that case.
     */
    var isGroupCountCacheEnabled: Boolean = false

    /**
     * Set to enable drawing applied spacing by this decoration. Useful for debugging.
     * @see [drawingConfig]
     */
    var isSpacingDrawingEnabled: Boolean = false

    /**
     * Colors used to mark spacing if [isSpacingDrawingEnabled] is set to `true`.
     */
    var drawingConfig: DrawingConfig = DrawingConfig()


    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        if (parent.getChildAdapterPosition(view) < 0) {
            outRect.setEmpty()
            return
        }

        determineItemOffsetsParams(view, parent, state, itemOffsetsParams)
        itemOffsetsRequestBuilder.fillItemOffsetsRequest(itemOffsetsParams, offsetsRequest)
        itemOffsetsCalculator.getItemOffsets(outRect, offsetsRequest)
    }

    private fun determineItemOffsetsParams(
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
        itemOffsetsParams: ItemOffsetsRequestBuilder.ItemOffsetsParams,
    ) {
        val layoutManager = parent.layoutManager
        val itemPosition = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        when (layoutManager) {
            null -> throw IllegalArgumentException("RecyclerView without layout manager")
            is GridLayoutManager -> {
                val layoutParams = view.layoutParams as GridLayoutManager.LayoutParams
                val clampedSpanCount = Math.max(layoutManager.spanCount, 1)
                val spanGroupIndex = layoutManager.spanSizeLookup.getSpanGroupIndex(
                    itemPosition, clampedSpanCount
                )

                itemOffsetsParams.apply {
                    spanIndex = layoutParams.spanIndex
                    groupIndex = spanGroupIndex
                    spanSize = layoutParams.spanSize
                    spanCount = clampedSpanCount
                    groupCount = getGridGroupCount(itemCount, layoutManager)
                    isLayoutVertical = (layoutManager.orientation == OrientationHelper.VERTICAL)
                    isLayoutReverse = layoutManager.reverseLayout
                }
            }
            is StaggeredGridLayoutManager -> {
                val layoutParams = view.layoutParams as StaggeredGridLayoutManager.LayoutParams

                // could write some logic to determine and cache group index for each item
                // could access to Span object in item layout params through reflection

                itemOffsetsParams.apply {
                    spanIndex = layoutParams.spanIndex
                    groupIndex = 0
                    spanSize = if (layoutParams.isFullSpan) layoutManager.spanCount else 1
                    spanCount = layoutManager.spanCount
                    groupCount = 1
                    isLayoutVertical = (layoutManager.orientation == OrientationHelper.VERTICAL)
                    isLayoutReverse = layoutManager.reverseLayout
                }
            }
            is LinearLayoutManager -> {
                itemOffsetsParams.apply {
                    spanIndex = 0
                    groupIndex = itemPosition
                    spanSize = 1
                    spanCount = 1
                    groupCount = itemCount
                    isLayoutVertical = (layoutManager.orientation == OrientationHelper.VERTICAL)
                    isLayoutReverse = layoutManager.reverseLayout
                }
            }
            else -> throw IllegalArgumentException(
                "Unsupported layout manager: ${layoutManager::class.java.simpleName}"
            )
        }
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (!isSpacingDrawingEnabled) return
        drawItemDependentSpacing(canvas, parent)
        drawEdges(canvas, parent, state)
    }

    private fun drawItemDependentSpacing(canvas: Canvas, parent: RecyclerView) {
        with(drawing) {
            parent.getChildrenSequence().forEach { itemView ->
                // drawing horizontal and vertical spacing in one loop with item spacing
                // for better performance

                // item spacing
                // drawing as one rect under the item

                drawingRect.set(
                    itemView.left - spacing.item.left,
                    itemView.top - spacing.item.top,
                    itemView.right + spacing.item.right,
                    itemView.bottom + spacing.item.bottom
                )
                drawRect(canvas, drawingConfig.itemColor)

                // horizontal spacing after item
                // no checking if item is most right item, we will draw edges spacing anyway

                drawingRect.left = itemView.right + spacing.item.right
                drawingRect.right = drawingRect.left + spacing.horizontal
                drawRect(canvas, drawingConfig.horizontalColor)

                // vertical spacing after item

                drawingRect.set(
                    itemView.left - spacing.item.left,
                    itemView.bottom + spacing.item.bottom,
                    itemView.right + spacing.item.right,
                    itemView.bottom + spacing.item.bottom + spacing.vertical
                )
                drawRect(canvas, drawingConfig.verticalColor)
            }
        }
    }

    private fun drawEdges(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.childCount == 0) return
        if (spacing.edges.isAllZeros()) return

        val extremeItems = parent.getExtremeChildren()
        var internalEdge: Int

        with(drawing) {
            paint.color = drawingConfig.edgeColor
            parent.getDrawingRect(visibleRect)

            determineItemOffsetsParams(extremeItems[0], parent, state, itemOffsetsParams)
            itemOffsetsRequestBuilder.fillItemOffsetsRequest(itemOffsetsParams, offsetsRequest)
            if (offsetsRequest.col == 0) {
                internalEdge = Math.min(
                    extremeItems[0].left - spacing.item.left,
                    spacing.edges.left
                )
                drawingRect.set(0, 0, internalEdge, visibleRect.bottom)
                drawRect(canvas)
            }

            determineItemOffsetsParams(extremeItems[1], parent, state, itemOffsetsParams)
            itemOffsetsRequestBuilder.fillItemOffsetsRequest(itemOffsetsParams, offsetsRequest)
            if (offsetsRequest.row == 0) {
                internalEdge = Math.min(
                    extremeItems[1].top - spacing.item.top,
                    spacing.edges.top
                )
                drawingRect.set(0, 0, visibleRect.right, internalEdge)
                drawRect(canvas)
            }

            determineItemOffsetsParams(extremeItems[2], parent, state, itemOffsetsParams)
            itemOffsetsRequestBuilder.fillItemOffsetsRequest(itemOffsetsParams, offsetsRequest)
            if (offsetsRequest.lastCol == offsetsRequest.cols - 1) {
                internalEdge = Math.max(
                    extremeItems[2].right + spacing.item.right,
                    visibleRect.right - spacing.edges.right
                )
                drawingRect.set(internalEdge, 0, visibleRect.right, visibleRect.bottom)
                drawRect(canvas)
            }

            determineItemOffsetsParams(extremeItems[3], parent, state, itemOffsetsParams)
            itemOffsetsRequestBuilder.fillItemOffsetsRequest(itemOffsetsParams, offsetsRequest)
            if (offsetsRequest.lastRow == offsetsRequest.rows - 1) {
                internalEdge = Math.max(
                    extremeItems[3].bottom + spacing.item.bottom,
                    visibleRect.bottom - spacing.edges.bottom
                )
                drawingRect.set(0, internalEdge, visibleRect.right, visibleRect.bottom)
                drawRect(canvas)
            }
        }
    }

    /**
     * Invalidate caches [spacing]. Should be called if [spacing] properties was modified without
     * setting [spacing] field.
     */
    fun invalidateSpacing() {
        itemOffsetsCalculator.invalidatePrecalculatedValues()
    }

    /**
     * Invalidate cached values.
     */
    fun invalidate() {
        invalidateSpacing()
        cachedGroupCount = -1
    }

    private fun getGridGroupCount(itemCount: Int, layoutManager: GridLayoutManager): Int {
        if (isGroupCountCacheEnabled && cachedGroupCount > 0)
            return cachedGroupCount

        val spanSizeLookup = layoutManager.spanSizeLookup
        val clampedSpanCount = Math.max(layoutManager.spanCount, 1)

        val groupCount: Int = when {
            hintSpanSizeAlwaysOne || spanSizeLookup is GridLayoutManager.DefaultSpanSizeLookup -> {
                Math.ceil(itemCount / clampedSpanCount.toDouble()).toInt()
            }
            else -> {
                val lastItemIndex = if (layoutManager.reverseLayout) 0 else itemCount - 1
                spanSizeLookup.getSpanGroupIndex(lastItemIndex, clampedSpanCount) + 1
            }
        }

        if (isGroupCountCacheEnabled)
            cachedGroupCount = groupCount

        return groupCount
    }


    data class DrawingConfig(
        var edgeColor: Int = Color.parseColor("#F44336"), // Red 500
        var itemColor: Int = Color.parseColor("#FFEB3B"), // Yellow 500
        var horizontalColor: Int = Color.parseColor("#00BCD4"), // Cyan 500
        var verticalColor: Int = Color.parseColor("#76FF03"), // Light Green A400
    )


    /**
     * Stuff used to draw spacings.
     */
    private class Drawing(
        val drawingRect: Rect = Rect(),
        val visibleRect: Rect = Rect(),
        val paint: Paint = Paint().apply {
            style = Paint.Style.FILL
        },
    ) {
        fun drawRect(canvas: Canvas, color: Int) {
            paint.color = color
            canvas.drawRect(drawingRect, paint)
        }

        fun drawRect(canvas: Canvas) = canvas.drawRect(drawingRect, paint)
    }

}


/**
 * Find most left, top, right and bottom children and return array with 4 items.
 * If list doesn't have children, empty array is returned.
 */
private fun RecyclerView.getExtremeChildren(): Array<View> {
    if (childCount == 0) return emptyArray()

    val firstChild = getChildAt(0)
    val extremeChildren = Array<View>(4, { firstChild })

    for (i in 1 until childCount) {
        val child = getChildAt(i)
        if (child.left < extremeChildren[0].left) extremeChildren[0] = child
        if (child.top < extremeChildren[1].top) extremeChildren[1] = child
        if (child.right > extremeChildren[2].right) extremeChildren[2] = child
        if (child.bottom > extremeChildren[3].bottom) extremeChildren[3] = child
    }

    return extremeChildren
}

/**
 * Check if all Rect edges are `0`.
 */
private fun Rect.isAllZeros(): Boolean {
    return (left == 0 && top == 0 && right == 0 && bottom == 0)
}

private fun RecyclerView.getChildrenSequence(): Sequence<View> {
    return (0 until childCount).asSequence().map { getChildAt(it) }
}


/**
 * Determine params for [ItemOffsetsCalculator] basing on given RecyclerView layout manager specifics.
 */
private open class ItemOffsetsRequestBuilder {

    /**
     * Data that describes position of item that offsets are desired, and some of its list layout
     * manager params.
     */
    data class ItemOffsetsParams(
        /* Item data */
        var spanIndex: Int = 0,
        var groupIndex: Int = 0,
        var spanSize: Int = 1,
        /* Layout manager data */
        var spanCount: Int = 1,
        var groupCount: Int = 1,
        var isLayoutVertical: Boolean = true,
        var isLayoutReverse: Boolean = false,
    )

    /**
     * Fill [offsetsRequest] basing on RecyclerView item and layout params passed by
     * [itemOffsetsParams]. All fields of [offsetsRequest] will be set.
     */
    open fun fillItemOffsetsRequest(
        itemOffsetsParams: ItemOffsetsParams,
        offsetsRequest: ItemOffsetsCalculator.OffsetsRequest,
    ) {
        val groupIndexAdjustedToReverse =
            if (!itemOffsetsParams.isLayoutReverse) itemOffsetsParams.groupIndex
            else itemOffsetsParams.groupCount - itemOffsetsParams.groupIndex - 1

        with(offsetsRequest) {
            if (itemOffsetsParams.isLayoutVertical) {
                row = groupIndexAdjustedToReverse
                col = itemOffsetsParams.spanIndex

                spanSizeH = itemOffsetsParams.spanSize
                spanSizeV = 1

                rows = itemOffsetsParams.groupCount
                cols = itemOffsetsParams.spanCount
            } else {
                row = itemOffsetsParams.spanIndex
                col = groupIndexAdjustedToReverse

                spanSizeH = 1
                spanSizeV = itemOffsetsParams.spanSize

                rows = itemOffsetsParams.spanCount
                cols = itemOffsetsParams.groupCount
            }
        }
    }
}

/**
 * Calculates RecyclerView item offsets depending on data prepared by [ItemOffsetsRequestBuilder].
 *
 * This class expects item position translated from RecyclerView LayoutManager coordinates to
 * grid table coordinates with first row on top and first column on left. Translation is done
 * by [ItemOffsetsRequestBuilder].
 */
private open class ItemOffsetsCalculator(
    spacing: Spacing,
) {

    /**
     * Input data for [ItemOffsetsCalculator].
     */
    data class OffsetsRequest(
        // indices of item in the table
        var row: Int = 0,
        var col: Int = 0,
        // size of item
        var spanSizeH: Int = 1,
        var spanSizeV: Int = 1,
        // total number of rows and cols in the table
        var rows: Int = 0,
        var cols: Int = 0,
    ) {
        val lastRow: Int get() = row + spanSizeV - 1
        val lastCol: Int get() = col + spanSizeH - 1
    }

    open var spacing: Spacing = spacing
        set(value) {
            field = value; invalidatePrecalculatedValues()
        }

    private var areCachedValuesInvalid = true

    // Data used to determine precalculated values; stored to compare inside getItemOffsets()
    // and determine if cached values are valid or not.
    private var cachedRows = 0
    private var cachedCols = 0

    // Precalculated (cached) values. Need to be calculated again when spacing, item count or
    // layout manager params change.

    private var startMargin: Int = 0
    private var topMargin: Int = 0
    private var itemDistanceH: Int = 0
    private var itemDistanceV: Int = 0
    private var itemDeltaH: Float = 0F
    private var itemDeltaV: Float = 0F


    /**
     * Calculate offsets for item specified by [OffsetsRequest] instance.
     * [offsetsRequest] `rows` and `cols` can't be `0`.
     */
    open fun getItemOffsets(outRect: Rect, offsetsRequest: OffsetsRequest) {
        updatePrecalculatedValuesValidityFor(offsetsRequest)
        if (areCachedValuesInvalid) {
            validatePrecalculatedValues(offsetsRequest)
        }

        outRect.apply {
            left = Math.round(startMargin + offsetsRequest.col * itemDeltaH)
            top = Math.round(topMargin + offsetsRequest.row * itemDeltaV)
            right = Math.round(itemDistanceH - left - offsetsRequest.spanSizeH * itemDeltaH)
            bottom = Math.round(itemDistanceV - top - offsetsRequest.spanSizeV * itemDeltaV)
        }
    }

    open fun invalidatePrecalculatedValues() {
        areCachedValuesInvalid = true
    }

    private fun updatePrecalculatedValuesValidityFor(offsetsRequest: OffsetsRequest) {
        if (offsetsRequest.rows != cachedRows || offsetsRequest.cols != cachedCols) {
            areCachedValuesInvalid = true
        }
    }

    private fun validatePrecalculatedValues(offsetsRequest: OffsetsRequest) {
        with(spacing) {
            startMargin = edges.left + item.left
            itemDistanceH = horizontal + item.left + item.right
            itemDeltaH = (horizontal - edges.left - edges.right) / offsetsRequest.cols.toFloat()

            topMargin = edges.top + item.top
            itemDistanceV = vertical + item.top + item.bottom
            itemDeltaV = (vertical - edges.top - edges.bottom) / offsetsRequest.rows.toFloat()
        }

        areCachedValuesInvalid = false

        cachedRows = offsetsRequest.rows
        cachedCols = offsetsRequest.cols
    }
}


/**
 * Set of offsets available to define for [SpacingItemDecoration].
 */
private data class Spacing(

    /**
     * Horizontal offset between every two items, in pixels.
     */
    var horizontal: Int = 0,

    /**
     * Vertical offset between every two items, in pixels.
     */
    var vertical: Int = 0,

    /**
     * Set of offsets that can be used to move items from parent edges toward parent's center.
     * Think about it like a list view padding.
     * Values in pixels are expected.
     */
    var edges: Rect = Rect(),

    /**
     * Offsets added to each of item edges, in pixels.
     */
    var item: Rect = Rect(),
) {

    /**
     * Set all spacing to `0`.
     */
    fun zero() {
        horizontal = 0
        vertical = 0
        edges.setEmpty()
        item.setEmpty()
    }

}
