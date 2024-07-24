package pan.project.fastrtsplive

import android.app.Activity
import android.widget.Toast
import androidx.fragment.app.Fragment


fun Activity.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
  Toast.makeText(this, message, duration).show()
}
fun Fragment.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
  Toast.makeText(context, message, duration).show()
}