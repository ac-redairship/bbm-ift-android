package com.redairship.ocbc.transfer.presentation.transfer.transferto

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ocbc.transfer.databinding.DialogBottomSheetPayeeListBinding
import com.redairship.ocbc.bb.components.extensions.toPx
import com.redairship.ocbc.bb.components.views.bottomsheet.BBBottomSheet
import com.redairship.ocbc.transfer.UiState
import com.redairship.ocbc.transfer.model.BeneficiaryData
import com.redairship.ocbc.transfer.presentation.localtransfer.transferto.PayeeListAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


interface PayeeListBottomInterface {
    fun selectItem(item: BeneficiaryData)
}

class PayeeListBottomSheet(private val selectedPayee: BeneficiaryData?) : BBBottomSheet(),
    PayeeListBottomInterface {
    var onSelectedPayeeClicked: ((data: BeneficiaryData) -> Unit)? = null
    lateinit var binding: DialogBottomSheetPayeeListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCollapsible = true
    }

    override fun getContentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogBottomSheetPayeeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    val viewModel: PayeeListBottomSheetViewModel by sharedViewModel()

    override fun onShow() {
        val behavior = behavior ?: return
//        behavior.peekHeight = resources.displayMetrics.heightPixels
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerview.isNestedScrollingEnabled = true
        binding.recyclerview.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            v.onTouchEvent(event)
            true
        }

        viewModel.getBeneficiaryList()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.beneficiarylist
                .collectLatest {
                    when (it) {
                        is UiState.Success -> {
                            binding.vErrorContainer.isVisible = it.data.isEmpty()
                            binding.recyclerview.isVisible = it.data.isNotEmpty()
//                            isCollapsible = true

                            populateAdapterWithInfo(it.data.map {
                                if (it.payDetail.accountNo == selectedPayee?.payDetail?.accountNo) it.copy(isSelected = true)
                                else it
                            })
                        }
                    }
                }
        }
    }

    private fun populateAdapterWithInfo(list: List<BeneficiaryData>) {
        binding.recyclerview.apply {
            isNestedScrollingEnabled = true
            val defaultMargin = marginTop
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    Log.d("textTitle Y pos", binding.textTitle.y.toString())
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager

                    if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) { //up
                        binding.vLiftShadow.alpha = 0f
                        updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            setMargins(leftMargin, defaultMargin, rightMargin, bottomMargin)
                        }
                        // prevent accidental dismissal on scroll
                        postDelayed({
                            behavior?.isDraggable = true
                        }, 500)
                    } else {
                        if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() >= 2) {
                            binding.vLiftShadow.alpha = 1f
                            updateLayoutParams<ViewGroup.MarginLayoutParams> {
                                setMargins(leftMargin, binding.searchBody.height + binding.searchBody.marginBottom + defaultMargin, rightMargin, bottomMargin)
                            }
                        }
                        behavior?.isDraggable = false
                    }
                }
            })
        }
        val adapter = PayeeListAdapter(this@PayeeListBottomSheet)
        binding.edtSearch.doAfterTextChanged { text ->
            if (text.isNullOrEmpty()) {
                binding.vErrorContainer.isVisible = false
                adapter.submitList(list)
            } else {
                val newList = list.filter { it.name.contains(text.toString(), true) }
                adapter.submitList(newList)
                if (newList.isEmpty()) {
                    binding.vErrorContainer.isVisible = true
                    binding.ivErrorMessage.isVisible = false
                    binding.tvErrorMessage.text = "Payee not found. Try using another search item."
                } else {
                    binding.vErrorContainer.isVisible = false
                }
            }

            adapter.notifyDataSetChanged()
        }

        binding.edtSearch.setOnFocusChangeListener { view, isFocused ->
            binding.searchImageButton.isVisible = !isFocused
        }
        adapter.submitList(list)
        if (list.isEmpty()) {
            binding.searchBody.isVisible = false
            binding.vErrorContainer.isVisible = true
            binding.ivErrorMessage.isVisible = true
            binding.tvErrorMessage.text = "Placeholder, copy will be soon finalised"
            binding.recyclerview.isVisible = false
            setPeekHeight()
        } else {
            binding.recyclerview.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            binding.recyclerview.adapter = adapter
            adapter.notifyDataSetChanged()
        }

    }

    private fun setPeekHeight() {
        behavior?.peekHeight = 300.toPx
        behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun selectItem(item: BeneficiaryData) {
        onSelectedPayeeClicked?.invoke(item)
        dismiss()
    }

}