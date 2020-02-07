package net.csgstore.setupskip;

import android.widget.Toast;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef(value = {
        Toast.LENGTH_SHORT,
        Toast.LENGTH_LONG
})
@Retention(RetentionPolicy.SOURCE)
public @interface Duration { }
