package net.csgstore.setupskip;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static net.csgstore.setupskip.PackageManagerFlags.Uninstaller.*;

@SuppressWarnings("AlibabaClassMustHaveAuthor")
@IntDef(flag = true, value = {
        DELETE_KEEP_DATA,
        DELETE_ALL_USERS,
        DELETE_SYSTEM_APP,
        DELETE_DONT_KILL_APP,
        DELETE_CHATTY
})
@Retention(RetentionPolicy.SOURCE)
public @interface DeleteFlags {}
