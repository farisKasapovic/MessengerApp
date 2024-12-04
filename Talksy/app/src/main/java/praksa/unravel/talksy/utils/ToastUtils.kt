package praksa.unravel.talksy.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import praksa.unravel.talksy.R

object ToastUtils {

    fun showCustomToast(context: Context, message: String) {
        // Inflate custom layout
        val inflater = LayoutInflater.from(context)
        val layout: View = inflater.inflate(R.layout.custom_toast, null)

        // Set text
        val textView: TextView = layout.findViewById(R.id.toast_text)
        textView.text = message

        // Create and show Toast
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }
}
