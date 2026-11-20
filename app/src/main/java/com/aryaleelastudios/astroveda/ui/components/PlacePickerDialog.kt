package com.aryaleelastudios.astroveda.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaleelastudios.astroveda.R
import com.aryaleelastudios.astroveda.ui.theme.CreamBackground
import com.aryaleelastudios.astroveda.ui.theme.CreamBorder
import com.aryaleelastudios.astroveda.ui.theme.DeepIndigo
import com.aryaleelastudios.astroveda.ui.theme.SaffronAccent
import com.aryaleelastudios.astroveda.ui.theme.SaffronDark
import com.aryaleelastudios.astroveda.ui.theme.TextMuted
import com.aryaleelastudios.astroveda.ui.theme.astroPrimaryButtonColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

data class PhotonPlace(
    val id: String,
    val name: String,
    val state: String,
    val country: String,
    val city: String,
    val latitude: Double,
    val longitude: Double
) {
    val subtitle: String
        get() = listOf(city, state, country)
            .filter { it.isNotBlank() && !it.equals(name, ignoreCase = true) }
            .distinct()
            .joinToString(", ")

    val displayName: String
        get() = if (subtitle.isNotBlank()) "$name, $subtitle" else name
}

data class CityLocation(
    val name: String,
    val state: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String,
    val id: String = "city_${name}_${state}"
) {
    val displayLabel: String get() = "$name, $state"
}

const val DEFAULT_PLACE_NAME = "New Delhi, Delhi"
const val DEFAULT_PLACE_LAT = 28.6139
const val DEFAULT_PLACE_LON = 77.2090
const val DEFAULT_PLACE_TZ = "Asia/Kolkata"

// Bundled 54 major Indian and global cities so birth place always works offline
val MAJOR_CITIES = listOf(
    CityLocation("New Delhi", "Delhi", 28.6139, 77.2090, "Asia/Kolkata"),
    CityLocation("Mumbai", "Maharashtra", 19.0760, 72.8777, "Asia/Kolkata"),
    CityLocation("Bengaluru", "Karnataka", 12.9716, 77.5946, "Asia/Kolkata"),
    CityLocation("Kolkata", "West Bengal", 22.5726, 88.3639, "Asia/Kolkata"),
    CityLocation("Chennai", "Tamil Nadu", 13.0827, 80.2707, "Asia/Kolkata"),
    CityLocation("Hyderabad", "Telangana", 17.3850, 78.4867, "Asia/Kolkata"),
    CityLocation("Pune", "Maharashtra", 18.5204, 73.8567, "Asia/Kolkata"),
    CityLocation("Ahmedabad", "Gujarat", 23.0225, 72.5714, "Asia/Kolkata"),
    CityLocation("Surat", "Gujarat", 21.1702, 72.8311, "Asia/Kolkata"),
    CityLocation("Vadodara", "Gujarat", 22.3072, 73.1812, "Asia/Kolkata"),
    CityLocation("Rajkot", "Gujarat", 22.3039, 70.8022, "Asia/Kolkata"),
    CityLocation("Jaipur", "Rajasthan", 26.9124, 75.7873, "Asia/Kolkata"),
    CityLocation("Udaipur", "Rajasthan", 24.5854, 73.7125, "Asia/Kolkata"),
    CityLocation("Jodhpur", "Rajasthan", 26.2389, 73.0243, "Asia/Kolkata"),
    CityLocation("Varanasi", "Uttar Pradesh", 25.3176, 82.9739, "Asia/Kolkata"),
    CityLocation("Lucknow", "Uttar Pradesh", 26.8467, 80.9462, "Asia/Kolkata"),
    CityLocation("Kanpur", "Uttar Pradesh", 26.4499, 80.3319, "Asia/Kolkata"),
    CityLocation("Agra", "Uttar Pradesh", 27.1767, 78.0081, "Asia/Kolkata"),
    CityLocation("Prayagraj", "Uttar Pradesh", 25.4358, 81.8463, "Asia/Kolkata"),
    CityLocation("Ayodhya", "Uttar Pradesh", 26.7922, 82.1998, "Asia/Kolkata"),
    CityLocation("Mathura", "Uttar Pradesh", 27.4924, 77.6737, "Asia/Kolkata"),
    CityLocation("Haridwar", "Uttarakhand", 29.9457, 78.1642, "Asia/Kolkata"),
    CityLocation("Rishikesh", "Uttarakhand", 30.0869, 78.2676, "Asia/Kolkata"),
    CityLocation("Dehradun", "Uttarakhand", 30.3165, 78.0322, "Asia/Kolkata"),
    CityLocation("Ujjain", "Madhya Pradesh", 23.1765, 75.7885, "Asia/Kolkata"),
    CityLocation("Bhopal", "Madhya Pradesh", 23.2599, 77.4126, "Asia/Kolkata"),
    CityLocation("Indore", "Madhya Pradesh", 22.7196, 75.8577, "Asia/Kolkata"),
    CityLocation("Gwalior", "Madhya Pradesh", 26.2183, 78.1828, "Asia/Kolkata"),
    CityLocation("Patna", "Bihar", 25.5941, 85.1376, "Asia/Kolkata"),
    CityLocation("Gaya", "Bihar", 24.7914, 85.0002, "Asia/Kolkata"),
    CityLocation("Ranchi", "Jharkhand", 23.3441, 85.3096, "Asia/Kolkata"),
    CityLocation("Raipur", "Chhattisgarh", 21.2514, 81.6296, "Asia/Kolkata"),
    CityLocation("Bhubaneswar", "Odisha", 20.2961, 85.8245, "Asia/Kolkata"),
    CityLocation("Puri", "Odisha", 19.8135, 85.8312, "Asia/Kolkata"),
    CityLocation("Nagpur", "Maharashtra", 21.1458, 79.0882, "Asia/Kolkata"),
    CityLocation("Nashik", "Maharashtra", 19.9975, 73.7898, "Asia/Kolkata"),
    CityLocation("Chandigarh", "Chandigarh", 30.7333, 76.7794, "Asia/Kolkata"),
    CityLocation("Amritsar", "Punjab", 31.6340, 74.8723, "Asia/Kolkata"),
    CityLocation("Ludhiana", "Punjab", 30.9010, 75.8573, "Asia/Kolkata"),
    CityLocation("Shimla", "Himachal Pradesh", 31.1048, 77.1734, "Asia/Kolkata"),
    CityLocation("Srinagar", "Jammu & Kashmir", 34.0837, 74.7973, "Asia/Kolkata"),
    CityLocation("Guwahati", "Assam", 26.1445, 91.7362, "Asia/Kolkata"),
    CityLocation("Kochi", "Kerala", 9.9312, 76.2673, "Asia/Kolkata"),
    CityLocation("Thiruvananthapuram", "Kerala", 8.5241, 76.9366, "Asia/Kolkata"),
    CityLocation("Coimbatore", "Tamil Nadu", 11.0168, 76.9558, "Asia/Kolkata"),
    CityLocation("Madurai", "Tamil Nadu", 9.9252, 78.1198, "Asia/Kolkata"),
    CityLocation("Mysuru", "Karnataka", 12.2958, 76.6394, "Asia/Kolkata"),
    CityLocation("Visakhapatnam", "Andhra Pradesh", 17.6868, 83.2185, "Asia/Kolkata"),
    CityLocation("Kathmandu", "Nepal", 27.7172, 85.3240, "Asia/Kathmandu"),
    CityLocation("London", "United Kingdom", 51.5074, -0.1278, "Europe/London"),
    CityLocation("New York", "United States", 40.7128, -74.0060, "America/New_York"),
    CityLocation("San Francisco", "United States", 37.7749, -122.4194, "America/Los_Angeles"),
    CityLocation("Dubai", "UAE", 25.2048, 55.2708, "Asia/Dubai"),
    CityLocation("Singapore", "Singapore", 1.3521, 103.8198, "Asia/Singapore")
)

fun calculateTimezone(lat: Double, lon: Double, country: String? = null): String {
    if (lat.isNaN() || lon.isNaN()) return DEFAULT_PLACE_TZ
    val normCountry = country?.trim()?.lowercase(Locale.ROOT) ?: ""

    // 1. Check explicit country name first so neighboring countries (e.g. Nepal, Bangladesh) aren't shadowed by India's bounding box
    if (normCountry.isNotBlank()) {
        when {
            normCountry.contains("nepal") -> return "Asia/Kathmandu"
            normCountry.contains("bangladesh") -> return "Asia/Dhaka"
            normCountry.contains("pakistan") -> return "Asia/Karachi"
            normCountry.contains("sri lanka") -> return "Asia/Colombo"
            normCountry.contains("india") -> return "Asia/Kolkata"
            normCountry.contains("united arab emirates") || normCountry == "uae" -> return "Asia/Dubai"
            normCountry.contains("united kingdom") || normCountry == "uk" -> return "Europe/London"
            normCountry.contains("singapore") -> return "Asia/Singapore"
            normCountry.contains("united states") || normCountry == "usa" -> {
                return when {
                    lon < -115.0 -> "America/Los_Angeles"
                    lon < -100.0 -> "America/Denver"
                    lon < -85.0 -> "America/Chicago"
                    else -> "America/New_York"
                }
            }
        }
    }

    if (lat == 0.0 && lon == 0.0 && normCountry.isBlank()) {
        return DEFAULT_PLACE_TZ
    }

    // 2. Coordinate bounding-box fallbacks (smaller/neighboring regions checked before broad India box)
    if (lat in 26.3..30.5 && lon in 80.0..88.2) return "Asia/Kathmandu"
    if (lat in 20.5..26.7 && lon in 88.0..92.7) return "Asia/Dhaka"
    if (lat in 5.8..9.9 && lon in 79.5..82.0) return "Asia/Colombo"
    if (lat in 23.5..37.0 && lon in 60.5..77.0 && !(lat in 23.5..36.0 && lon in 68.1..77.0 && normCountry.contains("india"))) {
        if (lon < 74.5 && lat > 24.0) return "Asia/Karachi"
    }
    if (lat in 6.0..38.0 && lon in 68.0..97.5) return "Asia/Kolkata"
    if (lat in 22.5..26.5 && lon in 51.0..56.5) return "Asia/Dubai"
    if (lat in 49.8..60.9 && lon in -8.0..2.0) return "Europe/London"

    return try {
        val rawOffsetHours = (lon / 15.0)
        val roundedHalfHours = (rawOffsetHours * 2.0).roundToLong() / 2.0
        val wholeHours = roundedHalfHours.toInt()
        val minutes = if (abs(roundedHalfHours - wholeHours) > 0.1) 30 else 0
        val sign = if (roundedHalfHours >= 0) "+" else "-"
        String.format(Locale.US, "GMT%s%02d:%02d", sign, abs(wholeHours), minutes)
    } catch (_: Exception) {
        DEFAULT_PLACE_TZ
    }
}

fun parsePhotonResponse(responseText: String): List<PhotonPlace> {
    val root = JSONObject(responseText)
    val features = root.optJSONArray("features") ?: JSONArray()
    val list = mutableListOf<PhotonPlace>()
    for (i in 0 until features.length()) {
        val item = features.optJSONObject(i) ?: continue
        val geom = item.optJSONObject("geometry")
        val coords = geom?.optJSONArray("coordinates")
        val lon = coords?.optDouble(0) ?: 0.0
        val lat = coords?.optDouble(1) ?: 0.0
        val props = item.optJSONObject("properties")
        val osmId = props?.opt("osm_id")?.toString()?.takeIf { it.isNotBlank() && it != "0" && it != "null" }
        val uniqueId = if (osmId != null) "osm_${osmId}_${lat}_${lon}" else "coord_${lat}_${lon}_$i"
        val name = props?.optString("name")?.trim().orEmpty()
        val state = props?.optString("state")?.trim().orEmpty()
        val country = props?.optString("country")?.trim().orEmpty()
        val city = props?.optString("city")?.trim().orEmpty()
        if (name.isNotBlank()) {
            list.add(
                PhotonPlace(
                    id = uniqueId,
                    name = name,
                    state = state,
                    country = country,
                    city = city,
                    latitude = lat,
                    longitude = lon
                )
            )
        }
    }
    return list
}

suspend fun searchPhotonPlaces(query: String): List<PhotonPlace> = withContext(Dispatchers.IO) {
    val encoded = URLEncoder.encode(query.trim(), "UTF-8")
    val url = URL("https://photon.komoot.io/api/?q=$encoded&limit=5")
    val conn = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 6000
        readTimeout = 6000
        setRequestProperty("User-Agent", "AstroVeda/1.0 (Android)")
    }
    try {
        val code = conn.responseCode
        if (code in 200..299) {
            val responseText = conn.inputStream.bufferedReader().use { it.readText() }
            parsePhotonResponse(responseText)
        } else {
            throw IOException("HTTP error: $code")
        }
    } finally {
        conn.disconnect()
    }
}

@Composable
fun PlacePickerDialog(
    currentPlace: String,
    currentLat: Double,
    currentLon: Double,
    currentTz: String,
    onPlaceSelected: (name: String, lat: Double, lon: Double, tzId: String) -> Unit,
    onDismiss: () -> Unit
) {
    var useLocationSearch by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedLat by remember { mutableDoubleStateOf(if (currentLat != 0.0) currentLat else DEFAULT_PLACE_LAT) }
    var selectedLon by remember { mutableDoubleStateOf(if (currentLon != 0.0) currentLon else DEFAULT_PLACE_LON) }
    var selectedTz by remember { mutableStateOf(currentTz.ifBlank { DEFAULT_PLACE_TZ }) }
    var selectedPlaceName by remember { mutableStateOf(currentPlace.ifBlank { DEFAULT_PLACE_NAME }) }

    var manualLatText by remember { mutableStateOf(selectedLat.toString()) }
    var manualLonText by remember { mutableStateOf(selectedLon.toString()) }

    var photonResults by remember { mutableStateOf<List<PhotonPlace>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var showFallbackNotice by remember { mutableStateOf(false) }

    var selectedPlace by remember { mutableStateOf<PhotonPlace?>(null) }
    var selectedCity by remember { mutableStateOf<CityLocation?>(null) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun dismissKeyboard() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    val customCoordFormat = stringResource(R.string.place_custom_format, selectedLat, selectedLon)

    LaunchedEffect(searchQuery, useLocationSearch) {
        selectedPlace = null
        selectedCity = null

        if (!useLocationSearch) {
            photonResults = emptyList()
            isSearching = false
            showFallbackNotice = false
            return@LaunchedEffect
        }

        val query = searchQuery.trim()
        if (query.length < 2) {
            photonResults = emptyList()
            isSearching = false
            showFallbackNotice = false
            return@LaunchedEffect
        }

        delay(500)
        isSearching = true
        showFallbackNotice = false
        try {
            val results = searchPhotonPlaces(query)
            photonResults = results
            if (results.isEmpty()) {
                showFallbackNotice = true
            }
        } catch (_: Exception) {
            photonResults = emptyList()
            showFallbackNotice = true
        } finally {
            isSearching = false
        }
    }

    val suggestedCities = remember(searchQuery, useLocationSearch, showFallbackNotice) {
        if (!useLocationSearch || searchQuery.isBlank() || showFallbackNotice) {
            val filtered = if (searchQuery.isBlank()) MAJOR_CITIES else MAJOR_CITIES.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.state.contains(searchQuery, ignoreCase = true)
            }
            filtered.ifEmpty { MAJOR_CITIES }
        } else {
            MAJOR_CITIES.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.state.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val showPhotonResultsList = useLocationSearch && searchQuery.trim().length >= 2 && photonResults.isNotEmpty()
    val showFallbackControls = !useLocationSearch || showFallbackNotice || !showPhotonResultsList

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.place_picker_title),
                style = MaterialTheme.typography.titleLarge,
                color = DeepIndigo,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .clearFocusOnTap()
            ) {
                // Toggle: Use location search (ON by default)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.place_use_location_search),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepIndigo
                    )
                    Switch(
                        checked = useLocationSearch,
                        onCheckedChange = {
                            dismissKeyboard()
                            useLocationSearch = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SaffronDark,
                            checkedTrackColor = SaffronAccent.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.testTag("use_location_search_switch")
                    )
                }

                if (useLocationSearch) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(stringResource(R.string.place_search_placeholder)) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = DeepIndigo)
                        },
                        trailingIcon = {
                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = SaffronDark
                                )
                            } else if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(R.string.cd_clear_search),
                                        tint = TextMuted
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepIndigo,
                            unfocusedBorderColor = CreamBorder,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("birth_place_search_input")
                    )

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.place_attribution),
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Inline fallback notice if Photon errored or returned no results
                if (useLocationSearch && showFallbackNotice) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("place_search_fallback_notice")
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF856404),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.place_search_unavailable),
                                fontSize = 12.sp,
                                color = Color(0xFF856404)
                            )
                        }
                    }
                }

                // Selected place summary card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, CreamBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = SaffronDark, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = selectedPlaceName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepIndigo,
                                maxLines = 1
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.place_coords_summary, selectedLat, selectedLon, selectedTz),
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                // Scrollable Photon results or 50+ City Directory + Manual Entry
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (showPhotonResultsList) {
                        items(photonResults, key = { it.id }) { place ->
                            val isSelected = selectedPlace?.id == place.id
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) SaffronAccent.copy(alpha = 0.2f) else Color.White,
                                border = BorderStroke(1.dp, if (isSelected) SaffronDark else CreamBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        dismissKeyboard()
                                        selectedPlace = place
                                        selectedCity = null
                                        selectedPlaceName = place.displayName
                                        selectedLat = place.latitude
                                        selectedLon = place.longitude
                                        manualLatText = place.latitude.toString()
                                        manualLonText = place.longitude.toString()
                                        selectedTz = calculateTimezone(place.latitude, place.longitude, place.country)
                                    }
                                    .testTag("photon_place_${place.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationCity, contentDescription = null, tint = DeepIndigo, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = place.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DeepIndigo
                                        )
                                        if (place.subtitle.isNotBlank()) {
                                            Text(
                                                text = place.subtitle,
                                                fontSize = 12.sp,
                                                color = TextMuted
                                            )
                                        }
                                        Text(
                                            text = stringResource(
                                                R.string.place_city_coords_row,
                                                place.latitude,
                                                place.longitude,
                                                calculateTimezone(place.latitude, place.longitude, place.country)
                                            ),
                                            fontSize = 10.sp,
                                            color = SaffronDark
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = SaffronDark)
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            Text(
                                text = stringResource(R.string.place_city_directory),
                                style = MaterialTheme.typography.titleSmall,
                                color = DeepIndigo,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        items(suggestedCities, key = { it.id }) { city ->
                            val isSelected = selectedCity?.id == city.id
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) SaffronAccent.copy(alpha = 0.2f) else Color.White,
                                border = BorderStroke(1.dp, if (isSelected) SaffronDark else CreamBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        dismissKeyboard()
                                        selectedCity = city
                                        selectedPlace = null
                                        selectedPlaceName = city.displayLabel
                                        selectedLat = city.latitude
                                        selectedLon = city.longitude
                                        manualLatText = city.latitude.toString()
                                        manualLonText = city.longitude.toString()
                                        selectedTz = city.timezoneId
                                    }
                                    .testTag("city_item_${city.name}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = DeepIndigo, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = city.displayLabel,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DeepIndigo
                                        )
                                        Text(
                                            text = stringResource(
                                                R.string.place_city_coords_row,
                                                city.latitude,
                                                city.longitude,
                                                city.timezoneId
                                            ),
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = SaffronDark)
                                    }
                                }
                            }
                        }
                    }
                }

                // Manual Lat / Lon / Timezone fields (immediately visible on fallback or toggle OFF; also always accessible)
                if (showFallbackControls) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.place_manual_section),
                        style = MaterialTheme.typography.titleSmall,
                        color = DeepIndigo,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = manualLatText,
                            onValueChange = { raw ->
                                manualLatText = raw
                                raw.toDoubleOrNull()?.let { lat ->
                                    selectedLat = lat
                                    selectedPlace = null
                                    selectedCity = null
                                    selectedPlaceName = customCoordFormat
                                }
                            },
                            label = { Text(stringResource(R.string.place_lat_label), fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_lat_input")
                        )
                        OutlinedTextField(
                            value = manualLonText,
                            onValueChange = { raw ->
                                manualLonText = raw
                                raw.toDoubleOrNull()?.let { lon ->
                                    selectedLon = lon
                                    selectedPlace = null
                                    selectedCity = null
                                    selectedPlaceName = customCoordFormat
                                }
                            },
                            label = { Text(stringResource(R.string.place_lon_label), fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_lon_input")
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = selectedTz,
                        onValueChange = { selectedTz = it },
                        label = { Text(stringResource(R.string.place_tz_label), fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_tz_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    dismissKeyboard()
                    val safeName = selectedPlaceName.ifBlank { DEFAULT_PLACE_NAME }
                    val safeTz = selectedTz.trim().ifBlank { DEFAULT_PLACE_TZ }
                    onPlaceSelected(safeName, selectedLat, selectedLon, safeTz)
                    onDismiss()
                },
                colors = astroPrimaryButtonColors(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("place_confirm_btn")
            ) {
                Text(stringResource(R.string.place_select_btn), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(stringResource(R.string.cancel_btn), color = DeepIndigo)
            }
        },
        containerColor = CreamBackground,
        shape = RoundedCornerShape(16.dp)
    )
}
