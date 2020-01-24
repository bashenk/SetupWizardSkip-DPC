package net.csgstore.setupskip

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.TargetApi
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.*

/**
 * @author Brian Shenk
 */
object AccessibilityUtils {

    const val SETTINGS_PACKAGE = "com.android.settings"
    const val SETTINGS_MAIN_CLASS = "com.android.settings.Settings"
    const val SETTINGS_SUBSETTINGS_CLASS = "com.android.settings.SubSettings"
    const val SETTINGS_WIFI_SETTINGS_CLASS = "com.android.settings.Settings\$WifiSettingsActivity"
    const val SETTINGS_WIFIAP_SETTINGS_CLASS =
        "com.android.settings.Settings\$WifiApSettingsActivity"
    const val SETTINGS_WIFI_TETHERING_SETTINGS_CLASS =
        "com.android.settings.Settings\$TetherSettingsActivity"
    const val SETTINGS_WIFI_SETUP_HELP_CLASS =
        "com.samsung.android.settings.helpdialog.TwHelpAnimatedDialog"
    const val WIFI_CONTROL_HISTORY_TEXT = "[Wi-Fi control history]"
    const val APP_INFO_TEXT = "[App info]"
    const val OK_BUTTON_TEXT = "OK"
    const val MOBILE_HOTSPOT_TEXT = "MOBILE HOTSPOT"
    const val MOBILE_HOTSPOT_INITIALCAPS_TEXT = "Mobile Hotspot"
    const val WIFI_TEXT = "Wi-Fi"
    const val CHECKING_SUBSCRIPTION_TEXT = "Checking subscription status…"
    const val DATA_CONNECTION_NOT_AVAILABLE_TEXT = "Data connection not available."
    const val CONFIGURE_MOBILE_HOTSPOT_TEXT = "Configure Mobile Hotspot"
    const val TIMEOUT_SETTINGS_TEXT = "Timeout settings"
    const val TURNING_WIFI_ON_OR_OFF_TEXT = "Turning Wi-Fi on or off"
    const val WIFI_AP_DISABLE_WARNING_RES_ID =
        "com.android.settings:id/wifi_ap_disable_warning_dialog_content"
    const val WIFI_AP_WARNING_NOT_SHOW_AGAIN_RES_ID =
        "com.android.settings:id/wifi_ap_warning_disable_do_not_show_again"
    // TODO: Find what this was for again, and rename it appropriately
    const val BUTTON1_RES_ID = "android:id/button1"
    const val SETTINGS_ON_BOARDING_SETTINGS_CLASS =
        "com.sec.android.inputmethod.implement.setting.OnBoardingSettingsActivity"
    const val ACCESSIBILITY_SETTINGS_CLASS =
        "com.android.settings.Settings\$AccessibilitySettingsActivity"
    const val RELATIVE_LAYOUT_CLASS = "android.widget.RelativeLayout"
    const val IMAGE_BUTTON_CLASS = "android.widget.ImageButton"
    const val FRAME_LAYOUT_CLASS = "android.widget.FrameLayout"
    const val ALERT_DIALOG_CLASS = "android.app.AlertDialog"
    val SWITCH_PREFERENCES_CLASSES =
        arrayOf("android.widget.Switch", "android.widget.SwitchPreference")
    val forbidden = ArrayList(listOf(SETTINGS_MAIN_CLASS))
    val fullScreenOverlay = ArrayList(listOf(WIFI_CONTROL_HISTORY_TEXT, APP_INFO_TEXT))
    val backButtonOverlay = ArrayList(
        listOf(SETTINGS_WIFI_SETTINGS_CLASS, SETTINGS_WIFIAP_SETTINGS_CLASS,
            SETTINGS_WIFI_TETHERING_SETTINGS_CLASS, SETTINGS_WIFI_SETUP_HELP_CLASS))
    const val ADD_TO_HOME_SCREEN = "Add to Home screen"
    val enableTouchExplorationFor = ArrayList(
        listOf(ADD_TO_HOME_SCREEN))
    val excludeFromTouchExplorationStrings =
        ArrayList(listOf(CONFIGURE_MOBILE_HOTSPOT_TEXT, TIMEOUT_SETTINGS_TEXT))
    val removeBackButtonOverlay = ArrayList(listOf(SETTINGS_SUBSETTINGS_CLASS))
    val viewIdsAndTextToClick = ArrayList(
        listOf(WIFI_AP_DISABLE_WARNING_RES_ID, WIFI_AP_WARNING_NOT_SHOW_AGAIN_RES_ID,
            BUTTON1_RES_ID, OK_BUTTON_TEXT))
    val removeOverlayForText =
        ArrayList(listOf(CHECKING_SUBSCRIPTION_TEXT, DATA_CONNECTION_NOT_AVAILABLE_TEXT))
    private val stuffIFound = ArrayList(
        listOf(SETTINGS_ON_BOARDING_SETTINGS_CLASS, ACCESSIBILITY_SETTINGS_CLASS,
            RELATIVE_LAYOUT_CLASS, FRAME_LAYOUT_CLASS, ALERT_DIALOG_CLASS))
    private const val OLD_UNINSTALLATION_PACKAGE_NAME = "com.android.packageinstaller"
    private const val UNINSTALLATION_PACKAGE_NAME = "com.google.android.packageinstaller"
    private const val AUTOMATION_CLICKED = "automation_clicked"
    const val AUTOMATION_RUNNING = "automation_running"
    private val info: AccessibilityServiceInfo? = null
    private val rootNode: AccessibilityNodeInfo? = null
    private val eventSourceNode: AccessibilityNodeInfo? = null
    private val eventChildViews: List<AccessibilityNodeInfo>? = null
    private val rootChildViews: List<AccessibilityNodeInfo>? = null
    private val parentChildViews: List<AccessibilityNodeInfo>? = null
    private val nodeToHandleEvents: AccessibilityNodeInfo? = null
    private val packageName: String? = null
    private val className: String? = null
    private val mText: String? = null

    /**
     * Helper method for [AccessibilityUtils.findChildViews]
     *
     * @param parentNodeInfo The parent AccessibilityNodeInfo for all the child views
     * @return List with added nodes, or new ArrayList if none
     */
    @JvmStatic
    fun getChildViews(parentNodeInfo: AccessibilityNodeInfo?): List<AccessibilityNodeInfo> {
        return findChildViews(null, parentNodeInfo, ArrayList())
    }

    /**
     * Helper method for [AccessibilityUtils.findChildViews]
     *
     * @param parentNodeInfo The parent AccessibilityNodeInfo for all the child views
     * @param classToMatch   Null or a String to match against the className for the child view
     * @return List with added nodes, or new ArrayList if none
     */
    @JvmStatic
    fun getChildViews(
        parentNodeInfo: AccessibilityNodeInfo?, classToMatch: String?
    ): List<AccessibilityNodeInfo> {
        return findChildViews(ArrayList(listOf(classToMatch)), parentNodeInfo, ArrayList())
    }

    /**
     * Helper method for [AccessibilityUtils.findChildViews]
     *
     * @param parentNodeInfo The parent AccessibilityNodeInfo for all the child views
     * @param classesToMatch Null, or a List of Strings to match against the className for the child view
     * @return List with added nodes, or new ArrayList if none
     */
    @JvmStatic
    fun getChildViews(
        parentNodeInfo: AccessibilityNodeInfo?, classesToMatch: List<String?>?
    ): List<AccessibilityNodeInfo> {
        return findChildViews(classesToMatch, parentNodeInfo, ArrayList())
    }

    /**
     * Helper method  for [AccessibilityUtils.findChildViews]
     *
     * @param parentNodeInfo The parent AccessibilityNodeInfo for all the child views
     * @param classesToMatch Null or a String array to match against the className for the child view
     * @return List with added nodes, or new ArrayList if none
     */
    @JvmStatic
    fun getChildViews(
        parentNodeInfo: AccessibilityNodeInfo?, vararg classesToMatch: String?
    ): List<AccessibilityNodeInfo> {
        val toMatch: ArrayList<String?>?
        toMatch = ArrayList(listOf(*classesToMatch))
        return findChildViews(toMatch, parentNodeInfo, ArrayList())
    }

    /**
     * Finds all child views from a parent node, and optionally matches the child view's className or ViewIdResourceName against a string or list of strings
     *
     * @param toMatch           Null or an ArrayList of Strings to match against the className for the child view
     * @param parentNodeInfo    The parent AccessibilityNodeInfo for all the child views
     * @param nodeInfoArrayList ArrayList to be populated with the child views
     * @return List with added nodes, or new ArrayList if none
     */
    @JvmStatic
    private fun findChildViews(
        toMatch: List<String?>?,
        parentNodeInfo: AccessibilityNodeInfo?,
        nodeInfoArrayList: ArrayList<AccessibilityNodeInfo>
    ): List<AccessibilityNodeInfo> {
        var nodeList: ArrayList<AccessibilityNodeInfo>? = nodeInfoArrayList
        if (parentNodeInfo == null) {
            return ArrayList()
        }
        if (nodeList == null) {
            nodeList = ArrayList()
        }
        val childCount = parentNodeInfo.childCount
        if (childCount == 0) {
            if (toMatch != null) {
                if (parentNodeInfo.className != null && toMatch.contains(
                        parentNodeInfo.className.toString()) || parentNodeInfo.viewIdResourceName != null && toMatch.contains(
                        parentNodeInfo.viewIdResourceName) || parentNodeInfo.text != null && toMatch.contains(
                        parentNodeInfo.text.toString())) {
                    nodeList.add(parentNodeInfo)
                }
            } else {
                nodeList.add(parentNodeInfo)
            }
        } else {
            for (i in 0 until childCount) {
                val child = parentNodeInfo.getChild(i)
                findChildViews(toMatch, child, nodeList)
            }
        }
        return nodeList
    }

    /**
     * Gets matching node info.
     *
     * @param toMatch        the [<] that is intended to match
     * @param parentNodeInfo The highest parent [AccessibilityNodeInfo] available, from which we will parse down the children to find the matching values
     * @return the matching node info, or null
     */
    @JvmStatic
    fun getMatchingNodeInfo(
        toMatch: List<String>, parentNodeInfo: AccessibilityNodeInfo?
    ): AccessibilityNodeInfo? {
        if (parentNodeInfo == null || parentNodeInfo.className == null) {
            return null
        }
        for (stringToMatch in toMatch) {
            var className: String? = null
            if (parentNodeInfo.className != null) {
                className = parentNodeInfo.className.toString()
            }
            val childCount = parentNodeInfo.childCount
            var viewIdResourceName: String? = null
            if (parentNodeInfo.viewIdResourceName != null) {
                viewIdResourceName = parentNodeInfo.viewIdResourceName
            }
            var text: String? = null
            if (parentNodeInfo.text != null) {
                text = parentNodeInfo.text.toString()
            }
            if (childCount == 0) {
                if (stringToMatch.equals(className, ignoreCase = true) || stringToMatch.equals(
                        viewIdResourceName, ignoreCase = true) || stringToMatch.equals(text,
                        ignoreCase = true)) {
                    return parentNodeInfo
                }
            } else {
                for (i in 0 until childCount) {
                    val child = parentNodeInfo.getChild(i)
                    val matchingInfo = getMatchingNodeInfo(toMatch, child)
                    if (matchingInfo != null) {
                        return matchingInfo
                    }
                }
            }
        }
        return null
    }

    @JvmStatic
    fun getMatchingNodeInfo(
        toMatch: List<String>, vararg parentNodeInfo: AccessibilityNodeInfo
    ): AccessibilityNodeInfo? {
        return findMatch(toMatch, ArrayList(listOf(*parentNodeInfo)))
    }

    @SafeVarargs
    @JvmStatic
    fun getMatchingNodeInfo(
        toMatch: List<String>, vararg nodeInfoList: List<AccessibilityNodeInfo>
    ): AccessibilityNodeInfo? {
        var match: AccessibilityNodeInfo?
        val nodeInfoArrayList = ArrayList(listOf(*nodeInfoList))
        for (nodeInfos in nodeInfoArrayList) {
            match = findMatch(toMatch, nodeInfos)
            if (match != null) {
                return match
            }
        }
        return null
    }

    @JvmStatic
    fun doesMatch(
        toMatch: List<String>, vararg parentNodeInfo: AccessibilityNodeInfo
    ): Boolean {
        return findMatch(toMatch, ArrayList(listOf(*parentNodeInfo))) != null
    }

    @SafeVarargs
    @JvmStatic
    fun doesMatch(
        toMatch: List<String>, vararg nodeInfoList: List<AccessibilityNodeInfo>
    ): Boolean {
        var match: Boolean
        val nodeInfoArrayList = ArrayList(listOf(*nodeInfoList))
        for (nodeInfos in nodeInfoArrayList) {
            match = findMatch(toMatch, nodeInfos) != null
            if (match) {
                return true
            }
        }
        return false
    }

    private fun findMatch(
        toMatch: List<String>, nodeInfoArrayList: List<AccessibilityNodeInfo>
    ): AccessibilityNodeInfo? {
        for (nodeInfo in nodeInfoArrayList) {
            var toReturn: Boolean
            if (nodeInfo.className != null) {
                val className = nodeInfo.className.toString()
                toReturn = toMatch.contains(className)
                if (toReturn) {
                    return nodeInfo
                }
            }
            if (nodeInfo.viewIdResourceName != null) {
                val resourceName = nodeInfo.viewIdResourceName
                toReturn = toMatch.contains(resourceName)
                if (toReturn) {
                    return nodeInfo
                }
            }
            if (nodeInfo.text != null) {
                val text = nodeInfo.text.toString()
                toReturn = toMatch.contains(text)
                if (toReturn) {
                    return nodeInfo
                }
            }
        }
        return null
    }

    /**
     * Used to create a [<] of [<] that each contain all of the child views in the passed [AccessibilityNodeInfo].
     *
     * @param nodeInfos All of the [AccessibilityNodeInfo]'s
     * @return The [<] of  [<]
     */
    @JvmStatic
    fun debugValues(vararg nodeInfos: AccessibilityNodeInfo): List<List<AccessibilityNodeInfo>> {
        val accessibilityNodeInfos: List<AccessibilityNodeInfo> = ArrayList(listOf(*nodeInfos))
        val viewHierarchy = ArrayList<List<AccessibilityNodeInfo>>()
        for (nodeInfo in accessibilityNodeInfos) {
            // This basically just gets the getRootInActiveWindow
            //            while (nodeInfo.getParent() != null) {
            //                nodeInfo = nodeInfo.getParent();
            //            }
            if (getChildViews(nodeInfo).isNotEmpty()) {
                viewHierarchy.add(getChildViews(nodeInfo))
            }
        }
        //        ArrayList<List<AccessibilityNodeInfo>> viewHierarchyClone = (ArrayList<List<AccessibilityNodeInfo>>) viewHierarchy.clone();
        //        for (List<AccessibilityNodeInfo> accessibilityNodeInfoList : viewHierarchyClone) {
        //            if (accessibilityNodeInfoList.isEmpty()) {
        //                viewHierarchy.remove(accessibilityNodeInfoList);
        //            }
        //        }
        viewHierarchy.trimToSize()
        return viewHierarchy
    }

    /**
     * Finds a clickable node from the parents of the current node if it is not clickable.
     *
     * @param nodeInfo the node info
     * @return the clickable node, or null if none were found
     */
    @JvmStatic
    fun getClickableNode(nodeInfo: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var nodeInfo = nodeInfo ?: return null
        val nodeChildren = getChildViews(nodeInfo)
        var isClickable = nodeInfo.isClickable
        while (!isClickable) {
            if (nodeInfo.parent == null) {
                return null
            }
            nodeInfo = nodeInfo.parent
            isClickable = nodeInfo.isClickable
        }
        return nodeInfo
    }

    @Deprecated("")
    private fun getMatchingChildNodes(
        rootNode: AccessibilityNodeInfo,
        event: AccessibilityEvent,
        eventType: Int,
        packageName: String?,
        className: String?
    ): ArrayList<AccessibilityNodeInfo> {
        val nodeArray = ArrayList<AccessibilityNodeInfo>()
        if (event.eventType == eventType) {
            val mNodeList = getChildViews(rootNode) as ArrayList<AccessibilityNodeInfo>
            var thisPackageName: String? = null
            if (event.packageName != null) {
                thisPackageName = event.packageName.toString()
            }
            var thisClassName: String? = null
            if (event.className != null) {
                thisClassName = event.className.toString()
            }
            for (mNode in mNodeList) {
                if (packageName == null || packageName.equals(thisPackageName, ignoreCase = true)) {
                    if (className == null || className.equals(thisClassName, ignoreCase = true)) {
                        nodeArray.add(mNode)
                    }
                }
            }
        }
        return nodeArray
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @JvmStatic
    private fun closeDialog(event: AccessibilityEvent) {
        var nodeInfo = event.source
        if (nodeInfo == null) {
            nodeInfo = rootNode
            if (nodeInfo == null) {
                return
            }
        }
        nodeInfo = getClickableNode(nodeInfo)
        val list = nodeInfo!!.findAccessibilityNodeInfosByText("CANCEL")
        if (list != null) {
            for (node in list) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }
    }

    private fun preventUninstall(event: AccessibilityEvent) {
        if (packageName != null && (OLD_UNINSTALLATION_PACKAGE_NAME == packageName || UNINSTALLATION_PACKAGE_NAME == packageName)) {
            closeDialog(event)
        }
    }

    //    public static void updateNodeObjects (AccessibilityEvent event, AccessibilityNodeInfo rootNode) {
    //        AccessibilityUtils.eventSourceNode = event.getSource();
    //        AccessibilityUtils.rootNode = rootNode;
    //        AccessibilityUtils.packageName = String.valueOf(event.getPackageName() != null ? event.getPackageName() : rootNode != null ? rootNode.getPackageName() : null);
    //        AccessibilityUtils.className = String.valueOf(event.getClassName() != null ? event.getClassName() : rootNode != null ? rootNode.getClassName() : null);
    //        AccessibilityUtils.mText = String.valueOf(event.getText() != null ? event.getText() : rootNode != null ? rootNode.getText() : null);
    //    }
    @JvmStatic
    fun doStringsMatch(
        eventTextOrClassNameOrPackageName: String, comparisonString: String?
    ): Boolean {
        return eventTextOrClassNameOrPackageName.equals(comparisonString, ignoreCase = true)
    }
}