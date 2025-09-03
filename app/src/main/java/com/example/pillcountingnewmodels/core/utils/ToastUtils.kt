package com.example.pillcountingnewmodels.core.utils

import android.content.Context
import android.widget.Toast

object ToastUtils {
    fun show(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }
}
