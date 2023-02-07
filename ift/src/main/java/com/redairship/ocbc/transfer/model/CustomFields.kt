package com.redairship.ocbc.transfer.model

import java.io.Serializable

data class CustomFields(
    val Product: String,
    val Currencies: List<Currency>,
) : Serializable

data class Currency(
    val entry: String,
    val sequence: Int
) : Serializable
