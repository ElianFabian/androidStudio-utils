import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.appcompat.widget.LinearLayoutCompat
import com.padelmanager.padelmanager.R

/**
 * A RadioGroup that doesn't need RadioButtons to be direct children in order to apply the logic.
 */
class RecursiveRadioGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayout(context, attrs, defStyle) {

    var checkedButtonId: Int = -1
        private set
    private val buttons = mutableListOf<RadioButton>()


    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LinearLayoutCompat, 0, 0)
        val orientation = a.getInt(R.styleable.LinearLayoutCompat_android_orientation, LinearLayoutCompat.VERTICAL)
        this.orientation = orientation
    }


    override fun onFinishInflate() {
        super.onFinishInflate()

        findRadioButtons(this)
        setOnCheckedChangeListener()
    }


    fun check(id: Int) {
        buttons.forEach { button ->
            button.isChecked = button.id == id
        }
        checkedButtonId = id
    }


    private fun findRadioButtons(view: View) {
        if (view is RadioButton) {
            buttons.add(view)
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                findRadioButtons(view.getChildAt(i))
            }
        }
    }

    private fun setOnCheckedChangeListener() {
        buttons.forEach { button ->
            button.setOnCheckedChangeListener { radioButton, isChecked ->
                if (isChecked) {
                    checkedButtonId = radioButton.id

                    buttons.forEach { otherButton ->
                        if (otherButton.id != radioButton.id) {
                            otherButton.isChecked = false
                        }
                    }
                }
            }
        }
    }
}
