package com.redairship.ocbc.transfer.presentation.common

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.NonNull
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.redairship.ocbc.transfer.UiState
import com.ocbc.transfer.databinding.CurrencyListBottomSheetBinding
import com.redairship.ocbc.transfer.presentation.base.CurrencyCode
import com.redairship.ocbc.transfer.presentation.base.getWindowHeight
import com.redairship.ocbc.transfer.presentation.base.launchWhenCreated
import com.redairship.ocbc.bb.components.R
import com.redairship.ocbc.bb.components.views.bottomsheet.BBBottomSheet
import com.redairship.ocbc.transfer.presentation.localtransfer.transferto.PayeeListAdapter
import kotlinx.android.synthetic.main.currency_list_bottom_sheet.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CurrencyListBottomSheet(
    val selectedCurrency: String?
) : BBBottomSheet(),
    GroupRecyclerViewAdapter.OnGroupRecyclerViewAdapterListener<CurrencyCode>,
    SearchResultsListener {
    var onSelectedCurrencyClicked: ((currency: CurrencyCode) -> Unit)? = null

    lateinit var binding: CurrencyListBottomSheetBinding
    var adapter: CurrencyListAdapter? = null
    var searchMode = false
    val viewModel: CurrencyListBottomSheetViewModel by sharedViewModel()

    override fun getContentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CurrencyListBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCollapsible = false
    }

    override fun onShow() {
        val behavior = behavior ?: return
        behavior.peekHeight = resources.displayMetrics.heightPixels
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerview.isNestedScrollingEnabled = true
        binding.recyclerview.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            v.onTouchEvent(event)
            true
        }

        viewModel.getCurrencyList()

        binding?.edtSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                searchMode = true
                adapter?.getFilter()?.filter(s.toString())
            }
        })

        observeViewModelResponse()

    }

    fun observeViewModelResponse(){
        launchWhenCreated {
            launch {
                viewModel.currencytlist
                    .collectLatest {
                        if (it is UiState.Success) {
                            var favorites = it.data?.filter {
                                it.sequence < 999
                            }
                            var all = it.data?.filter {
                                it.sequence == 999
                            }
                            populateAdapterWithInfo(favorites!!, all!!)
                        }
                    }
            }
        }
    }

    fun populateAdapterWithInfo(favorites: List<CurrencyCode>, all: List<CurrencyCode>) {
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
        adapter = activity?.let { CurrencyListAdapter(selectedCurrency, favorites, all, this@CurrencyListBottomSheet, this@CurrencyListBottomSheet) }
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

        adapter?.let {
            val layoutManager = LinearLayoutManager(context)
            binding.recyclerview.layoutManager = layoutManager
            binding.recyclerview.adapter = it
            it.notifyDataSetChanged()
            resultsFound()
        }
    }


    override fun noResultsFound() {
        binding.vErrorContainer.isVisible = true
        binding.recyclerview.isVisible = false
    }

    override fun resultsFound() {
        binding.vErrorContainer.isVisible = false
        binding.recyclerview.isVisible = true
    }

    override fun onGroupItemClick(item: CurrencyCode) {
        onSelectedCurrencyClicked?.invoke(item)
        dismiss()
    }

    override fun onChildItemClick(item: CurrencyCode) {
        onSelectedCurrencyClicked?.invoke(item)
        dismiss()
    }

}