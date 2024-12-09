package com.wiseman.currencyconverter.presentation.activity

import android.os.Bundle
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
import com.wiseman.currencyconverter.presentation.UiState
import com.wiseman.currencyconverter.presentation.adapter.AccountTypeAdapter
import com.wiseman.currencyconverter.presentation.sheet.AllExchangeRateBottomSheetFragment
import com.wiseman.currencyconverter.presentation.viewmodel.CurrencyExchangeData
import com.wiseman.currencyconverter.presentation.viewmodel.RatesConversionViewModel
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
                selectSellingCurrency()
            }
            sellingCurrencyTv.setOnClickListener {
                selectBuyingCurrency()
            }

            ratesConversionViewModel.selectedCurrencyDataHolder.collectInActivity { it: CurrencyExchangeData ->
                exchangeRateTv.text = showExchangeRate(
                    sendingCurrency = it.baseCurrency,
                    receivingCurrency = it.receivingCurrency,
                    receivingCurrencyValue = it.receivingCurrencyValue.toString()
                )
                buyingCurrencyTv.text = it.receivingCurrency
                sellingCurrencyTv.text = it.baseCurrency
            }

            sellingCurrencyEt.editText?.doAfterTextChanged { text ->
                if (text.toString().isNotBlank()) {
                    val amountToBeExchange = calculateExchangeRate(text.toString().toDouble())
                    buyingCurrencyEt.editText?.setText(
                        amountToBeExchange.toString()
                    )

                    // calculate the commission on input change
                    commissionTv.text = String.format(
                        "EUR %s",
                        calculateCommission().formatToTwoDecimalString()
                    )

                    // show the total on input change
                    totalValueTv.text = String.format(
                        "%s %s",
                        binding.buyingCurrencyTv.text.toString(),
                        (calculateCommission() + amountToBeExchange).formatToTwoDecimalString()
                    )
                }
            }

            submitBtn.setOnClickListener {
                showSuccessExchangeDialog()
            }

            binding.accountsTypeRv.apply {
                adapter = chooseCurrencyAdapter
                layoutManager =
                    LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            }
        }
    }


    private fun calculateExchangeRate(value: Double): Double {
        val amountToBeExchange = ratesConversionViewModel.calculateExchangeRate(
            amount = value,
            selectedCurrency = binding.buyingCurrencyTv.text.toString()
        )
        return amountToBeExchange
    }

    private fun calculateCommission(): Double {
        val totalValue = binding.sellingCurrencyEt.editText?.text.toString().toDouble()
        return ratesConversionViewModel.calculateCommission(totalValue)
    }


    private fun selectBuyingCurrency() {
        showSupportedExchangeRateBottomSheet {
            resetTextField()
            ratesConversionViewModel.changeSellingCurrency(it.first)
        }
    }

    private fun resetTextField(){
        binding.sellingCurrencyEt.editText?.setText(getString(R.string.empty))
        binding.buyingCurrencyEt.editText?.setText(getString(R.string._0_00))
    }

    private fun handleAccountType() {
        ratesConversionViewModel.allAccountType.collectInActivity { listItem ->
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
                        state.error ?: "SomeThing went wrong",
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
            val showCurrencyExchangeRateSheet =
                AllExchangeRateBottomSheetFragment.newInstance(it)
            showCurrencyExchangeRateSheet
                .show(supportFragmentManager, AllExchangeRateBottomSheetFragment.TAG)
            showCurrencyExchangeRateSheet.setOnItemClickListener { result: Pair<String, Double> ->
                selectedCurrency(result)
            }
        }
    }

    private fun selectSellingCurrency() {
        showSupportedExchangeRateBottomSheet {
            resetTextField()
            ratesConversionViewModel.changeBuyingCurrency(it)
        }
    }

    private fun showExchangeRate(
        sendingCurrency: String?,
        receivingCurrency: String?,
        receivingCurrencyValue: String
    ): String =
        String.format(
            "1 %s = %s %s",
            sendingCurrency,
            receivingCurrencyValue,
            receivingCurrency
        )


    private fun showSuccessExchangeDialog() {
        val buyValue = binding.buyingCurrencyEt.editText?.text.toString()
        val sellingCode = binding.sellingCurrencyTv.text.toString()
        if (buyValue.toDouble() == 0.00) {
            Toast.makeText(this@MainActivity, "please input an amount", Toast.LENGTH_SHORT).show()
            return
        }
        val commission = ratesConversionViewModel.calculateCommission(buyValue.toDouble())
        val totalValue =
            ratesConversionViewModel.calculateExchangeRate(
                buyValue.toDouble(),
                sellingCode
            )

        ratesConversionViewModel.createOrUpdateCurrency(
            binding.buyingCurrencyTv.text.toString(),
            buyValue.toDouble()
        )

        val message =
            String.format(
                "You have converted %s %s to %s %s. Commission Fee: %s %s",
                binding.sellingCurrencyEt.editText?.text.toString(),
                binding.sellingCurrencyTv.text.toString(),
                totalValue,
                binding.buyingCurrencyTv.text.toString(),
                commission,
                binding.sellingCurrencyTv.text.toString()
            )
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.currency_converted))
            .setMessage(message)
            .setPositiveButton(resources.getString(R.string.done)) { dialog, which ->
                resetTextField()
                ratesConversionViewModel.incrementTransactionCounter()
                dialog.dismiss()
            }
            .show()
    }

}