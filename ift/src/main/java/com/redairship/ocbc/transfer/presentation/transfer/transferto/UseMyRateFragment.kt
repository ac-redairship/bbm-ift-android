package com.redairship.ocbc.transfer.presentation.transfer.transferto

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.ocbc.transfer.R
import com.ocbc.transfer.databinding.FragmentUseMyRateBinding
import com.redairship.ocbc.bb.components.extensions.popBackstack
import com.redairship.ocbc.transfer.presentation.local.LocalTransferViewModel
import kotlinx.android.synthetic.main.fragment_use_my_rate.cb_item_1
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class UseMyRateFragment : Fragment() {

    companion object {
        const val BUNDLE_KEY_IS_SENDER: String = "BUNDLE_KEY_IS_SENDER"

        @JvmStatic
        fun newInstance() = UseMyRateFragment()
    }

    private lateinit var binding: FragmentUseMyRateBinding

    val sharedViewModel: LocalTransferViewModel by sharedViewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentUseMyRateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupViewEvents()
        observeChanges()
    }

    private fun observeChanges() {

    }

    private fun setupViewEvents() {
        val unchecked = ContextCompat.getDrawable(requireContext(), R.drawable.ic_radio_unchecked)
        val checked = ContextCompat.getDrawable(requireContext(), R.drawable.ic_radio_checked)

        binding.ivStartIcon.setOnClickListener {
            popBackstack()
        }

        binding.vItem1.setOnClickListener {
            binding.cbItem1.setImageDrawable(checked)
            binding.cbItem2.setImageDrawable(unchecked)
        }

        binding.vItem2.setOnClickListener {
            binding.cbItem1.setImageDrawable(unchecked)
            binding.cbItem2.setImageDrawable(checked)
        }

        binding.btnCta.setOnClickListener {
            sharedViewModel.getCounterFX(false, "USD", true)
            popBackstack()
        }
    }

    private fun setupViews() {

    }

}
