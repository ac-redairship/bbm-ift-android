package com.ocbc.transfer.presentation.local

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.ocbc.transfer.R
import com.ocbc.transfer.databinding.ActivityTransferLocalRevampBinding
import com.redairship.ocbc.bb.components.views.fragments.errors.GenericServerErrorFragment


class TransferActivity : AppCompatActivity(), GenericServerErrorFragment.Listener {

    private lateinit var dataBinding: ActivityTransferLocalRevampBinding

//    override fun getLoadingBinding(): LayoutLoadingBinding = dataBinding.loadingLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = ActivityTransferLocalRevampBinding.inflate(layoutInflater)
        setContentView(dataBinding.root)
        enableFullScreenMode()
    }

    private fun enableFullScreenMode() {
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setDecorFitsSystemWindows(false)
            }
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }
    }

    var fragment:GenericServerErrorFragment? = null

    fun showGenericServerErrorScreen(message:String?) {
        dataBinding.vRoot.isVisible = true

        val tag = GenericServerErrorFragment::class.java.simpleName
        fragment = GenericServerErrorFragment.newInstance(
            getString(R.string.service_generic_err_mutiple_login_title),
            if (message.isNullOrBlank()) "Sorry, we could not process your request. Please try again." else message,
            "Retry",
            com.redairship.ocbc.bb.components.R.raw.ras_components_system_error,
            true
        )
        supportFragmentManager.beginTransaction().replace(R.id.v_root, fragment!!)
            .disallowAddToBackStack().commit()
    }

    fun hideGenericServerErrorScreen() {
        dataBinding.vRoot.isVisible = false
        supportFragmentManager.beginTransaction().remove(fragment!!).commit()
    }

    override fun onGenericServerErrorCtaClicked(fragment: GenericServerErrorFragment) {
        hideGenericServerErrorScreen()
    }

}