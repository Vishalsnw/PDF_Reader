/*
 * File: SettingsViewModel.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 04:48:48 UTC
 *
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.yourapp.readers.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourapp.readers.domain.models.*
import com.yourapp.readers.domain.repository.ISettingsRepository
import com.yourapp.readers.utils.DispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: ISettingsRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch(dispatchers.io) {
            combine(
                settingsRepository.readerSettings,
                settingsRepository.fontSettings,
                settingsRepository.colorScheme,
                settingsRepository.appSettings
            ) { readerSettings, fontSettings, colorScheme, appSettings ->
                SettingsUiState.Success(
                    readerSettings = readerSettings,
                    fontSettings = fontSettings,
                    colorScheme = colorScheme,
                    appSettings = appSettings
                )
            }.catch { error ->
                _uiState.value = SettingsUiState.Error(error.message ?: "Unknown error")
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateReaderSettings(settings: ReaderSettings) {
        viewModelScope.launch(dispatchers.io) {
            try {
                settingsRepository.updateReaderSettings(settings)
                _events.emit(SettingsEvent.SettingsUpdated)
            } catch (e: Exception) {
                _events.emit(SettingsEvent.Error(e.message ?: "Failed to update reader settings"))
            }
        }
    }

    fun updateFontSettings(settings: FontSettings) {
        viewModelScope.launch(dispatchers.io) {
            try {
                settingsRepository.updateFontSettings(settings)
                _events.emit(SettingsEvent.SettingsUpdated)
            } catch (e: Exception) {
                _events.emit(SettingsEvent.Error(e.message ?: "Failed to update font settings"))
            }
        }
    }

    fun updateColorScheme(scheme: ColorScheme) {
        viewModelScope.launch(dispatchers.io) {
            try {
                settingsRepository.updateColorScheme(scheme)
                _events.emit(SettingsEvent.SettingsUpdated)
            } catch (e: Exception) {
                _events.emit(SettingsEvent.Error(e.message ?: "Failed to update color scheme"))
            }
        }
    }

    fun updateAppSettings(settings: AppSettings) {
        viewModelScope.launch(dispatchers.io) {
            try {
                settingsRepository.updateAppSettings(settings)
                _events.emit(SettingsEvent.SettingsUpdated)
            } catch (e: Exception) {
                _events.emit(SettingsEvent.Error(e.message ?: "Failed to update app settings"))
            }
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch(dispatchers.io) {
            try {
                settingsRepository.resetToDefaults()
                _events.emit(SettingsEvent.SettingsReset)
            } catch (e: Exception) {
                _events.emit(SettingsEvent.Error(e.message ?: "Failed to reset settings"))
            }
        }
    }

    fun exportSettings() {
        viewModelScope.launch(dispatchers.io) {
            try {
                val json = settingsRepository.exportSettings()
                _events.emit(SettingsEvent.SettingsExported(json))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.Error(e.message ?: "Failed to export settings"))
            }
        }
    }

    fun importSettings(json: String) {
        viewModelScope.launch(dispatchers.io) {
            try {
                when (val result = settingsRepository.importSettings(json)) {
                    is Result.Success -> _events.emit(SettingsEvent.SettingsImported)
                    is Result.Error -> _events.emit(SettingsEvent.Error(result.exception.message ?: "Failed to import settings"))
                }
            } catch (e: Exception) {
                _events.emit(SettingsEvent.Error(e.message ?: "Failed to import settings"))
            }
        }
    }
}

sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Success(
        val readerSettings: ReaderSettings,
        val fontSettings: FontSettings,
        val colorScheme: ColorScheme,
        val appSettings: AppSettings
    ) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}

sealed class SettingsEvent {
    object SettingsUpdated : SettingsEvent()
    object SettingsReset : SettingsEvent()
    object SettingsImported : SettingsEvent()
    data class SettingsExported(val json: String) : SettingsEvent()
    data class Error(val message: String) : SettingsEvent()
}
