package zip.sora.ulearntec.ui.screen.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import zip.sora.ulearntec.R
import zip.sora.ulearntec.domain.ClassRepository
import zip.sora.ulearntec.domain.TermRepository
import zip.sora.ulearntec.domain.UserRepository
import zip.sora.ulearntec.domain.isError
import zip.sora.ulearntec.domain.model.Class
import zip.sora.ulearntec.domain.model.Term

sealed interface TermUiState {
    val currentTerm: Term?
    val terms: List<Term>
    val classes: List<Class>

    data class Loading(
        override val terms: List<Term>,
        override val classes: List<Class>,
        override val currentTerm: Term?,
    ) : TermUiState

    data class Error(
        val message: (Context) -> String,
        override val currentTerm: Term?,
        override val terms: List<Term>,
        override val classes: List<Class>,
    ) : TermUiState

    data class Success(
        override val currentTerm: Term,
        override val terms: List<Term>,
        override val classes: List<Class>
    ) : TermUiState

    data object RequireLogin : TermUiState {
        override val currentTerm: Term? = null
        override val classes: List<Class> = emptyList()
        override val terms: List<Term> = emptyList()
    }
}

class TermViewModel(
    private val userRepository: UserRepository,
    private val termRepository: TermRepository,
    private val classRepository: ClassRepository
) : ViewModel() {
    private val _uiState =
        MutableStateFlow<TermUiState>(TermUiState.Loading(listOf(), listOf(), null))
    val uiState = _uiState.asStateFlow()

    private lateinit var currentTerm: Term

    fun refresh() {
        _uiState.update { TermUiState.Loading(it.terms, it.classes, it.currentTerm) }
        viewModelScope.launch {
            val terms = termRepository.refresh()
            if (terms.isError()) {
                _uiState.update {
                    TermUiState.Error(terms.error, it.currentTerm, it.terms, it.classes)
                }
                return@launch
            }

            currentTerm =
                terms.data.firstOrNull { it.year == currentTerm.year && it.num == currentTerm.num }
                    ?: terms.data.last()

            val classes = classRepository.refresh(currentTerm)
            if (classes.isError()) {
                _uiState.update {
                    TermUiState.Error(classes.error, it.currentTerm, it.terms, it.classes)
                }
                return@launch
            }

            _uiState.update {
                if (classes.data.isEmpty()) TermUiState.Error(
                    { ctx -> ctx.getString(R.string.no_classes) },
                    currentTerm,
                    it.terms,
                    classes.data
                )
                else TermUiState.Success(currentTerm, terms.data, classes.data)
            }
        }
    }

    fun selectTerm(term: Term) {
        currentTerm = term
        _uiState.update {
            TermUiState.Loading(
                terms = it.terms,
                classes = listOf(),
                currentTerm = term
            )
        }

        viewModelScope.launch {
            val classes = classRepository.getTermClasses(term)
            if (classes.isError()) {
                _uiState.update {
                    TermUiState.Error(classes.error, it.currentTerm, it.terms, it.classes)
                }
                return@launch
            }

            _uiState.update {
                if (classes.data.isEmpty()) TermUiState.Error(
                    { ctx -> ctx.getString(R.string.no_classes) },
                    currentTerm,
                    it.terms,
                    classes.data
                )
                else TermUiState.Success(currentTerm, it.terms, classes.data)
            }
        }
    }

    init {
        viewModelScope.launch {
            if (!userRepository.isLoggedIn()) {
                _uiState.update { TermUiState.RequireLogin }
                return@launch
            }

            val terms = termRepository.getAllTerms()
            if (terms.isError()) {
                _uiState.update {
                    TermUiState.Error(terms.error, it.currentTerm, it.terms, it.classes)
                }
                return@launch
            }

            currentTerm = terms.data.last()
            val classes = classRepository.getTermClasses(currentTerm)
            if (classes.isError()) {
                _uiState.update {
                    TermUiState.Error(classes.error, currentTerm, terms.data, it.classes)
                }
                return@launch
            }

            _uiState.update {
                if (classes.data.isEmpty()) TermUiState.Error(
                    { ctx -> ctx.getString(R.string.no_classes) },
                    currentTerm,
                    terms.data,
                    classes.data
                )
                else TermUiState.Success(currentTerm, terms.data, classes.data)
            }
        }
    }
}