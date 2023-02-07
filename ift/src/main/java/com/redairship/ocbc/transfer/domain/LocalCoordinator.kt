package com.redairship.ocbc.transfer.domain

import androidx.fragment.app.Fragment
import com.redairship.domain.common.Coordinator
import com.redairship.ocbc.bb.components.views.headers.BBStandardHeader

interface LocalCoordinator: Coordinator {
    fun navigateTo(fragment: Fragment, addToBackStack: Boolean)
}