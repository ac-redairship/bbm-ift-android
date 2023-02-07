package com.redairship.ocbc.transfer.presentation.base

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.text.Editable
import android.util.DisplayMetrics
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.redairship.ocbc.transfer.utils.DataTransform
import com.redairship.ocbc.transfer.utils.DateFormat
import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

val alphanumericPattern = Pattern.compile("[^a-zA-Z0-9 \\ ]")
val alphanumericDashPattern = Pattern.compile("[^a-zA-Z0-9- \\ ]")

fun checkAlphanumericDashPattern(accountNumber: String): Boolean{
    return !alphanumericDashPattern.matcher(accountNumber).find()
}

inline fun Fragment.launchWhenStarted(crossinline block: suspend CoroutineScope.() -> Unit) {
    viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        block()
    }
}

inline fun Fragment.launchWhenCreated(crossinline block: suspend CoroutineScope.() -> Unit) {
    viewLifecycleOwner.lifecycleScope.launchWhenCreated {
        block()
    }
}

inline fun Fragment.launchWhenResumed(crossinline block: suspend CoroutineScope.() -> Unit) {
    viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        block()
    }
}

fun editTextAmountToString(editable: Editable): String {
    var text = ""
    if (editable.length == 0) {
        text = "0.00" + editable.toString()
    } else if (editable.length > 0 && editable.length == 1) {
        text = "0.0" + editable.toString()
    } else if (editable.length > 0 && editable.length == 2) {
        text = "0." + editable.toString()
    } else if (editable.length > 2) {
        val editText: String = editable.toString()
        val appendedText = editText.substring(
            0,
            editText.length - 2
        ) + "." + editText.substring(editText.length - 2, editText.length)
        try {
            text = DataTransform.formatMoney(BigDecimal(appendedText)).toString()
        } catch (e: Exception) {
        }
    }
    return text
}

internal fun convertStrToDateFormat(stringdate : String):String {
    return DataTransform.getFormatDate(convertStringToDate(stringdate), DateFormat.FORMATE_R)
}

internal fun convertStringToDate(stringdate: String): Date {
    var date = Date()
    try {
        date = SimpleDateFormat(BaseTransferFragment.TRANSFER_DATE_FORMAT).parse(stringdate)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return date
}

fun getStatusBarHeight(activity: Activity): Int {
    val rect = Rect()
    activity.window.decorView.getWindowVisibleDisplayFrame(rect)
    return rect.top
}

private fun takeScreenshot(
    context: Context,
    bitmap: Bitmap,
    fileName: String
): Uri {
    val imagesFolder = File(context.cacheDir, "pdf")
    imagesFolder.mkdirs()
    val file = File(imagesFolder, fileName)
    val output = FileOutputStream(file)

    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    output.flush()
    output.close()

    return FileProvider.getUriForFile(context, context.applicationContext.packageName.toString() + ".provider", file)
}

fun viewShoot(context: Context, layout: View, fileName:String): Uri {
    var h = 0

    if (layout is LinearLayout) {
        for (i in 0 until layout.childCount) {
            h += layout.getChildAt(i).height
        }
    } else {
        h = layout.height
    }

    val bitmap = Bitmap.createBitmap(layout.width, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)
    layout.draw(canvas)
    return takeScreenshot(context, bitmap, fileName)
}

fun getWindowHeight(context: Context?): Int {
    // Calculate window height for fullscreen use
    val displayMetrics = DisplayMetrics()
    (context as Activity?)!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.heightPixels
}

internal fun convertDateToString(date: Date): String {
    return SimpleDateFormat(BaseTransferFragment.TRANSFER_DATE_FORMAT).format(date)
}
