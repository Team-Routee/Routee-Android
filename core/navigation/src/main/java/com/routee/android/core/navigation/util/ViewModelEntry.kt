package com.routee.android.core.navigation.util

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

/**
 * 화면 키로 ViewModel을 만드는 assisted 팩토리
 */
interface NavKeyViewModelFactory<K : NavKey, VM : ViewModel> {
    fun create(key: K): VM
}

/**
 * 화면 키를 ViewModel에 전달하는 entry
 *
 * ```kotlin
 * @HiltViewModel(assistedFactory = ExampleViewModel.Factory::class)
 * class ExampleViewModel @AssistedInject constructor(
 *     @Assisted val key: ExampleNavKey,
 * ) : ViewModel() {
 *     @AssistedFactory
 *     interface Factory : NavKeyViewModelFactory<ExampleNavKey, ExampleViewModel>
 * }
 *
 * viewModelEntry<ExampleNavKey, ExampleViewModel, ExampleViewModel.Factory> { key, viewModel -> }
 * ```
 */
inline fun <
    reified K : NavKey,
    reified VM : ViewModel,
    reified F : NavKeyViewModelFactory<K, VM>,
    > EntryProviderScope<NavKey>.viewModelEntry(
    metadata: Map<String, Any> = emptyMap(),
    crossinline content: @Composable (key: K, viewModel: VM) -> Unit,
) {
    entry<K>(metadata = metadata) { key ->
        val viewModel = hiltViewModel<VM, F>(creationCallback = { factory -> factory.create(key) })
        content(key, viewModel)
    }
}
