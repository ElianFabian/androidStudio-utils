import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity


fun FragmentActivity.goToFragment(fragment: Fragment, args: Bundle? = null): Fragment
{
    fragment.arguments = args

    supportFragmentManager.beginTransaction().apply()
    {
        replace(R.id.nav_host_fragment, fragment)
        commit()
    }
	
	return fragment
}
