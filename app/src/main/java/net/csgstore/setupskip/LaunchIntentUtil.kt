package net.csgstore.setupskip

import android.accounts.Account
import android.content.Intent
import android.os.PersistableBundle


/**
 * Common utility functions used for retrieving information from the intent that launched TestDPC.
 */
object LaunchIntentUtil {
    const val EXTRA_ACCOUNT_NAME = "account_name"
    const val EXTRA_AFFILIATION_ID = "affiliation_id"
    private const val EXTRA_IS_SETUP_WIZARD = "is_setup_wizard"
    private const val EXTRA_ACCOUNT = "account"
    /**
     * @returns true if TestDPC was launched as part of synchronous authentication flow in setup
     * wizard or settings->add account
     */
    fun isSynchronousAuthLaunch(launchIntent: Intent?): Boolean {
        return launchIntent != null && launchIntent.extras != null && launchIntent.extras!![EXTRA_IS_SETUP_WIZARD] != null
    }

    /**
     * @returns true if TestDPC was launched as part of a COSU deployment. This is identified by
     * a cosu extra in the admin extras.
     */
    fun isCosuLaunch(extras: PersistableBundle?): Boolean {
        return extras != null && (extras.get(EnableCosuActivity.BUNDLE_KEY_COSU_CONFIG) != null);    }

    /**
     * @returns true if TestDPC was launched as part of synchronous authentication flow in setup
     * wizard or settings->add account, based upon the extras of the given bundle which was
     * populated by [.prepareDeviceAdminExtras]
     */
    fun isSynchronousAuthLaunch(extras: PersistableBundle?): Boolean { // NOTE: Value here is irrelevant, only presence of extra matters - true indicates sync-
        // auth from Setup Wizard, false indicates another path (e.g. Settings->Add Account).
        return extras != null && extras[EXTRA_IS_SETUP_WIZARD] != null
    }

    /**
     * @returns an account, if TestDPC was informed of which account it was invoked as a result of
     * adding in synchronous auth cases
     */
    fun getAddedAccount(intent: Intent?): Account? {
        return intent?.getParcelableExtra(EXTRA_ACCOUNT)
    }

    /**
     * @returns the account name in the given bundle, as populated by
     * [.prepareDeviceAdminExtras]
     */
    fun getAddedAccountName(persistableBundle: PersistableBundle?): String? {
        return persistableBundle?.getString(EXTRA_ACCOUNT_NAME, null)
    }

    /**
     * Copy important intent extras from the launching intent launchIntent into newBundle.
     */
    fun prepareDeviceAdminExtras(launchIntent: Intent, newBundle: PersistableBundle) {
        if (isSynchronousAuthLaunch(launchIntent)) {
            val isSetupWizard = launchIntent.getBooleanExtra(EXTRA_IS_SETUP_WIZARD, false)
            // Store as String in new bundle, as API 21 doesn't support putBoolean.
            newBundle.putString(EXTRA_IS_SETUP_WIZARD, java.lang.Boolean.toString(isSetupWizard))
            val addedAccount = getAddedAccount(launchIntent)
            if (addedAccount != null) {
                newBundle.putString(EXTRA_ACCOUNT_NAME, addedAccount.name)
            }
        }
    }
}