package com.redairship.ocbc.transfer.presentation.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.ocbc.transfer.databinding.LayoutAmountWithCurrencyBinding
import com.redairship.ocbc.bb.components.extensions.toEditable
import com.redairship.ocbc.bb.components.models.Amount
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class AmountWithCurrencyTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var amount: Amount = Amount(BigDecimal.ZERO, "$", Locale("en", "SG"), "SGD")
        set(value) {
            field = value

            val symbols = DecimalFormatSymbols(value.locale)
            val pattern = "#,##0.##"
            val decimalFormat = DecimalFormat(pattern, symbols)
            val formattedDecimal: String = decimalFormat.format(value.value.toLong())

            binding.tvAmount.text = formattedDecimal.toEditable()
            binding.tvCurrency.text = value.currency
            
        }

    val binding: LayoutAmountWithCurrencyBinding

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = LayoutAmountWithCurrencyBinding.inflate(inflater, this, true)
    }
}