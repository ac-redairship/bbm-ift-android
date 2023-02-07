package com.redairship.ocbc.transfer.presentation.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.NonNull
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.redairship.ocbc.transfer.UiState
import com.ocbc.transfer.databinding.DialogBottomSheetPayeeListBinding
import com.redairship.ocbc.transfer.model.BeneficiaryData
import com.redairship.ocbc.transfer.presentation.base.getWindowHeight
import com.redairship.ocbc.bb.components.R
import kotlinx.android.synthetic.main.currency_list_bottom_sheet.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

interface PayeeListBottomInterface {
    fun selectItem(item: BeneficiaryData)
}

class PayeeListBottomSheet() :  BottomSheetDialogFragment(), PayeeListBottomInterface {
    var onSelectedPayeeClicked: ((data: BeneficiaryData) -> Unit)? = null
    var searchMode = false
    var listAdapter : PayeeListAdapter? = null
    lateinit var binding: DialogBottomSheetPayeeListBinding
    val viewModel: PayeeListBottomSheetViewModel by sharedViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.ras_components_BBBottomSheetDialogTheme)
        viewModel.getBeneficiaryList()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener {
            bottomSheetDialog.findViewById<FrameLayout>(
                com.google.android.material.R.id.design_bottom_sheet
            )?.let { bottomSheet ->
                BottomSheetBehavior.from(bottomSheet).apply {
                    val layoutParams = bottomSheet!!.layoutParams
                    val windowHeight: Int = getWindowHeight(context)
                    if (layoutParams != null) {
                        layoutParams.height = windowHeight
                    }
                    bottomSheet.layoutParams = layoutParams
                    state = BottomSheetBehavior.STATE_EXPANDED
                    setBottomSheetCallback(mBottomSheetBehaviorCallback)
                }
            }
        }
        return bottomSheetDialog
    }

    private val mBottomSheetBehaviorCallback: BottomSheetBehavior.BottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                dismiss()
            }
        }
        override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogBottomSheetPayeeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.beneficiarylist
                .collectLatest {
                    when (it) {
                        is UiState.Success-> {
                            binding.emptyLly.isVisible = it.data.isEmpty()
                            binding.searchBody.isVisible = it.data.isNotEmpty()
                            binding.recyclerview.isVisible = it.data.isNotEmpty()

                            populateAdapterWithInfo(it.data)
                        }
                    }
                }
        }
    }

    private fun populateAdapterWithInfo(list: List<BeneficiaryData>) {
        binding.recyclerview.apply {
            isNestedScrollingEnabled = true
            addOnScrollListener(object: RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    searchMode = false
                }
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (binding?.edtSearch?.text.isNullOrEmpty() && !searchMode) {
                        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                        if (linearLayoutManager.findFirstVisibleItemPosition() == 0) { //up
                            search_body.visibility = View.GONE
                        } else {
                            search_body.visibility = View.VISIBLE
                        }
                    } else {
                        search_body.visibility = View.VISIBLE
                    }
                }
            })
        }
        listAdapter =
            activity?.let { PayeeListAdapter(list, this@PayeeListBottomSheet) }
        binding.recyclerview.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerview.adapter = listAdapter
        listAdapter?.notifyDataSetChanged()
    }

    override fun selectItem(item: BeneficiaryData) {
        onSelectedPayeeClicked?.invoke(item)
        dismiss()
    }

}