import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import java.util.*

class DatePickerFragment(private val listener: OnDateSelectedListener) : DialogFragment(),
    DatePickerDialog.OnDateSetListener
{
    interface OnDateSelectedListener
    {
        fun onDateSelected(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int)

        fun showDatePickerDialog(fragment: Fragment)
        {
            val datePickerFragment = DatePickerFragment(fragment as OnDateSelectedListener)

            datePickerFragment.show(fragment.parentFragmentManager, "datePicker")
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int)
    {
        listener.onDateSelected(view, year, month, dayOfMonth)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val calendar = Calendar.getInstance()

        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        // Creates a DatePickerDialog with an initial date (the current date in this case).
        val picker = DatePickerDialog(activity as Context, this, year, month, dayOfMonth)

//        // Set the available range in the Dialog.
//        picker.datePicker.minDate = calendar.timeInMillis
//        picker.datePicker.minDate = calendar.timeInMillis

        return picker
    }
}