//package com.redairship.ocbc.transfer.presentation.common
//
//import android.app.Dialog
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.FrameLayout
//import androidx.annotation.NonNull
//import com.google.android.material.bottomsheet.BottomSheetBehavior
//import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
//import com.google.android.material.bottomsheet.BottomSheetDialog
//import com.google.android.material.bottomsheet.BottomSheetDialogFragment
//import com.ocbc.transfer.databinding.TransferBottomSheetScrollableCalendarBinding
//import com.redairship.ocbc.transfer.presentation.base.convertStringToDate
//import com.redairship.ocbc.bb.components.R
//import kotlinx.android.synthetic.main.transfer_bottom_sheet_scrollable_calendar.*
//import java.text.SimpleDateFormat
//import java.util.*
//
//class ScrollCalendarBottomSheet(val selectedDate: String?) : BottomSheetDialogFragment() {
//
//    var onSelectedDateClicked: ((date: Date)->Unit)? = null
//
//    companion object {
//        const val KEY_SELECTEDDATE = "selecteddate"
//    }
//
//    lateinit var binding: TransferBottomSheetScrollableCalendarBinding
//    var behavior: BottomSheetBehavior<*>? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setStyle(STYLE_NORMAL, R.style.ras_components_BBBottomSheetDialogTheme)
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        binding = TransferBottomSheetScrollableCalendarBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
//        bottomSheetDialog.setOnShowListener {
//            bottomSheetDialog.findViewById<FrameLayout>(
//                com.google.android.material.R.id.design_bottom_sheet
//            )?.let { bottomSheet ->
//                behavior = BottomSheetBehavior.from(bottomSheet).apply {
//                    state = BottomSheetBehavior.STATE_EXPANDED
//                    setBottomSheetCallback(mBottomSheetBehaviorCallback)
//                }
//            }
//
//        }
//        bottomSheetDialog.dismissWithAnimation = true
//
//        return bottomSheetDialog
//    }
//
//    private val mBottomSheetBehaviorCallback: BottomSheetCallback = object : BottomSheetCallback() {
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
//        val nextYear = Calendar.getInstance()
//        nextYear.add(Calendar.MONTH, 3)
//        val lastYear = Calendar.getInstance()
//        lastYear.add(Calendar.YEAR, 0)
//
//        val deactivateDates = ArrayList<Int>()
//        deactivateDates.add(1)
//        deactivateDates.add(7)
//        calendar_view.deactivateDates(deactivateDates)
//
//        val highlightDates = ArrayList<Date>()
//        var selectDate = Date()
//        try {
//            selectedDate?.let {
//                selectDate = convertStringToDate(it)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        highlightDates.add(selectDate)
//
//        calendar_view.init(
//            lastYear.time,
//            nextYear.time,
//            SimpleDateFormat("MMM YYYY", Locale.getDefault())
//        )
//            .inMode(CalendarPickerView.SelectionMode.SINGLE)
//            .withDeactivateDates(deactivateDates)
//            .withSelectedDate(selectDate)
////        .withHighlightedDates(highlightDates)
//
//        calendar_view.scrollToDate(Date())
//
//        bt_primary_button.setOnClickListener {
//            if (calendar_view.selectedDates.isNotEmpty()) {
//                onSelectedDateClicked?.invoke(calendar_view.selectedDates.get(0))
//                dismiss()
//            }
//        }
//    }
//
//}