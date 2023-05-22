package com.redairship.ocbc.transfer.presentation.base

enum class TransferStatus(val description: String) {
    TransferToUEN("Mobile no./NRIC/FIN/UEN/VPA"),
    TransferToOtherBank("Other bank account"),
    TransferToOcbcAccount("Other OCBC account"),
    TransferToMyAccounts("My OCBC account"),

    TransferFrom("Transfer from"),
    TransferTo("Transfer to"),



    TransferMakerReview("Review"),
    TransferConfirm("Confirm"),
}