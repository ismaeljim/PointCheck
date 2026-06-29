package com.pointcheck.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Componente de mapa basado en Google Maps para mostrar la ubicación de un servicio.
 * 
 * Configura automáticamente la cámara para centrarse en las coordenadas proporcionadas
 * con un nivel de zoom adecuado para visualización urbana (15f).
 *
 * @param latitude Latitud de la ubicación.
 * @param longitude Longitud de la ubicación.
 * @param title Título que se mostrará en el marcador del mapa.
 * @param modifier Modificador para personalizar el tamaño o layout del contenedor.
 */
@Composable
fun PointCheckMapView(
    latitude: Double,
    longitude: Double,
    title: String,
    modifier: Modifier = Modifier
) {
    val location = LatLng(latitude, longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 15f)
    }

    GoogleMap(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp)),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false
        )
    ) {
        Marker(
            state = MarkerState(position = location),
            title = title,
            snippet = "Ubicación del Servicio"
        )
    }
}
