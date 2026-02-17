package com.jorge.mysound.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jorge.mysound.data.repository.MusicRepository
import com.jorge.mysound.data.remote.SongResponse
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

/**
 * SearchViewModel: Gestiona la lógica de búsqueda de canciones de forma reactiva.
 * Implementa técnicas de optimización como Debouncing y filtrado para mejorar
 * la eficiencia de las peticiones a la API y la experiencia de usuario (UX).
 */
class SearchViewModel(private val repository: MusicRepository) : ViewModel() {

    // Estado del texto ingresado por el usuario en la barra de búsqueda
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    // Estado que indica si hay una operación de búsqueda activa en el servidor
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    /**
     * searchResults: Flujo reactivo que transforma la entrada del usuario en resultados.
     * * Operadores utilizados:
     * - debounce: Espera 500ms tras la última pulsación para evitar peticiones excesivas.
     * - filter: Solo permite búsquedas con 2 o más caracteres para optimizar el tráfico.
     * - distinctUntilChanged: Evita repetir la búsqueda si el texto no ha variado realmente.
     * - flatMapLatest: Cancela la búsqueda anterior si el usuario introduce un nuevo término.
     */
    @OptIn(FlowPreview::class)
    val searchResults: StateFlow<List<SongResponse>> = _query
        .debounce(500)
        .filter { it.length >= 2 }
        .distinctUntilChanged()
        .flatMapLatest { queryText ->
            flow {
                _isSearching.value = true
                val result = repository.searchSongs(queryText)

                // Emitimos los resultados obtenidos o una lista vacía en caso de error
                emit(result.getOrDefault(emptyList()))
                _isSearching.value = false
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Mantiene el estado vivo 5s tras desuscribirse
            initialValue = emptyList()
        )

    /**
     * Actualiza el valor de la consulta de búsqueda.
     * Este cambio dispara automáticamente la cadena de transformación de [searchResults].
     * * @param newQuery El nuevo texto introducido por el usuario.
     */
    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }
}