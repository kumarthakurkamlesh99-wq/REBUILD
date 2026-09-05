package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.XpTransactionEntity
import com.example.data.model.XpSummaryData
import com.example.data.repository.RebuildRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class XpLedgerUiState(
    val transactions: List<XpTransactionEntity> = emptyList(),
    val filteredTransactions: List<XpTransactionEntity> = emptyList(),
    val summary: XpSummaryData = XpSummaryData(),
    val currentXpBalance: Int = 0,
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

class XpLedgerViewModel(
    private val repository: RebuildRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(XpLedgerUiState())
    val uiState: StateFlow<XpLedgerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialXpTransactionsIfEmpty()
            refreshSummary()
        }

        viewModelScope.launch {
            repository.getAllXpTransactions().collectLatest { txList ->
                _uiState.update { current ->
                    val filtered = applyFilterAndSearch(txList, current.selectedCategory, current.searchQuery)
                    current.copy(
                        transactions = txList,
                        filteredTransactions = filtered,
                        isLoading = false
                    )
                }
                refreshSummary()
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { current ->
            val filtered = applyFilterAndSearch(current.transactions, category, current.searchQuery)
            current.copy(
                selectedCategory = category,
                filteredTransactions = filtered
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { current ->
            val filtered = applyFilterAndSearch(current.transactions, current.selectedCategory, query)
            current.copy(
                searchQuery = query,
                filteredTransactions = filtered
            )
        }
    }

    fun logQuickXp(title: String, category: String, xp: Int) {
        viewModelScope.launch {
            repository.addXp(xp, title, category)
            refreshSummary()
        }
    }

    private suspend fun refreshSummary() {
        val summary = repository.getXpSummary()
        val balance = repository.getCurrentXpBalance()
        _uiState.update { it.copy(summary = summary, currentXpBalance = balance) }
    }

    private fun applyFilterAndSearch(
        items: List<XpTransactionEntity>,
        category: String,
        query: String
    ): List<XpTransactionEntity> {
        val catFiltered = if (category.equals("All", ignoreCase = true)) {
            items
        } else {
            items.filter { it.category.equals(category, ignoreCase = true) }
        }

        if (query.isBlank()) return catFiltered

        val q = query.trim().lowercase()
        return catFiltered.filter {
            it.title.lowercase().contains(q) ||
            it.category.lowercase().contains(q)
        }
    }
}

class XpLedgerViewModelFactory(
    private val repository: RebuildRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(XpLedgerViewModel::class.java)) {
            return XpLedgerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
