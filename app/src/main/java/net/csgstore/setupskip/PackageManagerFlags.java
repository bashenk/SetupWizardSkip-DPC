package net.csgstore.setupskip;

import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.os.storage.StorageManager;

public class PackageManagerFlags {
    public static class Uninstaller {
        /**
         * Flag parameter for {@link PackageManager#deletePackage} to indicate that you don't want to delete the
         * package's data directory.
         *
         * @hide
         */
        public static final int DELETE_KEEP_DATA = 0x00000001;

        /**
         * Flag parameter for {@link PackageManager#deletePackage} to indicate that you want the
         * package deleted for all users.
         *
         * @hide
         */
        public static final int DELETE_ALL_USERS = 1 << 1;

        /**
         * Flag parameter for {@link PackageManager#deletePackage} to indicate that, if you are calling
         * uninstall on a system that has been updated, then don't do the normal process
         * of uninstalling the update and rolling back to the older system version (which
         * needs to happen for all users); instead, just mark the app as uninstalled for
         * the current user.
         *
         * @hide
         */
        public static final int DELETE_SYSTEM_APP = 1 << 2;

        /**
         * Flag parameter for {@link PackageManager#deletePackage} to indicate that, if you are calling
         * uninstall on a package that is replaced to provide new feature splits, the
         * existing application should not be killed during the removal process.
         *
         * @hide
         */
        public static final int DELETE_DONT_KILL_APP = 1 << 3;

        /**
         * Flag parameter for {@link PackageManager#deletePackage} to indicate that any
         * contributed media should also be deleted during this uninstall. The
         * meaning of "contributed" means it won't automatically be deleted when the
         * app is uninstalled.
         *
         * @hide
         */
        public static final int DELETE_CONTRIBUTED_MEDIA = 0x00000010;

        /**
         * Flag parameter for {@link PackageManager#deletePackage} to indicate that package deletion
         * should be chatty.
         *
         * @hide
         */
        public static final int DELETE_CHATTY = 0x80000000;
    }

    public static class Installer {
        /**
         * Flag parameter for {@link #installPackage} to indicate that you want to
         * replace an already installed package, if one exists.
         *
         * @hide
         */
        public static final int INSTALL_REPLACE_EXISTING = 0x00000002;

        /**
         * Flag parameter for {@link #installPackage} to indicate that you want to
         * allow test packages (those that have set android:testOnly in their
         * manifest) to be installed.
         * @hide
         */
        public static final int INSTALL_ALLOW_TEST = 0x00000004;

        /**
         * Flag parameter for {@link #installPackage} to indicate that this package
         * must be installed to internal storage.
         *
         * @hide
         */
        public static final int INSTALL_INTERNAL = 0x00000010;

        /**
         * Flag parameter for {@link #installPackage} to indicate that this install
         * was initiated via ADB.
         *
         * @hide
         */
        public static final int INSTALL_FROM_ADB = 0x00000020;

        /**
         * Flag parameter for {@link #installPackage} to indicate that this install
         * should immediately be visible to all users.
         *
         * @hide
         */
        public static final int INSTALL_ALL_USERS = 0x00000040;

        /**
         * Flag parameter for {@link #installPackage} to indicate that an upgrade to a lower version
         * of a package than currently installed has been requested.
         *
         * <p>Note that this flag doesn't guarantee that downgrade will be performed. That decision
         * depends
         * on whenever:
         * <ul>
         * <li>An app is debuggable.
         * <li>Or a build is debuggable.
         * <li>Or {@link #INSTALL_ALLOW_DOWNGRADE} is set.
         * </ul>
         *
         * @hide
         */
        public static final int INSTALL_REQUEST_DOWNGRADE = 0x00000080;

        /**
         * Flag parameter for {@link #installPackage} to indicate that all runtime
         * permissions should be granted to the package. If {@link #INSTALL_ALL_USERS}
         * is set the runtime permissions will be granted to all users, otherwise
         * only to the owner.
         *
         * @hide
         */
        public static final int INSTALL_GRANT_RUNTIME_PERMISSIONS = 0x00000100;

        /**
         * Flag parameter for {@link #installPackage} to indicate that all restricted
         * permissions should be whitelisted. If {@link #INSTALL_ALL_USERS}
         * is set the restricted permissions will be whitelisted for all users, otherwise
         * only to the owner.
         *
         * @hide
         */
        public static final int INSTALL_ALL_WHITELIST_RESTRICTED_PERMISSIONS = 0x00400000;

        /** {@hide} */
        public static final int INSTALL_FORCE_VOLUME_UUID = 0x00000200;

        /**
         * Flag parameter for {@link #installPackage} to indicate that we always want to force
         * the prompt for permission approval. This overrides any special behaviour for internal
         * components.
         *
         * @hide
         */
        public static final int INSTALL_FORCE_PERMISSION_PROMPT = 0x00000400;

        /**
         * Flag parameter for {@link #installPackage} to indicate that this package is
         * to be installed as a lightweight "ephemeral" app.
         *
         * @hide
         */
        public static final int INSTALL_INSTANT_APP = 0x00000800;

        /**
         * Flag parameter for {@link #installPackage} to indicate that this package contains
         * a feature split to an existing application and the existing application should not
         * be killed during the installation process.
         *
         * @hide
         */
        public static final int INSTALL_DONT_KILL_APP = 0x00001000;

        /**
         * Flag parameter for {@link #installPackage} to indicate that this package is
         * to be installed as a heavy weight app. This is fundamentally the opposite of
         * {@link #INSTALL_INSTANT_APP}.
         *
         * @hide
         */
        public static final int INSTALL_FULL_APP = 0x00004000;

        /**
         * Flag parameter for {@link #installPackage} to indicate that this package
         * is critical to system health or security, meaning the system should use
         * {@link StorageManager#FLAG_ALLOCATE_AGGRESSIVE} internally.
         *
         * @hide
         */
        public static final int INSTALL_ALLOCATE_AGGRESSIVE = 0x00008000;

        /**
         * Flag parameter for {@link #installPackage} to indicate that this package
         * is a virtual preload.
         *
         * @hide
         */
        public static final int INSTALL_VIRTUAL_PRELOAD = 0x00010000;

        /**
         * Flag parameter for {@link #installPackage} to indicate that this package
         * is an APEX package
         *
         * @hide
         */
        public static final int INSTALL_APEX = 0x00020000;

        /**
         * Flag parameter for {@link #installPackage} to indicate that rollback
         * should be enabled for this install.
         *
         * @hide
         */
        public static final int INSTALL_ENABLE_ROLLBACK = 0x00040000;

        /**
         * Flag parameter for {@link #installPackage} to indicate that package verification should be
         * disabled for this package.
         *
         * @hide
         */
        public static final int INSTALL_DISABLE_VERIFICATION = 0x00080000;

        /**
         * Flag parameter for {@link #installPackage} to indicate that
         * {@link #INSTALL_REQUEST_DOWNGRADE} should be allowed.
         *
         * @hide
         */
        public static final int INSTALL_ALLOW_DOWNGRADE = 0x00100000;

        /**
         * Flag parameter for {@link #installPackage} to indicate that this package
         * is being installed as part of a staged install.
         *
         * @hide
         */
        public static final int INSTALL_STAGED = 0x00200000;

        /**
         * Flag parameter for {@link #installPackage} to indicate that package should only be verified
         * but not installed.
         *
         * @hide
         */
        public static final int INSTALL_DRY_RUN = 0x00800000;
    }

    public static class PackageInstallObserver {

        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link PackageManager#installPackage(android.net.Uri, IPackageInstallObserver, int)} on success.
         * @hide
         */
        public static final int INSTALL_SUCCEEDED = 1;

        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} if the package is
         * already installed.
         * @hide
         */
        public static final int INSTALL_FAILED_ALREADY_EXISTS = -1;

        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} if the package archive
         * file is invalid.
         * @hide
         */
        public static final int INSTALL_FAILED_INVALID_APK = -2;

        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} if the URI passed in
         * is invalid.
         * @hide
         */
        public static final int INSTALL_FAILED_INVALID_URI = -3;

        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} if the package manager
         * service found that the device didn't have enough storage space to install the app.
         * @hide
         */
        public static final int INSTALL_FAILED_INSUFFICIENT_STORAGE = -4;

        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} if a
         * package is already installed with the same name.
         * @hide
         */
        public static final int INSTALL_FAILED_DUPLICATE_PACKAGE = -5;

        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
         * the requested shared user does not exist.
         * @hide
         */
        public static final int INSTALL_FAILED_NO_SHARED_USER = -6;

        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
         * a previously installed package of the same name has a different signature
         * than the new package (and the old package's data was not removed).
         * @hide
         */
        public static final int INSTALL_FAILED_UPDATE_INCOMPATIBLE = -7;

        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
         * the new package is requested a shared user which is already installed on the
         * device and does not have matching signature.
         * @hide
         */
        public static final int INSTALL_FAILED_SHARED_USER_INCOMPATIBLE = -8;

        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
         * the new package uses a shared library that is not available.
         * @hide
         */
        public static final int INSTALL_FAILED_MISSING_SHARED_LIBRARY = -9;

        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
         * the new package uses a shared library that is not available.
         * @hide
         */
        public static final int INSTALL_FAILED_REPLACE_COULDNT_DELETE = -10;

        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
         * the new package failed while optimizing and validating its dex files,
         * either because there was not enough storage or the validation failed.
         * @hide
         */
        public static final int INSTALL_FAILED_DEXOPT = -11;

        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
         * the new package failed because the current SDK version is older than
         * that required by the package.
         * @hide
         */
        public static final int INSTALL_FAILED_OLDER_SDK = -12;

        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
         * the new package failed because it contains a content provider with the
         * same authority as a provider already installed in the system.
         * @hide
         */
        public static final int INSTALL_FAILED_CONFLICTING_PROVIDER = -13;

        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
         * the new package failed because the current SDK version is newer than
         * that required by the package.
         * @hide
         */
        public static final int INSTALL_FAILED_NEWER_SDK = -14;

        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
         * the new package failed because it has specified that it is a test-only
         * package and the caller has not supplied the {@link #INSTALL_ALLOW_TEST}
         * flag.
         * @hide
         */
        public static final int INSTALL_FAILED_TEST_ONLY = -15;

        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
         * the package being installed contains native code, but none that is
         * compatible with the the device's CPU_ABI.
         * @hide
         */
        public static final int INSTALL_FAILED_CPU_ABI_INCOMPATIBLE = -16;

        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
         * the new package uses a feature that is not available.
         * @hide
         */
        public static final int INSTALL_FAILED_MISSING_FEATURE = -17;

        // ------ Errors related to sdcard
        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
         * a secure container mount point couldn't be accessed on external media.
         * @hide
         */
        public static final int INSTALL_FAILED_CONTAINER_ERROR = -18;

        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
         * the new package couldn't be installed in the specified install
         * location.
         * @hide
         */
        public static final int INSTALL_FAILED_INVALID_INSTALL_LOCATION = -19;

        /**
         * Installation return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
         * the new package couldn't be installed in the specified install
         * location because the media is not available.
         * @hide
         */
        public static final int INSTALL_FAILED_MEDIA_UNAVAILABLE = -20;

        /**
         * Installation parse return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)}
         * if the parser was given a path that is not a file, or does not end with the expected
         * '.apk' extension.
         * @hide
         */
        public static final int INSTALL_PARSE_FAILED_NOT_APK = -100;

        /**
         * Installation parse return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)}
         * if the parser was unable to retrieve the AndroidManifest.xml file.
         * @hide
         */
        public static final int INSTALL_PARSE_FAILED_BAD_MANIFEST = -101;

        /**
         * Installation parse return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)}
         * if the parser encountered an unexpected exception.
         * @hide
         */
        public static final int INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION = -102;

        /**
         * Installation parse return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)}
         * if the parser did not find any certificates in the .apk.
         * @hide
         */
        public static final int INSTALL_PARSE_FAILED_NO_CERTIFICATES = -103;

        /**
         * Installation parse return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)}
         * if the parser found inconsistent certificates on the files in the .apk.
         * @hide
         */
        public static final int INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES = -104;

        /**
         * Installation parse return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)}
         * if the parser encountered a CertificateEncodingException in one of the
         * files in the .apk.
         * @hide
         */
        public static final int INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING = -105;

        /**
         * Installation parse return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)}
         * if the parser encountered a bad or missing package name in the manifest.
         * @hide
         */
        public static final int INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME = -106;

        /**
         * Installation parse return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)}
         * if the parser encountered a bad shared user id name in the manifest.
         * @hide
         */
        public static final int INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID = -107;

        /**
         * Installation parse return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)}
         * if the parser encountered some structural problem in the manifest.
         * @hide
         */
        public static final int INSTALL_PARSE_FAILED_MANIFEST_MALFORMED = -108;

        /**
         * Installation parse return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)}
         * if the parser did not find any actionable tags (instrumentation or application)
         * in the manifest.
         * @hide
         */
        public static final int INSTALL_PARSE_FAILED_MANIFEST_EMPTY = -109;

        /**
         * Installation failed return code: this is passed to the {@link IPackageInstallObserver} by
         * {@link #installPackage(android.net.Uri, IPackageInstallObserver, int)}
         * if the system failed to install the package because of system issues.
         * @hide
         */
        public static final int INSTALL_FAILED_INTERNAL_ERROR = -110;
    }
}
