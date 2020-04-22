package net.csgstore.setupskip

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.UserManager
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import org.intellij.lang.annotations.PrintFormat

const val ALL_TAG = "SetupSkip"
const val PACKAGE_NAME = "net.csgstore.setupskip"

/**
 * @return  [String] The simple name of the class
 */
inline val <reified T : Any> T?.TAG: String
    get() = T::class.java.simpleName


//val KClass<*>.componentName : ComponentName
//    get() = java.componentName
//val Class<*>.componentName : ComponentName
//    get() = ComponentName((`package` as Package).name, name)
/**
 * Get the component name of a class
 * As of kotlin 1.3.50, to get the component name of a class without an instance reference, use the format:
 * (null as Class?).componentName
 *
 * @return [ComponentName] The component name for the class, with the package name supplied by
 * [BuildConfig.APPLICATION_ID]
 */
//inline val <reified T> T?.componentName: ComponentName
//    get() {
//        if (T::class.isCompanion)
//            return ComponentName((T::class.java.`package` as Package).name, (T::class.java.enclosingClass as Class).name)
//        return ComponentName((T::class.java.`package` as Package).name, T::class.java.name)
//    }
//    get() = ComponentName(Class::class.java::getPackage.name, Class::class.java.javaClass.name)

private fun <T : Any, R : Any> Activity.dialogBuilder(
    title: T,
    message: R,
    cancelable: Boolean = false,
    onPositiveButton: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
    onNegativeButton: ((dialog: DialogInterface, which: Int) -> Unit)? = null
): AlertDialog.Builder {
    setFinishOnTouchOutside(true)
    val builder = AlertDialog.Builder(this).setCancelable(cancelable)
    onPositiveButton?.let { builder.setPositiveButton(android.R.string.ok) { dialog, which -> it(dialog, which) } }
    onNegativeButton?.let { builder.setNegativeButton(android.R.string.cancel) { dialog, which -> it(dialog, which) } }
    builder.setOnDismissListener { dialog -> finish() }
    (message as? CharSequence)?.let { builder.setMessage(message) }
    (message as? View)?.let { builder.setView(message) }
    (title as? @StringRes Int)?.let { builder.setTitle(it) }
    (title as? CharSequence)?.let { builder.setTitle(it) }
    return builder
}

fun Activity.createDialog(
    title: CharSequence,
    message: CharSequence,
    cancelable: Boolean = false,
    onPositiveButton: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
    onNegativeButton: ((dialog: DialogInterface, which: Int) -> Unit)? = null
): AlertDialog.Builder {
    return this.dialogBuilder(title, message, cancelable, onPositiveButton, onNegativeButton)
}

fun Activity.createDialog(
    @StringRes title: Int, message: CharSequence,
    cancelable: Boolean = false,
    onPositiveButton: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
    onNegativeButton: ((dialog: DialogInterface, which: Int) -> Unit)? = null
): AlertDialog.Builder {
    return this.dialogBuilder(title, message, cancelable, onPositiveButton, onNegativeButton)
}

fun Activity.createDialog(
    title: CharSequence,
    view: View,
    cancelable: Boolean = false,
    onPositiveButton: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
    onNegativeButton: ((dialog: DialogInterface, which: Int) -> Unit)? = null
): AlertDialog.Builder {
    return this.dialogBuilder(title, view, cancelable, onPositiveButton, onNegativeButton)
}

fun Activity.createDialog(
    @StringRes title: Int, view: View,
    cancelable: Boolean = false,
    onPositiveButton: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
    onNegativeButton: ((dialog: DialogInterface, which: Int) -> Unit)? = null
): AlertDialog.Builder {
    return this.dialogBuilder(title, view, cancelable, onPositiveButton, onNegativeButton)
}

/** Returns the DevicePolicyManager instance. **/
val Context.devicePolicyManager: DevicePolicyManager
    get() = applicationContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
/** Returns the DevicePolicyManager instance. **/
val Context.activityManager: ActivityManager
    get() = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

/** Returns the UserManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1) val Context.userManager: UserManager
    get() = applicationContext.getSystemService(Context.USER_SERVICE) as UserManager

fun <T> Context.showToast(text: T, @Duration duration: Int = Toast.LENGTH_SHORT) where T : CharSequence {
    if (this is Activity && this.isFinishing) return
    Handler(Looper.getMainLooper()).post { Toast.makeText(applicationContext, text, duration).show() }
}

fun <T> Context.showToast(text: T, @PrintFormat vararg formatArgs: Any) where T : CharSequence =
    showToast("$text".format(formatArgs), Toast.LENGTH_SHORT)

fun Context.showToast(@StringRes msgId: Int, @PrintFormat vararg formatArgs: Any) =
    showToast(getString(msgId, formatArgs), Toast.LENGTH_SHORT)
