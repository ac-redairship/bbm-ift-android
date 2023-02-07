package com.redairship.ocbc.transfer.presentation.base

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.ocbc.transfer.R
import com.redairship.ocbc.transfer.model.AccountItemModel
import com.redairship.ocbc.transfer.presentation.local.LocalTransferViewModel
import com.redairship.ocbc.bb.components.views.fragments.BaseFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

abstract class BaseTransferFragment<DB : ViewDataBinding>: BaseFragment() {
    protected val sharedViewModel: LocalTransferViewModel by sharedViewModel()
    var transferStatus: TransferStatus? = null

    /**
     * the data binding related to this fragment
     */
    protected val dataBinding: DB
        get() = binding!!

    /**
     * internal data binding, after [onDestroy], it will be set to null
     */
    private var binding: DB? = null

    /**
     * the layout of this fragment, it is a layout resource
     */
    @LayoutRes
    protected abstract fun getContentLayoutId(): Int

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
        binding = DataBindingUtil.inflate(inflater, getContentLayoutId(), container, false)
        dataBinding.lifecycleOwner = this
        onBindView()
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                backStack()
//            }
//        })
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
        when(type) {
            TransferStatus.TransferFrom -> {
                sharedViewModel.getLocalTransferData().selectFromAc = item
            }
            TransferStatus.TransferToMyAccounts -> {
                sharedViewModel.getLocalTransferData().selectToAc = item
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