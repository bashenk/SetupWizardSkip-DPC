package net.csgstore.setupskip

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * The type Accessibility background service.
 *
 * @author Brian Shenk
 */
class MyAccessibilityService : AccessibilityService() {
    private val appContext: Context by lazy { applicationContext }

    init {
        instance = this
    }
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(CLASS_NAME, "Service Connected")
//        disableTouchExploration(instance)

    }
    /**
     * Callback that allows an accessibility service to observe the key events
     * before they are passed to the rest of the system. This means that the events
     * are first delivered here before they are passed to the device policy, the
     * input method, or applications.
     *
     *
     * **Note:** It is important that key events are handled in such
     * a way that the event stream that would be passed to the rest of the system
     * is well-formed. For example, handling the down event but not the up event
     * and vice versa would generate an inconsistent event stream.
     *
     *
     *
     * **Note:** The key events delivered in this method are copies
     * and modifying them will have no effect on the events that will be passed
     * to the system. This method is intended to perform purely filtering
     * functionality.
     *
     *
     *
     * @param event The event to be processed. This event is owned by the caller and cannot be used
     * after this method returns. Services wishing to use the event after this method returns should
     * make a copy.
     * @return If true then the event will be consumed and not delivered to
     * applications, otherwise it will be delivered as usual.
     */
    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        return super.onKeyEvent(event)
    }

    private var nodeHierarchy: List<List<AccessibilityNodeInfo>>? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d(CLASS_NAME, "Accessibility Event Occurred")
    }

    override fun onInterrupt() {
        Log.d(CLASS_NAME, "Accessibility Interrupted")
    }

    override fun onDestroy() {
        Log.d(CLASS_NAME, "Service Destroyed")
        super.onDestroy()
    }

    companion object {
        private val CLASS_NAME = MyAccessibilityService::class.java.simpleName
        private val TAG = CLASS_NAME
        lateinit var instance: MyAccessibilityService

        fun disableTouchExploration(mService: MyAccessibilityService?) {
            val serviceInfo = mService!!.serviceInfo
            serviceInfo.flags =
                serviceInfo.flags and AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE.inv()
            mService.serviceInfo = serviceInfo
        }
    }
}