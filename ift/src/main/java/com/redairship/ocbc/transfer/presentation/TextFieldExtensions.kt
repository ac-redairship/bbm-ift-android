package com.redairship.ocbc.transfer.presentation

import android.text.Editable
import android.text.TextWatcher
import com.redairship.ocbc.bb.components.views.textviews.BBTextField

fun BBTextField.doAfterTextChanged(action: (text: String) -> Unit) {
    this.addTextChangeListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

        override fun afterTextChanged(p0: Editable?) {
            action.invoke(p0?.toString() ?: "")
        }
    })
}