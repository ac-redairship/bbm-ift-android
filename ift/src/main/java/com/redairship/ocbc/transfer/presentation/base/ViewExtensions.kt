package com.redairship.ocbc.transfer.presentation.base

import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.redairship.ocbc.transfer.presentation.common.AmountWithCurrencyTextView

fun AmountWithCurrencyTextView.doAfterTextChanged(action: (text: Editable) -> Unit) {
//    this.addTextChangeListener(object : TextWatcher {
//        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
//
//        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//        }
//
//        override fun afterTextChanged(p0: Editable) {
//            action.invoke(p0)
//        }
//    })
}

//fun AmountWithCurrencyIconView.setOnEditorActionListener(action: (actionId: Int) -> Unit) {
//    this.addTextKeyBoardListener( object: TextView.OnEditorActionListener {
//        override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
//            if (p1 == EditorInfo.IME_ACTION_DONE) {
//                clearFocus()
//            }
//            return false
//        }
//    })
//}