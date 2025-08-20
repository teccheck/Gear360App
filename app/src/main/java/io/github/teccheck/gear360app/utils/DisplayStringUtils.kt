package io.github.teccheck.gear360app.utils

import android.content.Context
import io.github.teccheck.gear360app.R

object DisplayStringUtils {

    fun percentage(context: Context, value: Int?) : String {
        value ?: return context.getString(R.string.no_value)
        return  context.getString(R.string.percentage, value)
    }

}