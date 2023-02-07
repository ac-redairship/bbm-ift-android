package com.redairship.ocbc.transfer.presentation.common

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ocbc.transfer.databinding.FragmentUseMyRateBinding

class UseMyRateFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = UseMyRateFragment()
    }

    private lateinit var binding: FragmentUseMyRateBinding

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
    }

}
