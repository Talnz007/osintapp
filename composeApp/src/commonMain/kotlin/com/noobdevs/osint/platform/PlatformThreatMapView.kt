package com.noobdevs.osint.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.noobdevs.osint.data.models.ThreatMapMarker

@Composable
expect fun PlatformThreatMapView(
    markers: List<ThreatMapMarker>,
    onMarkerClicked: (Long) -> Unit,
    modifier: Modifier = Modifier,
    centerTrigger: Int = 0
)
