package com.wiseman.currencyconverter.presentation.sheet

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.WindowManager
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wiseman.currencyconverter.R
import com.wiseman.currencyconverter.databinding.FragmentAllCurrencyBottomSheetBinding
import com.wiseman.currencyconverter.domain.model.ExchangeRates
import com.wiseman.currencyconverter.presentation.adapter.SelectExchangeRateAdapter
import com.wiseman.currencyconverter.presentation.viewmodel.RatesConversionViewModel
import com.wiseman.currencyconverter.util.parcelable
import com.wiseman.currencyconverter.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllExchangeRateBottomSheetFragment :
    BottomSheetDialogFragment(R.layout.fragment_all_currency_bottom_sheet) {
    private val binding by viewBinding(FragmentAllCurrencyBottomSheetBinding::bind)
    private lateinit var chooseCurrencyAdapter: SelectExchangeRateAdapter
    private val ratesConversionViewModel:RatesConversionViewModel by activityViewModels()
    private lateinit var exchangeRates: ExchangeRates
    private var onItemClickListener: ((Pair<String, Double>) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val parentLayout = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let {
                val behaviour = BottomSheetBehavior.from(it)
                val layoutParams = it.layoutParams
                layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
                it.layoutParams = layoutParams
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireArguments().parcelable<ExchangeRates>(BUNDLE_KEY)?.let { rate ->
            exchangeRates = rate
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chooseCurrencyAdapter = SelectExchangeRateAdapter { exchangeRate ->
            onItemClickListener?.invoke(exchangeRate)
            dismiss()
        }
        initComponents()
        searchExchangeRate()
    }

    private fun initComponents() {
        with(binding) {
            exchangeRateRv.apply {
                adapter = chooseCurrencyAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
            val initialExchangeRates = exchangeRates.currencyRates
                .filterNot { it.value == null }
                .mapNotNull { (key, value) -> value?.let { key to it } }
            chooseCurrencyAdapter.submitItem(initialExchangeRates)
            backNav.setOnClickListener { dismiss() }
        }
    }

    private fun searchExchangeRate() {
        binding.etSearchBank.doAfterTextChanged { searchText: Editable? ->
            val filteredList = ratesConversionViewModel.searchExchangeRate(searchText.toString())
            chooseCurrencyAdapter.submitItem(filteredList)
        }
    }

    companion object {
        const val TAG = "AllExchangeRateBottomSheetFragment"
        const val BUNDLE_KEY = ".AllExchangeRateBottomSheetFragment.Exchange_Rates"

        fun newInstance(
            exchangeRates: ExchangeRates
        ): AllExchangeRateBottomSheetFragment {
            val sheet = AllExchangeRateBottomSheetFragment()
            val bundle = Bundle().apply {
                putParcelable(BUNDLE_KEY, exchangeRates)
            }

            sheet.arguments = bundle
            return sheet
        }
    }

    fun setOnItemClickListener(listener: (Pair<String, Double>) -> Unit) {
        this.onItemClickListener = listener
    }

}
