//package com.ocbc.transfer.presentation.common
//
//import android.app.Dialog
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.FrameLayout
//import androidx.annotation.NonNull
//import androidx.core.view.isVisible
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.android.material.bottomsheet.BottomSheetBehavior
//import com.google.android.material.bottomsheet.BottomSheetDialog
//import com.google.android.material.bottomsheet.BottomSheetDialogFragment
//import com.ocbc.components.grouprecycler.OnGroupRecyclerViewAdapterListener
//import com.redairship.ocbc.transfer.UiState
//import com.ocbc.transfer.databinding.CurrencyListBottomSheetBinding
//import com.redairship.ocbc.transfer.presentation.base.CurrencyCode
//import com.redairship.ocbc.transfer.presentation.base.getWindowHeight
//import com.redairship.ocbc.transfer.presentation.base.launchWhenCreated
//import com.redairship.ocbc.bb.components.R
//import kotlinx.android.synthetic.main.currency_list_bottom_sheet.*
//import kotlinx.coroutines.flow.collectLatest
//import kotlinx.coroutines.launch
//import org.koin.androidx.viewmodel.ext.android.sharedViewModel
//
//class CurrencyListBottomSheet(
//    val selectedCurrency: String?
//) : BottomSheetDialogFragment(),
//    OnGroupRecyclerViewAdapterListener<CurrencyCode> {
//    var onSelectedCurrencyClicked: ((currency: CurrencyCode) -> Unit)? = null
//
//    lateinit var binding: CurrencyListBottomSheetBinding
//    var adapter: CurrencyListAdapter? = null
//    var searchMode = false
//    val viewModel: CurrencyListBottomSheetViewModel by sharedViewModel()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setStyle(STYLE_NORMAL, R.style.ras_components_BBBottomSheetDialogTheme)
//        viewModel.getCurrencyList()
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        binding = CurrencyListBottomSheetBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
//        bottomSheetDialog.setOnShowListener {
//            bottomSheetDialog.findViewById<FrameLayout>(
//                com.google.android.material.R.id.design_bottom_sheet
//            )?.let { bottomSheet ->
//                BottomSheetBehavior.from(bottomSheet).apply {
//                    val layoutParams = bottomSheet!!.layoutParams
//                    val windowHeight: Int = getWindowHeight(context)
//                    if (layoutParams != null) {
//                        layoutParams.height = windowHeight
//                    }
//                    bottomSheet.layoutParams = layoutParams
//                    state = BottomSheetBehavior.STATE_EXPANDED
//                    setBottomSheetCallback(mBottomSheetBehaviorCallback)
//                }
//            }
//        }
//        return bottomSheetDialog
//    }
//
//    private val mBottomSheetBehaviorCallback: BottomSheetBehavior.BottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
//        override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
//            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
//                dismiss()
//            }
//        }
//        override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {}
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding?.edtSearch?.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
//            override fun afterTextChanged(s: Editable) {
//                searchMode = true
//                adapter?.getFilter()?.filter(s.toString())
//            }
//        })
//
//        observeViewModelResponse()
//
//    }
//
//    fun observeViewModelResponse(){
//        launchWhenCreated {
//            launch {
//                viewModel.currencytlist
//                    .collectLatest {
//                        if (it is UiState.Success) {
//                            var favorites = it.data?.filter {
//                                it.sequence < 999
//                            }
//                            var all = it.data?.filter {
//                                it.sequence == 999
//                            }
//                            populateAdapterWithInfo(favorites!!, all!!)
//                        }
//                    }
//            }
//        }
//    }
//
//    fun populateAdapterWithInfo(favorites: List<CurrencyCode>, all: List<CurrencyCode>) {
//        binding.recyclerview.apply {
//            isNestedScrollingEnabled = true
//            addOnScrollListener(object: RecyclerView.OnScrollListener(){
//                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                    super.onScrollStateChanged(recyclerView, newState)
//                    searchMode = false
//                }
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    super.onScrolled(recyclerView, dx, dy)
//                    if (binding?.edtSearch?.text.isNullOrEmpty() && !searchMode) {
//                        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
//                        if (linearLayoutManager.findFirstVisibleItemPosition() == 0) { //up
//                            search_body.visibility = View.GONE
//                        } else {
//                            search_body.visibility = View.VISIBLE
//                        }
//                    } else {
//                        search_body.visibility = View.VISIBLE
//                    }
//                }
//            })
//        }
//        adapter =
//            activity?.let { CurrencyListAdapter(selectedCurrency, favorites, all, this@CurrencyListBottomSheet) }
//        adapter?.let {
//            val layoutManager = LinearLayoutManager(context)
//            binding.recyclerview.layoutManager = layoutManager
//            binding.recyclerview.adapter = it
//            it.notifyDataSetChanged()
//            resultsFound()
//        }
//    }
//
//    override fun onGroupRecyclerViewAdapterSelected(item: CurrencyCode) {
//        onSelectedCurrencyClicked?.invoke(item)
//        dismiss()
//    }
//
//    override fun noResultsFound() {
//        binding.emptymessage.isVisible = true
//        binding.recyclerview.isVisible = false
//    }
//
//    override fun resultsFound() {
//        emptymessage.isVisible = false
//        binding.recyclerview.isVisible = true
//    }
//
//
//}