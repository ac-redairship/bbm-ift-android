package com.redairship.ocbc.transfer.model

import java.io.Serializable

data class E2EEDetailsResponse (
    val encryptKey : String,
    val exponent : String,
    val modulus : String,
    val securityNonce : String,
    val productCode : String,
): Serializable
{
    var activationCode: String = ""
    var encryptedMessage: String? = null

    val requestedProperties: Array<String>
        get() {
            return encryptKey.replace("$.","").split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        }
}