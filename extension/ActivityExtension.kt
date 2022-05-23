import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity


fun Activity.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT)
{
    Toast.makeText(this, text, duration).show()
}

fun FragmentActivity.goToFragment(fragment: Fragment, args: Bundle? = null)
{
    fragment.arguments = args

    supportFragmentManager.beginTransaction().apply()
    {
        replace(R.id.nav_host_fragment, fragment)
        commit()
    }
}