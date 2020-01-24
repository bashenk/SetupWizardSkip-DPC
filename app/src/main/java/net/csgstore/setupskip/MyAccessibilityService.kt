package net.csgstore.setupskip

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import net.csgstore.setupskip.AccessibilityUtils.FRAME_LAYOUT_CLASS
import net.csgstore.setupskip.AccessibilityUtils.debugValues
import net.csgstore.setupskip.AccessibilityUtils.enableTouchExplorationFor
import net.csgstore.setupskip.AccessibilityUtils.getChildViews
import net.csgstore.setupskip.AccessibilityUtils.getClickableNode
import java.util.*

/**
 * The type Accessibility background service.
 *
 * @author Brian Shenk
 */
class MyAccessibilityService : AccessibilityService() {
    private val appContext: Context by lazy { applicationContext }
    private lateinit var info: AccessibilityServiceInfo
    private lateinit var eventSourceNode: AccessibilityNodeInfo
    private lateinit var eventChildViews: List<AccessibilityNodeInfo>
    private lateinit var rootChildViews: List<AccessibilityNodeInfo>
    private lateinit var parentChildViews: List<AccessibilityNodeInfo>
    private var nodeToHandleEvents: AccessibilityNodeInfo? = null
    private var eventPackageName: String? = null
    private var eventClassName: String? = null
    private var eventText: String? = null

    init {
        instance = this
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(CLASS_NAME, "Service Connected")
        //        info = new AccessibilityServiceInfo();
        //        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        //        info.notificationTimeout = 10;
        //        info.feedbackType = AccessibilityEvent.TYPES_ALL_MASK;
        //        info.packageNames = new String[]{"com.android.settings","com.samsung.spg",OLD_UNINSTALLATION_PACKAGE_NAME,UNINSTALLATION_PACKAGE_NAME};
        //        info.flags = AccessibilityServiceInfo.DEFAULT | AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS | AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS | AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;
        //        setServiceInfo(info);
        //        if (VERSION.SDK_INT >= VERSION_CODES.O) { info.flags = info.flags | AccessibilityServiceInfo.FLAG_REQUEST_FINGERPRINT_GESTURES; }
        disableTouchExploration(instance)
        //        setHasAutoClicked(appContext, false);
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
        val doInAMoment = Runnable {}
        //        if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_APP_SWITCH) {
        //        doInAMoment.run();
        ////            performGlobalAction(GLOBAL_ACTION_HOME);
        //            return false;
        //        }
        val root = rootInActiveWindow
        if (event.action == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_APP_SWITCH) || keyCode == KeyEvent.KEYCODE_BACK && root != null && root.packageName != null && packageName == root.packageName.toString()) {
            doInAMoment.run()
        }
        return super.onKeyEvent(event)
    }

    private var nodeHierarchy: List<List<AccessibilityNodeInfo>>? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        nodeHierarchy = debugValues(event.source, rootInActiveWindow)
        eventSourceNode = event.source
        when (event.eventType) {
            AccessibilityEvent.TYPE_TOUCH_INTERACTION_START, AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START, AccessibilityEvent.TYPE_VIEW_HOVER_ENTER, AccessibilityEvent.TYPE_VIEW_HOVER_EXIT -> if (instance.serviceInfo.capabilities and AccessibilityServiceInfo.CAPABILITY_CAN_REQUEST_TOUCH_EXPLORATION == AccessibilityServiceInfo.CAPABILITY_CAN_REQUEST_TOUCH_EXPLORATION) {
                eventSourceNode = event.source
                nodeToHandleEvents = getClickableNode(eventSourceNode)
            }
            AccessibilityEvent.TYPE_GESTURE_DETECTION_END, AccessibilityEvent.TYPE_TOUCH_INTERACTION_END -> if (instance.serviceInfo.capabilities and AccessibilityServiceInfo.CAPABILITY_CAN_REQUEST_TOUCH_EXPLORATION == AccessibilityServiceInfo.CAPABILITY_CAN_REQUEST_TOUCH_EXPLORATION) {
                updateNodeObjects(event, rootInActiveWindow)
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                findAndClickPref(this)
                updateNodeObjects(event, rootInActiveWindow)
                if (event.packageName != null) {
                    if (doesMeetRequirementsForTouchExploration(event)) {
                        if (!isAPopupDisplayed()) {
                            enableTouchExploration(instance)
                        }
                    } else if (BuildConfig.APPLICATION_ID == event.packageName.toString()) {
                        disableTouchExploration(instance)
                    } else {
                        disableTouchExploration(instance)
                    }
                    //                } else {
                }
            }
            else -> {
            }
        }
    }

    private fun doesMeetRequirementsForTouchExploration(event: AccessibilityEvent): Boolean {
        if (event.className == null) return false
        val classArray = ArrayList(listOf(FRAME_LAYOUT_CLASS))
        if (event.text != null && event.text.isNotEmpty() && classArray.contains(
                event.className.toString())) {
            for (mEventText in event.text) {
                if (mEventText != null && enableTouchExplorationFor.contains(mEventText.toString())) {
                    return true
                }
            }
        }
        return false
    }

    private fun isAPopupDisplayed(@NonNull vararg withText: String): Boolean {
        val root = rootInActiveWindow ?: return false
        for (thisString in withText) {
            if (root.findAccessibilityNodeInfosByText(
                    thisString) != null && root.findAccessibilityNodeInfosByText(
                    thisString).isNotEmpty()) {
                return true
            }
        }
        return false
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun debugClicks(event: AccessibilityEvent, type: Int) {
        val isCorrectType =
            type == AccessibilityEvent.TYPE_VIEW_CLICKED || type == AccessibilityEvent.TYPE_VIEW_FOCUSED || type == AccessibilityEvent.TYPE_VIEW_SELECTED || type == AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED || type == AccessibilityEvent.TYPE_TOUCH_INTERACTION_END || type == AccessibilityEvent.TYPE_TOUCH_INTERACTION_START
        if (isCorrectType) {
            Log.d(TAG, "debugClicks: Hooray!")
        }
    }

    fun performDefaultEvent(source: AccessibilityNodeInfo?, action: Int) {
        source?.performAction(action)
    }

    private fun recycleIfNotIntendedEvent(
        event: AccessibilityEvent?, vararg packageNames: String
    ): Boolean {
        if (event == null || event.packageName == null) {
            return true
        }
        val eventPackageName = event.packageName.toString()
        for (packageName in packageNames) {
            if (packageName.equals(eventPackageName, ignoreCase = true)) {
                return false
            }
        }
        return true
    }

    fun updateNodeObjects(event: AccessibilityEvent, rootNode: AccessibilityNodeInfo) {
        eventSourceNode = event.source
        eventPackageName = when {
            event.packageName != null -> event.packageName.toString()
            else -> rootNode.packageName.toString()
        }
        eventClassName = when {
            event.className != null -> event.className.toString()
            else -> rootNode.className.toString()
        }
        eventText = when {
            event.text != null -> event.text.toString()
            else -> rootNode.text.toString()
        }
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
        //    public AccessibilityBackgroundService() {
        //        appContext = getApplicationContext();
        ////        mSharedPrefs = appContext.getSharedPreferences(SWITCH_PREFERENCE_PREFS, Context.MODE_PRIVATE);
        //    }

        private fun clickNode(mNode: AccessibilityNodeInfo) {
            mNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }

        fun enableTouchExploration(mService: MyAccessibilityService?) {
            val serviceInfo = mService!!.serviceInfo
            serviceInfo.flags =
                serviceInfo.flags or AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE
            mService.serviceInfo = serviceInfo
        }

        fun disableTouchExploration(mService: MyAccessibilityService?) {
            val serviceInfo = mService!!.serviceInfo
            serviceInfo.flags =
                serviceInfo.flags and AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE.inv()
            mService.serviceInfo = serviceInfo
        }

        fun findAndClickPref(
            mService: MyAccessibilityService,
            search: String = "Add Automatically"
        ): Boolean {
            val switchWidgets =
                getChildViews(mService.rootInActiveWindow) as ArrayList<AccessibilityNodeInfo>
            for (mNode in switchWidgets) {
                val resourceName = mNode.viewIdResourceName
                val text = mNode.text.toString()
                if (search.equals(text, ignoreCase = true)) {
                    clickNode(mNode)
                    return true
                }
            }
            return false
        }

        lateinit var instance: MyAccessibilityService
    }
}