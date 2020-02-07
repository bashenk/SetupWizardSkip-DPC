package net.csgstore.setupskip;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static net.csgstore.setupskip.PackageManagerFlags.Installer.*;

/** @hide */
@SuppressWarnings("ALL")
@IntDef(flag = true, value = {
        INSTALL_REPLACE_EXISTING,
        INSTALL_ALLOW_TEST,
        INSTALL_INTERNAL,
        INSTALL_FROM_ADB,
        INSTALL_ALL_USERS,
        INSTALL_REQUEST_DOWNGRADE,
        INSTALL_GRANT_RUNTIME_PERMISSIONS,
        INSTALL_ALL_WHITELIST_RESTRICTED_PERMISSIONS,
        INSTALL_FORCE_VOLUME_UUID,
        INSTALL_FORCE_PERMISSION_PROMPT,
        INSTALL_INSTANT_APP,
        INSTALL_DONT_KILL_APP,
        INSTALL_FULL_APP,
        INSTALL_ALLOCATE_AGGRESSIVE,
        INSTALL_VIRTUAL_PRELOAD,
        INSTALL_APEX,
        INSTALL_ENABLE_ROLLBACK,
        INSTALL_ALLOW_DOWNGRADE,
        INSTALL_STAGED,
        INSTALL_DRY_RUN,
})
@Retention(RetentionPolicy.SOURCE)
public @interface InstallFlags {}
