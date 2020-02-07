package net.csgstore.setupskip

import android.app.UiAutomation
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.*
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import java.util.concurrent.TimeUnit
import kotlin.test.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
//@RunWith(AndroidJUnit4ClassRunner::class)
//@RunWith(AndroidJUnit4::class)
@SmallTest
class InstrumentedTest {

//    private lateinit var instrument : Instrumentation
//
//    @Before
//    fun setup() {
//        instrument = Instrumentation()
//        InstrumentationRegistry.registerInstance(instrument, null)
//    }

    @Test
    fun useAppContext() { // Context of the app under test.
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        Assert.assertEquals(PACKAGE_NAME, appContext.packageName)
    }

    @Test
    @RequiresApi(Build.VERSION_CODES.N)
    fun enableAccessibilityService() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        var result = appContext.isAccessibilityServiceEnabled(MyAccessibilityService::class.java)
        if (result) {
            Assert.assertTrue(result)
        } else {
            val prevAccessibilityServices = Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).split(":").filterNot { s -> s.isEmpty() }
            val flatComponentName = ComponentName(appContext, MyAccessibilityService::class.java).flattenToString()
            val newAccessibilityServiceList = arrayOf(flatComponentName, *prevAccessibilityServices.toTypedArray());
            val newString = if (newAccessibilityServiceList.isNotEmpty()) newAccessibilityServiceList.joinToString(separator = ":") else null
            val accessibilityServices = "enabled_accessibility_services"
            val cmd = "settings put secure $accessibilityServices $newString"
            InstrumentationRegistry.getInstrumentation().getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)
                .executeShellCommand(cmd).close()
            TimeUnit.SECONDS.sleep(3)
            result = appContext.isAccessibilityServiceEnabled(MyAccessibilityService::class.java)
            Assert.assertTrue(result)
        }
    }
}