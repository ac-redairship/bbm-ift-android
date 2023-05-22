package com.redairship.ocbc.transfer.presentation.common

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.ocbc.transfer.R
import com.ocbc.transfer.databinding.LayoutAmountWithCurrencyBinding
import com.redairship.ocbc.bb.components.extensions.toEditable
import com.redairship.ocbc.bb.components.models.Amount
import com.redairship.ocbc.transfer.presentation.base.getFlagResouce
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class AmountWithCurrencyTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val SGD = Currency.getInstance("SGD")
    var amount: Amount = Amount(value = BigDecimal.ZERO, symbol = SGD.symbol, locale = Locale("en", "SG"), currency = SGD)
        set(value) {
            field = value

            val symbols = DecimalFormatSymbols(value.locale)
            val pattern = "#,##0.##"
            val decimalFormat = DecimalFormat(pattern, symbols)
            val formattedDecimal: String = decimalFormat.format(value.value.toLong())

            binding.tvAmount.text = formattedDecimal.toEditable()
            binding.tvCurrency.text = value.currency.currencyCode
            
        }

    val binding: LayoutAmountWithCurrencyBinding

    var downImage: Boolean = true
        set(value) {
            field = value
            val id = context?.let { getFlagResouce(amount.currency.currencyCode, it) }
            binding.tvCurrency.setCompoundDrawablesRelativeWithIntrinsicBounds(id ?: 0, 0, if (value) R.drawable.ras_components_ic_bb_chevron_down_thick_light else 0, 0);
        }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = LayoutAmountWithCurrencyBinding.inflate(inflater, this, true)
    }
}