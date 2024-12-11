package com.wiseman.currencyconverter.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.wiseman.currencyconverter.databinding.CurrenciesItemLayoutBinding
import com.wiseman.currencyconverter.util.ExchangeRateDiffUtil


class SelectExchangeRateAdapter(private val onItemClickListener: (selectedCurrency: Pair<String, Double>) -> Unit) :
    RecyclerView.Adapter<SelectExchangeRateAdapter.DataViewHolder>() {
    private var exchangeRates = listOf<Pair<String, Double>>()

    inner class DataViewHolder(val binding: CurrenciesItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(rate: Pair<String, Double>) {
            with(binding) {
                tvCurrencyName.text = rate.first
                exchangeRateTv.text = rate.second.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val binding =
            CurrenciesItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DataViewHolder(binding)
    }

    override fun getItemCount() = exchangeRates.size


    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        val currentBank = exchangeRates[position]
        holder.bind(currentBank)
        holder.binding.root.setOnClickListener {
            onItemClickListener(currentBank)
        }
    }

    fun submitItem(currencies: List<Pair<String, Double>>) {
        val accountTypeDiffUtil = ExchangeRateDiffUtil(exchangeRates, currencies)
        val diffUtilResult = DiffUtil.calculateDiff(accountTypeDiffUtil)
        exchangeRates = currencies
        diffUtilResult.dispatchUpdatesTo(this)
    }
}
