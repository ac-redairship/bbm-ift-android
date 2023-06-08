package com.redairship.ocbc.transfer.presentation.base

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.ocbc.transfer.R
import com.redairship.ocbc.transfer.model.AccountItemModel
import com.redairship.ocbc.transfer.presentation.local.LocalTransferViewModel
import com.redairship.ocbc.bb.components.views.fragments.BaseFragment
import com.redairship.ocbc.transfer.LocalTransferData
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

abstract class BaseTransferFragment<VB : ViewBinding>: BaseFragment() {
    protected val sharedViewModel: LocalTransferViewModel by sharedViewModel()
    protected lateinit var localTransferData: LocalTransferData
    var transferStatus: TransferStatus? = null

    /**
     * internal data binding, after [onDestroy], it will be set to null
     */
    protected lateinit var binding: VB

    protected abstract fun bindView(inflater: LayoutInflater, container: ViewGroup?): View

    /**
     * if you need to execute something in [onCreateView]
     * do it here
     */
    protected abstract fun onBindView()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = bindView(inflater, container)
        onBindView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                backStack()
//            }
//        })

        sharedViewModel.localTransferData.value?.let {
            localTransferData = it
        }
        sharedViewModel.localTransferData.observe(viewLifecycleOwner) {
            localTransferData = it
        }

        setViewData(transferStatus)
        setupViewEvents()
    }

    abstract fun setupViewEvents()
    abstract fun setViewData(transfertype: TransferStatus?)

    fun backStack() {
//        when (findNavController().currentDestination?.id) {
//            R.id.transferlocal_entry -> {
//                requireActivity().finish()
//            }
//            else -> {
//                transferStatus = TransferStatus.TransferFrom
//                findNavController().popBackStack()
//            }
//        }
    }

    fun updateSelectAccountItem(type: TransferStatus, item: AccountItemModel) {
        val localTransferData = sharedViewModel.localTransferData.value ?: return
        when(type) {
            TransferStatus.TransferFrom -> {
                sharedViewModel.updateLocalTransferData(
                    localTransferData.copy(
                        senderAccountData = item
                    )
                )
            }
            TransferStatus.TransferToMyAccounts -> {
                sharedViewModel.updateLocalTransferData(
                    localTransferData.copy(
                        recipientAccountData = item
                    )
                )
            }
        }
    }

    fun showGenericError(
        message: String?,
        closeActivity: Boolean
    ) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(message)

        builder.setPositiveButton("OK") { dialogInterface, i ->
            dialogInterface.dismiss()
            if (closeActivity)
                activity?.finish()
        }
        var alertDialog:AlertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.darkblue))
    }

    companion object {
        const val TRANSFER_DATE_FORMAT = "yyyy-MM-dd"
    }

}