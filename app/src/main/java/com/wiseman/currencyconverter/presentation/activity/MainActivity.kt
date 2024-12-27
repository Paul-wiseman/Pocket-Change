package com.wiseman.currencyconverter.presentation.activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wiseman.currencyconverter.R
import com.wiseman.currencyconverter.databinding.ActivityMainBinding
import com.wiseman.currencyconverter.domain.model.ExchangeRates
import com.wiseman.currencyconverter.presentation.adapter.CurrencyTypeAdapter
import com.wiseman.currencyconverter.presentation.intent.ExchangeRateIntent
import com.wiseman.currencyconverter.presentation.sheet.AllExchangeRateBottomSheetFragment
import com.wiseman.currencyconverter.presentation.state.CurrencyExchangeData
import com.wiseman.currencyconverter.presentation.state.UiState
import com.wiseman.currencyconverter.presentation.viewmodel.RatesConversionViewModel
import com.wiseman.currencyconverter.util.AmountInputFilter
import com.wiseman.currencyconverter.util.ValidationResult
import com.wiseman.currencyconverter.util.collectInActivity
import com.wiseman.currencyconverter.util.formatCurrencyValue
import com.wiseman.currencyconverter.util.formatToTwoDecimalString
import com.wiseman.currencyconverter.util.showErrorDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val ratesConversionViewModel: RatesConversionViewModel by viewModels()
    private var exchangeRate: ExchangeRates? = null
    private val chooseCurrencyAdapter by lazy { CurrencyTypeAdapter() }


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
        setupClickListeners()
        setupDataObserver()
        setupAccountTypeRecyclerView()
        setupAmountInputListener()
    }

    private fun setupAmountInputListener() {
        binding.sellingCurrencyEt.editText?.filters = arrayOf(AmountInputFilter())
        binding.sellingCurrencyEt.editText?.doAfterTextChanged { text ->
            if (!text.isNullOrBlank()) {
                val amountToBeExchange =
                    binding.sellingCurrencyEt.editText?.text.toString().toDouble()
                val sellingCurrencyCode = binding.sellingCurrencyTv.text.toString()
                val buyingCurrencyCode = binding.buyingCurrencyTv.text.toString()

                performValidationOnInputChange(
                    sellingCurrencyCode,
                    amountToBeExchange,
                    buyingCurrencyCode
                )
            }
        }
    }

    private fun performValidationOnInputChange(
        sellingCurrencyCode: String,
        amountToBeExchange: Double,
        buyingCurrencyCode: String,
    ) {
        when (val validationResult = ratesConversionViewModel.performValidation(
            sellingCurrencyCode,
            amountToBeExchange,
            buyingCurrencyCode
        )) {
            is ValidationResult.Error -> binding.sellingCurrencyEt.error =
                validationResult.errorMessage

            ValidationResult.Success -> {
                binding.sellingCurrencyEt.error = null
                ratesConversionViewModel.processIntent(
                    ExchangeRateIntent.UpdateAmountToBuy(
                        binding.sellingCurrencyTv.text.toString(),
                        binding.buyingCurrencyTv.text.toString(),
                        binding.sellingCurrencyEt.editText?.text.toString().toDouble()
                    )
                )
                ratesConversionViewModel.processIntent(
                    ExchangeRateIntent.CalculateCommission(
                        amountToBeExchange
                    )
                )
                ratesConversionViewModel.processIntent(
                    ExchangeRateIntent.CalculateTotalValue(
                        amountToBeExchange
                    )
                )

            }
        }
    }

    private fun setupAccountTypeRecyclerView() {
        binding.accountsTypeRv.apply {
            adapter = chooseCurrencyAdapter
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupDataObserver() {
        with(binding) {
            ratesConversionViewModel.currentExchangeData.collectInActivity { exchangeDetail: CurrencyExchangeData ->
                buyingCurrencyTv.text = exchangeDetail.buyingCurrency.code
                sellingCurrencyTv.text = exchangeDetail.sellingCurrency.code
                commissionTv.text = formatCurrencyValue(
                    exchangeDetail.sellingCurrency.code,
                    exchangeDetail.commission
                )
                buyingCurrencyEt.editText?.setText(exchangeDetail.buyingCurrency.value.formatToTwoDecimalString())
                totalValueTv.text = formatCurrencyValue(
                    exchangeDetail.sellingCurrency.code,
                    exchangeDetail.totalAmount
                )
            }
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            buyingCurrencyTv.setOnClickListener {
                showSupportedExchangeRateBottomSheet {
                    resetTextField()
                    ratesConversionViewModel.processIntent(
                        ExchangeRateIntent.ChangeBuyingCurrency(
                            it.first
                        )
                    )
                }
            }
            sellingCurrencyTv.setOnClickListener {
                showSupportedExchangeRateBottomSheet {
                    resetTextField()
                    ratesConversionViewModel.processIntent(
                        ExchangeRateIntent.ChangeSellingCurrency(
                            it.first
                        )
                    )
                }
            }
            submitBtn.setOnClickListener {
                val sellingCurrency = binding.sellingCurrencyEt.editText?.text.toString()
                if (sellingCurrency.isNotBlank()) {
                    performCurrencyExchange()
                }
            }
        }
    }

    private fun resetTextField() {
        with(binding) {
            sellingCurrencyEt.editText?.setText(getString(R.string.empty))
            sellingCurrencyEt.error = null
            buyingCurrencyEt.editText?.setText(getString(R.string._0_00))
            totalValueTv.text = formatCurrencyValue(
                sellingCurrencyTv.text.toString(),
                0.00
            )
            commissionTv.text = formatCurrencyValue(
                sellingCurrencyTv.text.toString(),
                0.00
            )
        }
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
                    showErrorDialog(
                        this,
                        getString(R.string.unable_to_load_exchange_rates),
                        state.error ?: getString(R.string.something_went_wrong)
                    )
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

    private fun showSupportedExchangeRateBottomSheet(selectedCurrency: (Pair<String, Double>) -> Unit) =
        exchangeRate?.let { values ->
            AllExchangeRateBottomSheetFragment.newInstance(
                values
            ).apply {
                setOnItemClickListener { result: Pair<String, Double> ->
                    selectedCurrency(result)
                }
                show(
                    supportFragmentManager,
                    AllExchangeRateBottomSheetFragment.TAG
                )
            }
        } ?: kotlin.run {
            showErrorDialog(
                this,
                getString(R.string.something_went_wrong),
                getString(R.string.exchange_rates_not_available)
            )
        }


    private fun performCurrencyExchange() {
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
            is ValidationResult.Error -> showErrorDialog(
                this,
                getString(R.string.oops),
                validationResult.errorMessage
            )

            is ValidationResult.Success -> {
                val buyingCurrencyCode = binding.buyingCurrencyTv.text.toString()
                ratesConversionViewModel.processIntent(
                    ExchangeRateIntent.PerformExchange(
                        sellingCurrency,
                        buyingCurrencyCode,
                        amountToSell.toDouble()
                    )
                )
                showSuccessDialog()
            }
        }
    }

    private fun showSuccessDialog() {
        val commission = binding.commissionTv.text.toString()
        val message =
            String.format(
                getString(R.string.you_have_converted_s_s_to_s_s_commission_fee_s_s),
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
