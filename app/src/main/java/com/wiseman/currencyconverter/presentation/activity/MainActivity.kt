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
import com.wiseman.currencyconverter.presentation.UiEvent
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
                selectBuyingCurrency()
            }
            sellingCurrencyTv.setOnClickListener {
                selectSellingCurrency()
            }

            ratesConversionViewModel.selectedCurrencyDataHolder.collectInActivity { exchangeDetail: CurrencyExchangeData ->
                exchangeRateTv.text = showExchangeRate(
                    sellingCurrencyCode = exchangeDetail.sellingCurrencyCode,
                    exchangeRate = exchangeDetail.exchangeRate,
                    buyingCurrencyCode = exchangeDetail.buyingCurrencyCode
                )
                buyingCurrencyTv.text = exchangeDetail.buyingCurrencyCode
                sellingCurrencyTv.text = exchangeDetail.sellingCurrencyCode
                binding.commissionTv.text = String.format(
                    "EUR %s",
                    exchangeDetail.commission.formatToTwoDecimalString()
                )
                buyingCurrencyEt.editText?.setText(exchangeDetail.amountToBuy.toString())
                totalValueTv.text = String.format(
                    "%s %s",
                    exchangeDetail.sellingCurrencyCode, exchangeDetail.totalAmount
                )
            }

            sellingCurrencyEt.editText?.doAfterTextChanged { text ->
                // implement a calculation of exchange rate for other currencies
                // implement calculated and displaying the values to 2 decimal places
                if (text.toString().isNotBlank()) {
                    val amountToBeExchange =
                        binding.sellingCurrencyEt.editText?.text.toString().toDouble()
                    ratesConversionViewModel.allAccountType.collectInActivity { it ->
                        val accountType =
                            it.find { it.currency == binding.sellingCurrencyTv.text.toString() }
                        accountType?.let {
                            if (amountToBeExchange > it.value) {
                                sellingCurrencyEt.error =
                                    "The inputed buyingCurrencyAmount is greater than you balance"
                            } else if (binding.sellingCurrencyTv.text.toString() == binding.buyingCurrencyTv.text.toString()){
                                sellingCurrencyEt.error =
                                    "you cannot perform currency exchange on the same currency"
                            }
                            else {
                                sellingCurrencyEt.error = null

                            }
                        } ?: kotlin.run {
                            sellingCurrencyEt.error = "You do not have the currency to sell"
                        }
                    }

                    ratesConversionViewModel.updateUiOnEventChange(
                        UiEvent.UpdateAmountToBuy(
                            text.toString().toDouble()
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
                    ratesConversionViewModel.updateUiOnEventChange(
                        UiEvent.UpdateExchangeRate(
                            binding.buyingCurrencyTv.text.toString()
                        )
                    )
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
            ratesConversionViewModel.updateUiOnEventChange(UiEvent.ChangeSellingCurrency(it.first))
        }
    }

    private fun showExchangeRate(
        sellingCurrencyCode: String?,
        exchangeRate: Double,
        buyingCurrencyCode: String
    ): String =
        String.format(
            "1 %s = %s %s",
            sellingCurrencyCode,
            exchangeRate,
            buyingCurrencyCode
        )


    private fun showSuccessExchangeDialog() {
        // work on the total calculation display on the UI
        // check if buyingCurrencyAmount is not equal to zero
        // perform validation
        // fix the bug that shows when a transaction is performed using the total balance
        val amountToSell = binding.sellingCurrencyEt.editText?.text.toString()
        if (amountToSell.isBlank() || amountToSell.toDouble() == 0.00) {
            Toast.makeText(
                this@MainActivity,
                "please input an buyingCurrencyAmount",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val amountToBuy = binding.buyingCurrencyEt.editText?.text.toString().toDouble()
        val sellingCurrency = binding.sellingCurrencyTv.text.toString()
        val buyingCurrencyCode = binding.buyingCurrencyTv.text.toString()
        val commission = ratesConversionViewModel.calculateCommission(amountToSell.toDouble())

        binding.sellingCurrencyTv.text.toString()


        val isValidTraction = ratesConversionViewModel.performValidation(
            sellingCurrency, amountToSell.toDouble()
        )
        if (binding.sellingCurrencyTv.text.toString() == binding.buyingCurrencyTv.text.toString()){
            Toast.makeText(
                this@MainActivity,
                "you cannot perform currency exchange on the same currency",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (isValidTraction) {
            ratesConversionViewModel.updateUiOnEventChange(
                UiEvent.PerformExchange(
                    buyingCurrencyCode,
                    amountToBuy,
                    amountToSell.toDouble()
                )
            )
        }
        else {
            Toast.makeText(
                this@MainActivity,
                "Please check that you have suficient balance to perform the above transaction",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val message =
            String.format(
                "You have converted %s %s to %s %s. Commission Fee: %s %s",
                binding.sellingCurrencyEt.editText?.text.toString(),
                binding.sellingCurrencyTv.text.toString(),
                (amountToSell + commission),
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