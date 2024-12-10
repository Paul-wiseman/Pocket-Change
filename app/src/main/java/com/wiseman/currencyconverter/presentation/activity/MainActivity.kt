package com.wiseman.currencyconverter.presentation.activity

import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.wiseman.currencyconverter.R
import com.wiseman.currencyconverter.databinding.ActivityMainBinding
import com.wiseman.currencyconverter.domain.model.ExchangeRates
import com.wiseman.currencyconverter.presentation.UiEvent
import com.wiseman.currencyconverter.presentation.UiState
import com.wiseman.currencyconverter.presentation.adapter.AccountTypeAdapter
import com.wiseman.currencyconverter.presentation.sheet.AllExchangeRateBottomSheetFragment
import com.wiseman.currencyconverter.presentation.viewmodel.CurrencyExchangeData
import com.wiseman.currencyconverter.presentation.viewmodel.RatesConversionViewModel
import com.wiseman.currencyconverter.util.ValidationResult
import com.wiseman.currencyconverter.util.collectInActivity
import com.wiseman.currencyconverter.util.formatToTwoDecimalString
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val ratesConversionViewModel: RatesConversionViewModel by viewModels()
    private var exchangeRate: ExchangeRates? = null
    private val chooseCurrencyAdapter by lazy { AccountTypeAdapter() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initialComponents()
        handleExchangeResponse()
        handleAccountType()
    }

    private fun initialComponents() {
        with(binding) {

            buyingCurrencyTv.setOnClickListener {
                selectBuyingCurrency()
            }
            sellingCurrencyTv.setOnClickListener {
                selectSellingCurrency()
            }

            ratesConversionViewModel.selectedCurrencyData.collectInActivity { exchangeDetail: CurrencyExchangeData ->
                buyingCurrencyTv.text = exchangeDetail.buyingCurrencyCode
                sellingCurrencyTv.text = exchangeDetail.sellingCurrencyCode
                binding.commissionTv.text = String.format(
                    getString(R.string.s_s),
                    exchangeDetail.sellingCurrencyCode,
                    exchangeDetail.commission.formatToTwoDecimalString()
                )
                buyingCurrencyEt.editText?.setText(exchangeDetail.amountToBuy.formatToTwoDecimalString())
                totalValueTv.text = String.format(
                    getString(R.string.s_s),
                    exchangeDetail.sellingCurrencyCode,
                    exchangeDetail.totalAmount.toString()
                )
            }

            sellingCurrencyEt.editText?.doAfterTextChanged { text ->
                if (!text.isNullOrBlank()) {
                    handInputChange(text)
                }
            }

            submitBtn.setOnClickListener {
                val sellingCurrency = binding.sellingCurrencyEt.editText?.text.toString()
                if (sellingCurrency.isNotBlank()) {
                    performCurrencyExchange()
                }
            }

            binding.accountsTypeRv.apply {
                adapter = chooseCurrencyAdapter
                layoutManager =
                    LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            }
        }
    }

    private fun handInputChange(text: Editable) {
        val amountToBeExchange =
            binding.sellingCurrencyEt.editText?.text.toString().toDouble()
        val sellingCurrencyCode = binding.sellingCurrencyTv.text.toString()
        val buyingCurrencyCode = binding.buyingCurrencyTv.text.toString()
        when (val validationResult = ratesConversionViewModel.performValidation(
            sellingCurrencyCode,
            amountToBeExchange,
            buyingCurrencyCode
        )) {
            is ValidationResult.Error -> binding.sellingCurrencyEt.error =
                validationResult.errorMessage

            ValidationResult.Success -> {
                binding.sellingCurrencyEt.error = null
                ratesConversionViewModel.updateUiOnEventChange(
                    UiEvent.UpdateAmountToBuy(
                        binding.sellingCurrencyTv.text.toString(),
                        binding.buyingCurrencyTv.text.toString(),
                        binding.sellingCurrencyEt.editText?.text.toString().toDouble()
                    )
                )
                ratesConversionViewModel.updateUiOnEventChange(
                    UiEvent.CalculateCommission(
                        text.toString().toDouble()
                    )
                )
                ratesConversionViewModel.updateUiOnEventChange(
                    UiEvent.CalculateTotalValue(
                        text.toString().toDouble()
                    )
                )

            }
        }
    }

    private fun selectBuyingCurrency() {
        showSupportedExchangeRateBottomSheet {
            resetTextField()
            ratesConversionViewModel.updateUiOnEventChange(
                UiEvent.ChangeBuyingCurrency(
                    it.first,
                    it.second
                )
            )
        }
    }

    private fun resetTextField() {
        binding.sellingCurrencyEt.editText?.setText(getString(R.string.empty))
        binding.sellingCurrencyEt.error = null
        binding.buyingCurrencyEt.editText?.setText(getString(R.string._0_00))
        binding.totalValueTv.text = String.format(
            getString(R.string.s_s),
            binding.sellingCurrencyTv.text.toString(),
            getString(R.string._0_00)
        )
    }

    private fun handleAccountType() {
        ratesConversionViewModel.currencyTypes.collectInActivity { listItem ->
            chooseCurrencyAdapter.submitItem(listItem)
        }
    }

    private fun handleExchangeResponse() {
        ratesConversionViewModel.currentExchangeRateState.collectInActivity { state ->
            when (state.uiState) {
                UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(
                        binding.root,
                        state.error ?: getString(R.string.something_went_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE

                }

                UiState.Success -> {
                    exchangeRate = state.data
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun showSupportedExchangeRateBottomSheet(selectedCurrency: (Pair<String, Double>) -> Unit) {
        exchangeRate?.let {
            val showCurrencyExchangeRateSheet = AllExchangeRateBottomSheetFragment.newInstance(it)
            showCurrencyExchangeRateSheet.show(
                supportFragmentManager,
                AllExchangeRateBottomSheetFragment.TAG
            )
            showCurrencyExchangeRateSheet.setOnItemClickListener { result: Pair<String, Double> ->
                selectedCurrency(result)
            }
        }
    }

    private fun selectSellingCurrency() {
        showSupportedExchangeRateBottomSheet {
            resetTextField()
            ratesConversionViewModel.updateUiOnEventChange(UiEvent.ChangeSellingCurrency(it.first))
        }
    }


    private fun performCurrencyExchange() {
        // fix the bug that shows when a transaction is performed using the total balance
        val amountToSell = binding.sellingCurrencyEt.editText?.text.toString()
        val sellingCurrency = binding.sellingCurrencyTv.text.toString()
        val buyingCurrency = binding.buyingCurrencyTv.text.toString()

        val validationResult =
            ratesConversionViewModel.performValidation(
                sellingCurrency,
                amountToSell.toDouble(),
                buyingCurrency
            )
        when (validationResult) {
            is ValidationResult.Error -> Toast.makeText(
                this,
                validationResult.errorMessage,
                Toast.LENGTH_SHORT
            ).show()

            is ValidationResult.Success -> {
                val buyingCurrencyCode = binding.buyingCurrencyTv.text.toString()
                ratesConversionViewModel.updateUiOnEventChange(
                    UiEvent.PerformExchange(
                        sellingCurrency,
                        buyingCurrencyCode,
                        amountToSell.toDouble()
                    )
                )
                showSuccessDialog(amountToSell)
            }
        }
    }

    private fun showSuccessDialog(amountToSell: String) {
        val commission = ratesConversionViewModel.calculateCommission(amountToSell.toDouble())
        val message =
            String.format(
                "You have converted %s %s to %s %s. Commission Fee: %s %s",
                binding.sellingCurrencyEt.editText?.text.toString(),
                binding.sellingCurrencyTv.text.toString(),
                binding.buyingCurrencyEt.editText?.text.toString(),
                binding.buyingCurrencyTv.text.toString(),
                commission,
                binding.sellingCurrencyTv.text.toString()
            )
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.currency_converted))
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.done)) { dialog, which ->
                resetTextField()
                dialog.dismiss()
            }
            .show()
    }

}