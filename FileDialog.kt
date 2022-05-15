import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment

class FileDialog(fragment: Fragment)
{
    fun interface OnFileSelectedListener
    {
        fun onFileSelected(uri: Uri)
    }

    private lateinit var onFileSelectedListener: OnFileSelectedListener

    private var resultLauncher = fragment.registerForActivityResult(StartActivityForResult())
    {
        if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult

        it.data?.data.let { uri -> onFileSelectedListener.onFileSelected(uri!!) }
    }

    fun open(accept: String = "*/*", listener: OnFileSelectedListener) = Intent().apply()
    {
        onFileSelectedListener = listener

        type = accept
        action = Intent.ACTION_GET_CONTENT
        resultLauncher.launch(Intent.createChooser(this, ""))
    }
}