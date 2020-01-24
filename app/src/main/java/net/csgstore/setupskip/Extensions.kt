package net.csgstore.setupskip

import android.content.ComponentName
import kotlin.reflect.KClass

const val TAG = "SetupSkip"
const val PACKAGE_NAME = "net.csgstore.setupskip"

/**
 * @return  [String] The simple name of the class
 */
inline val <reified T> T?.TAG: String get() = T::class.java.simpleName


val KClass<*>.componentName : ComponentName
    get() = java.componentName
val Class<*>.componentName : ComponentName
    get() = ComponentName((`package` as Package).name, name)
/**
 * Get the component name of a class
 * As of kotlin 1.3.50, to get the component name of a class without an instance reference, use the format:
 * (null as Class?).componentName
 *
 * @return [ComponentName] The component name for the class, with the package name supplied by
 * [BuildConfig.APPLICATION_ID]
 */
inline val <reified T> T?.componentName: ComponentName
    get() {
        if (T::class.isCompanion)
            return ComponentName((T::class.java.`package` as Package).name, (T::class.java.enclosingClass as Class).name)
        return T::class.java.componentName
    }
//    get() = ComponentName(Class::class.java::getPackage.name, Class::class.java.javaClass.name)