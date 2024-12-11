package com.wiseman.currencyconverter.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.wiseman.currencyconverter.databinding.AccountTypeItemLayoutBinding
import com.wiseman.currencyconverter.domain.model.CurrencyType
import com.wiseman.currencyconverter.util.ExchangeRateDiffUtil


class CurrencyTypeAdapter:
    RecyclerView.Adapter<CurrencyTypeAdapter.DataViewHolder>() {
    private var exchangeRates = listOf<CurrencyType>()

    inner class DataViewHolder(val binding: AccountTypeItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currencyType: CurrencyType) {
            with(binding) {
                tvCurrencyName.text = currencyType.currency
                exchangeRateTv.text = currencyType.value.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val binding =
            AccountTypeItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DataViewHolder(binding)
    }

    override fun getItemCount() = exchangeRates.size


    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        val currentBank = exchangeRates[position]
        holder.bind(currentBank)
    }

    fun submitItem(currencies: List<CurrencyType>) {
        val accountTypeDiffUtil = ExchangeRateDiffUtil(exchangeRates, currencies)
        val diffUtilResult = DiffUtil.calculateDiff(accountTypeDiffUtil)
        exchangeRates = currencies
        diffUtilResult.dispatchUpdatesTo(this)
    }
}
