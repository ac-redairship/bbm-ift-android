package com.redairship.ocbc.transfer.model

import java.io.Serializable

data class CheckValueDate(
    val workingHourFlag: Boolean,
    val workingDayFlag: Boolean,
    val nextWorkingDayFlag: Boolean,
    val evalDateTime: String,
    val nextDayOffset: Int,
    val variant: String,
    val groupCodes: List<String>,
    val isWorkingDay: Boolean,
    val isWorkingHour: Boolean,
    val nextWorkingDay: String,
    val nextValueDate: String,
    val maxDay: Int = 0
) : Serializable