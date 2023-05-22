package com.redairship.ocbc.transfer.utils

import android.text.Html
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

class DataTransform {
    companion object {
        val STRING_DASH = "-"

        fun formatMoney(amount: BigDecimal): CharSequence {
            var amount = amount
            return try {
                var mark = ""
                var amountStr = ""
                val formatStr: StringBuffer
                if (amount == null) {
                    return STRING_DASH
                } else if (amount != null && amount.compareTo(BigDecimal("0.00")) < 0) {
                    amount = amount.setScale(2, BigDecimal.ROUND_FLOOR)
                    amountStr = amount.toString()
                    mark = amountStr.substring(0, 1)
                    amountStr = amountStr.substring(1)
                    formatStr = StringBuffer(mark)
                } else {
                    amount = amount.setScale(2, BigDecimal.ROUND_FLOOR)
                    amountStr = amount.toString()
                    formatStr = StringBuffer()
                }
                val splitAmount = amountStr.split(".").toTypedArray()
                val beforePoint = splitAmount[0].toCharArray()
                val afterPoint: MutableList<String> = ArrayList()
                var count = 0
                for (i in beforePoint.indices.reversed()) {
                    afterPoint.add(beforePoint[i].toString())
                    if ((count + 1) % 3 == 0 && count + 1 < beforePoint.size) {
                        afterPoint.add(",")
                    }
                    count++
                }
                for (i in afterPoint.indices.reversed()) {
                    formatStr.append(afterPoint[i])
                }
                formatStr.append(".")
                formatStr.append(splitAmount[1])
                Html.fromHtml(formatStr.toString())
            } catch (e: Exception) {
                STRING_DASH
            }
        }

        fun getFormatDate(date: Date, format: DateFormat): String {
            var dateFormat: SimpleDateFormat? = null
            var afterFormatDate: String = ""
            try {
                when (format) {
                    DateFormat.FORMATE_A -> {
                        dateFormat = SimpleDateFormat("dd MM yyyy", Locale.ENGLISH)
                        afterFormatDate = dateFormat.format(date)
                    }
                    DateFormat.FORMATE_B -> {
                        dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                        afterFormatDate = dateFormat.format(date)
                    }
                    DateFormat.FORMATE_C -> {
                        dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
                        afterFormatDate = dateFormat.format(date)
                    }
                    DateFormat.FORMATE_D -> {
                        dateFormat = SimpleDateFormat("dd MM", Locale.ENGLISH)
                        afterFormatDate = dateFormat.format(date)
                    }
                    DateFormat.FORMATE_E -> {
                        dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                        afterFormatDate = dateFormat.format(date)
                    }
                    DateFormat.FORMATE_F -> {
                        dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.ENGLISH)
                        afterFormatDate = dateFormat.format(date)
                    }
                    DateFormat.FORMATE_G -> {
                        dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH)
                        afterFormatDate = dateFormat.format(date)
                    }
                    DateFormat.FORMATE_H -> {
                        dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH)
                        afterFormatDate = dateFormat.format(date)
                    }
                    DateFormat.FORMATE_I -> {
                        dateFormat = SimpleDateFormat("EEEEEEEEEEEE, dd MMM yyyy", Locale.ENGLISH)
                        afterFormatDate = dateFormat.format(date)
                    }
                    DateFormat.FORMATE_J -> {
                        dateFormat = SimpleDateFormat("hh:mm:ss", Locale.ENGLISH)
                        afterFormatDate = dateFormat.format(date)
                    }
                    DateFormat.FORMATE_K -> {
                        dateFormat = SimpleDateFormat("dd MMM, hh:mm", Locale.ENGLISH)
                        afterFormatDate = dateFormat.format(date)
                    }
                    DateFormat.FORMATE_L -> {
                        dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.ENGLISH)
                        afterFormatDate = dateFormat.format(date)
                    }
                    DateFormat.FORMATE_M -> {
                        dateFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
                        afterFormatDate = dateFormat.format(date)
                    }
                    DateFormat.FORMATE_N -> {
                        dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm aa", Locale.ENGLISH)
                        afterFormatDate = dateFormat.format(date)
                    }
                    DateFormat.FORMATE_O -> {
                        dateFormat = SimpleDateFormat("dd/MM/yyyy, hh:mm aa", Locale.ENGLISH)
                        afterFormatDate = dateFormat.format(date)
                    }
                    DateFormat.FORMATE_Z -> {
                        dateFormat = SimpleDateFormat("d MMM yyyy, hh:mmaa", Locale.ENGLISH)
                        afterFormatDate = dateFormat.format(date)
                    }
                    DateFormat.FORMATE_P -> {
                        dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH)
                        afterFormatDate = dateFormat.format(date)
                    }
                    DateFormat.FORMATE_Q -> {
                        dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm aa", Locale.ENGLISH)
                        afterFormatDate = dateFormat.format(date)
                    }
                    DateFormat.FORMATE_R -> {
                        dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                        afterFormatDate = dateFormat.format(date)
                        val dateFormatter = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
                        val toDayCalendar = Calendar.getInstance()
                        val toDayCalendarDate = dateFormatter.format(toDayCalendar.time)
                        val compareDate = dateFormatter.format(date.time)
                        if (toDayCalendarDate == compareDate) {
                            afterFormatDate += " (Today) "
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                return STRING_DASH
            }
            return afterFormatDate
        }
    }

    fun formatMoney(inputAmount: BigDecimal?): CharSequence {
        var amount = inputAmount

        try {
            val mark: String
            var amountStr: String
            val formatStr: StringBuffer

            when {
                amount == null -> {
                    return STRING_DASH
                }

                amount < BigDecimal(0.00) -> {
                    amount = amount.setScale(2, BigDecimal.ROUND_CEILING)
                    amountStr = amount.toString()
                    mark = amountStr.substring(0, 1)
                    amountStr = amountStr.substring(1)
                    formatStr = StringBuffer(mark)
                }

                else -> {
                    amount = amount.setScale(2, BigDecimal.ROUND_CEILING)
                    amountStr = amount.toString()
                    formatStr = StringBuffer()
                }
            }

            val splitAmount =
                amountStr.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val beforePoint = splitAmount[0].toCharArray()
            val afterPoint = ArrayList<String>()

            for ((count, i) in beforePoint.indices.reversed().withIndex()) {
                afterPoint.add(beforePoint[i].toString())

                if ((count + 1) % 3 == 0 && count + 1 < beforePoint.size) {
                    afterPoint.add(",")
                }

            }

            for (i in afterPoint.indices.reversed()) {
                formatStr.append(afterPoint[i])
            }

            formatStr.append(".")
            formatStr.append(splitAmount[1])
            return Html.fromHtml(formatStr.toString())

        } catch (e: Exception) {
            return STRING_DASH
        }
    }
}