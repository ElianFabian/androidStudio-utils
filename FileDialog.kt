import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment

class FileDialog(fragment: Fragment)
{
    private lateinit var listener: (Uri) -> Unit

    private var resultLauncher = fragment.registerForActivityResult(StartActivityForResult())
    {
        if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult

        it.data?.data.let { uri -> listener(uri!!) }
    }

    fun open(accept: String = "*/*", listener: (Uri) -> Unit)
    {
        this.listener = listener

        Intent().apply()
        {
            type = accept
            action = Intent.ACTION_GET_CONTENT
            resultLauncher.launch(Intent.createChooser(this, ""))
        }
    }
}