package com.redairship.ocbc.transfer.presentation.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.FrameLayout
import androidx.annotation.NonNull
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ocbc.transfer.databinding.TransferBottomSheetScrollableCalendarBinding
import com.redairship.ocbc.bb.components.R
import com.redairship.ocbc.bb.components.models.FontWeight
import com.redairship.ocbc.bb.components.models.OpenSansStyle
import com.redairship.ocbc.bb.components.utils.FontCacheUtils
import com.redairship.ocbc.bb.components.views.calendar.CalendarPickerView
import com.redairship.ocbc.transfer.presentation.base.convertStringToDate
import kotlinx.android.synthetic.main.transfer_bottom_sheet_scrollable_calendar.*
import java.util.*

class ScrollCalendarBottomSheet(val selectedDate: String?) : BottomSheetDialogFragment() {

    var onSelectedDateClicked: ((date: Date)->Unit)? = null

    companion object {
        const val KEY_SELECTEDDATE = "selecteddate"
    }

    lateinit var binding: TransferBottomSheetScrollableCalendarBinding
    var behavior: BottomSheetBehavior<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.ras_components_BBBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = TransferBottomSheetScrollableCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener {
            bottomSheetDialog.findViewById<FrameLayout>(
                com.google.android.material.R.id.design_bottom_sheet
            )?.let { bottomSheet ->
                behavior = BottomSheetBehavior.from(bottomSheet).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    isDraggable = false
                    setBottomSheetCallback(mBottomSheetBehaviorCallback)
                }
            }

        }
        bottomSheetDialog.dismissWithAnimation = true

        return bottomSheetDialog
    }

    private val mBottomSheetBehaviorCallback: BottomSheetCallback = object : BottomSheetCallback() {
        override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                dismiss()
            }
        }
        override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendar_view.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {}
            override fun onScroll(
                view: AbsListView?,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {
                if (firstVisibleItem == 0 && listIsAtTop()) {
                    behavior?.isDraggable = true
                    v_lift_shadow.isVisible = false
                }else{
                    behavior?.isDraggable = false
                    v_lift_shadow.isVisible = true
                }
            }
        })
//
//        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
//            binding.liftShadow.isVisible = !(verticalOffset == 0 || abs(verticalOffset) - appBarLayout.totalScrollRange == 0)
//        })
//
        val nextYear = Calendar.getInstance()
        nextYear.add(Calendar.MONTH, 3)
        val lastYear = Calendar.getInstance()
        lastYear.add(Calendar.DAY_OF_YEAR, (lastYear.get(Calendar.DAY_OF_MONTH) + 1)*-1)

        val deactivateDates = ArrayList<Int>()
        deactivateDates.add(1)
        deactivateDates.add(7)
        val typeface = FontCacheUtils.getTypeface(
            OpenSansStyle.findByFontWeight(
                FontWeight.Bold
            ), requireContext()
        )
        val regularTypeface = FontCacheUtils.getTypeface(
            OpenSansStyle.findByFontWeight(
                FontWeight.Normal
            ), requireContext()
        )
        calendar_view.setTitleTypeface(typeface)
        calendar_view.setDateTypeface(regularTypeface)
        calendar_view.setTodayTypeface(typeface)
        calendar_view.setDateSelectableFilter {
            val calendar = Calendar.getInstance().apply { time = it }
            return@setDateSelectableFilter calendar.get(Calendar.DAY_OF_YEAR) >= Calendar.getInstance().get(Calendar.DAY_OF_YEAR) && calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY &&
                calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY
        }

        val highlightDates = ArrayList<Date>()
        var selectDate = Date()
        try {
            selectedDate?.let {
                selectDate = convertStringToDate(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        highlightDates.add(selectDate)

        calendar_view.init(
            lastYear.time,
            nextYear.time,
//            SimpleDateFormat("MMM YYYY", Locale.getDefault())
        )
            .inMode(CalendarPickerView.SelectionMode.SINGLE)
//            .withDeactivateDates(deactivateDates)
            .withSelectedDate(selectDate)
//            .withHighlightedDates(highlightDates)
//        .withHighlightedDates(highlightDates)

        calendar_view.scrollToDate(Date())

        bt_primary_button.setOnClickListener {
            if (calendar_view.selectedDates.isNotEmpty()) {
                onSelectedDateClicked?.invoke(calendar_view.selectedDates.get(0))
                dismiss()
            }
        }
    }

    private fun listIsAtTop(): Boolean {
        return if (calendar_view.childCount == 0) true else calendar_view.getChildAt(0).top == 0
    }

}