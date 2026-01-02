package com.example.electionapp.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.maps.android.compose.*

@Composable
fun MapPickerScreen(
    onLocationPicked: (Double, Double) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState()

    GoogleMap(
        modifier = Modifier,
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            onLocationPicked(latLng.latitude, latLng.longitude)
        }
    )
}
