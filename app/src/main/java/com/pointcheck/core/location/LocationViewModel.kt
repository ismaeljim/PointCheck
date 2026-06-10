package com.pointcheck.core.location

import android.annotation.SuppressLint
import android.app.Application
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Representa el estado de la interfaz de usuario para la geolocalización.
 *
 * @property lastKnownLat Última latitud conocida del dispositivo.
 * @property lastKnownLng Última longitud conocida del dispositivo.
 * @property addressSuggestions Lista de direcciones sugeridas obtenidas por geocodificación inversa.
 * @property isLocating Indica si se está realizando una operación de obtención de ubicación.
 * @property error Mensaje de error en caso de fallo al localizar o geocodificar.
 */
data class LocationUiState(
    val lastKnownLat: Double? = null,
    val lastKnownLng: Double? = null,
    val addressSuggestions: List<Address> = emptyList(),
    val isLocating: Boolean = false,
    val error: String? = null
)

/**
 * Motor de Geolocalización y Geocodificación.
 * Utiliza Google Play Services (FusedLocation) para precisión y eficiencia energética.
 * Se implementa compatibilidad con API 33+ (Tiramisu) mediante GeocodeListener asíncrono.
 *
 * @param application Contexto de la aplicación para inicializar servicios de ubicación.
 */
class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val geocoder = Geocoder(application, Locale.getDefault())

    private val _state = MutableStateFlow(LocationUiState())
    val state: StateFlow<LocationUiState> = _state

    /**
     * Solicita la ubicación actual del dispositivo con alta precisión.
     *
     * @param onSuccess Callback que se ejecuta al obtener las coordenadas exitosamente.
     */
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onSuccess: (Double, Double) -> Unit) {
        _state.update { it.copy(isLocating = true, error = null) }
        viewModelScope.launch {
            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            _state.update { it.copy(
                                lastKnownLat = location.latitude,
                                lastKnownLng = location.longitude,
                                isLocating = false
                            ) }
                            onSuccess(location.latitude, location.longitude)
                        } else {
                            _state.update { it.copy(isLocating = false, error = "No se pudo obtener la ubicación") }
                        }
                    }
                    .addOnFailureListener { e ->
                        _state.update { it.copy(isLocating = false, error = e.message) }
                    }
            } catch (e: Exception) {
                _state.update { it.copy(isLocating = false, error = e.message) }
            }
        }
    }

    /**
     * Obtiene sugerencias de direcciones basadas en una cadena de búsqueda (Geocoding).
     * Requiere al menos 5 caracteres para iniciar la búsqueda.
     *
     * @param query Dirección parcial o nombre de lugar a buscar.
     */
    fun getAddressSuggestions(query: String) {
        if (query.length < 5) {
            _state.update { it.copy(addressSuggestions = emptyList()) }
            return
        }
        
        viewModelScope.launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocationName(query, 5, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            _state.update { it.copy(addressSuggestions = addresses) }
                        }
                        override fun onError(errorMessage: String?) {
                            super.onError(errorMessage)
                        }
                    })
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(query, 5)
                    _state.update { it.copy(addressSuggestions = addresses ?: emptyList()) }
                }
            } catch (e: Exception) {
                // Silently fail for suggestions
            }
        }
    }

    /** Limpia la lista de sugerencias de direcciones actuales. */
    fun clearSuggestions() {
        _state.update { it.copy(addressSuggestions = emptyList()) }
    }

    /**
     * Convierte una dirección de texto en coordenadas geográficas (Lat/Lng).
     *
     * @param address Dirección completa en texto.
     * @return Par de Latitud y Longitud, o null si no se pudo geocodificar.
     */
    suspend fun getLatLngFromAddress(address: String): Pair<Double, Double>? = suspendCancellableCoroutine { continuation ->
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocationName(address, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        val loc = addresses.firstOrNull()?.let { it.latitude to it.longitude }
                        continuation.resume(loc)
                    }
                    override fun onError(errorMessage: String?) {
                        continuation.resume(null)
                    }
                })
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(address, 1)
                val loc = addresses?.firstOrNull()?.let { it.latitude to it.longitude }
                continuation.resume(loc)
            }
        } catch (e: Exception) {
            continuation.resume(null)
        }
    }
}
