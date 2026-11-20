package com.aryaleelastudios.kundaliguru.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaleelastudios.kundaliguru.ui.theme.CreamBackground
import com.aryaleelastudios.kundaliguru.ui.theme.CreamBorder
import com.aryaleelastudios.kundaliguru.ui.theme.DeepIndigo
import com.aryaleelastudios.kundaliguru.ui.theme.SaffronAccent
import com.aryaleelastudios.kundaliguru.ui.theme.SaffronDark
import com.aryaleelastudios.kundaliguru.ui.theme.TextMuted
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
    val nameEn: String,
    val nameHi: String,
    val stateEn: String,
    val stateHi: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String,
    val id: String = "city_${nameEn}_${stateEn}"
)

val MAJOR_CITIES = listOf(
    CityLocation("New Delhi", "नई दिल्ली", "Delhi", "दिल्ली", 28.6139, 77.2090, "Asia/Kolkata"),
    CityLocation("Varanasi", "वाराणसी", "Uttar Pradesh", "उत्तर प्रदेश", 25.3176, 82.9739, "Asia/Kolkata"),
    CityLocation("Ujjain", "उज्जैन", "Madhya Pradesh", "मध्य प्रदेश", 23.1765, 75.7885, "Asia/Kolkata"),
    CityLocation("Haridwar", "हरिद्वार", "Uttarakhand", "उत्तराखंड", 29.9457, 78.1642, "Asia/Kolkata"),
    CityLocation("Ayodhya", "अयोध्या", "Uttar Pradesh", "उत्तर प्रदेश", 26.7922, 82.1998, "Asia/Kolkata"),
    CityLocation("Mumbai", "मुंबई", "Maharashtra", "महाराष्ट्र", 19.0760, 72.8777, "Asia/Kolkata"),
    CityLocation("Bengaluru", "बेंगलुरु", "Karnataka", "कर्नाटक", 12.9716, 77.5946, "Asia/Kolkata"),
    CityLocation("Kolkata", "कोलकाता", "West Bengal", "पश्चिम बंगाल", 22.5726, 88.3639, "Asia/Kolkata"),
    CityLocation("Chennai", "चेन्नई", "Tamil Nadu", "तमिलनाडु", 13.0827, 80.2707, "Asia/Kolkata"),
    CityLocation("Hyderabad", "हैदराबाद", "Telangana", "तेलंगाना", 17.3850, 78.4867, "Asia/Kolkata"),
    CityLocation("Ahmedabad", "अहमदाबाद", "Gujarat", "गुजरात", 23.0225, 72.5714, "Asia/Kolkata"),
    CityLocation("Pune", "पुणे", "Maharashtra", "महाराष्ट्र", 18.5204, 73.8567, "Asia/Kolkata"),
    CityLocation("Jaipur", "जयपुर", "Rajasthan", "राजस्थान", 26.9124, 75.7873, "Asia/Kolkata"),
    CityLocation("Lucknow", "लखनऊ", "Uttar Pradesh", "उत्तर प्रदेश", 26.8467, 80.9462, "Asia/Kolkata"),
    CityLocation("Patna", "पटना", "Bihar", "बिहार", 25.5941, 85.1376, "Asia/Kolkata"),
    CityLocation("Bhopal", "भोपाल", "Madhya Pradesh", "मध्य प्रदेश", 23.2599, 77.4126, "Asia/Kolkata"),
    CityLocation("Indore", "इंदौर", "Madhya Pradesh", "मध्य प्रदेश", 22.7196, 75.8577, "Asia/Kolkata"),
    CityLocation("Surat", "सूरत", "Gujarat", "गुजरात", 21.1702, 72.8311, "Asia/Kolkata"),
    CityLocation("Nagpur", "नागपुर", "Maharashtra", "महाराष्ट्र", 21.1458, 79.0882, "Asia/Kolkata"),
    CityLocation("Chandigarh", "चंडीगढ़", "Punjab / Haryana", "पंजाब / हरियाणा", 30.7333, 76.7794, "Asia/Kolkata"),
    CityLocation("Dehradun", "देहरादून", "Uttarakhand", "उत्तराखंड", 30.3165, 78.0322, "Asia/Kolkata"),
    CityLocation("Ranchi", "राँची", "Jharkhand", "झारखंड", 23.3441, 85.3096, "Asia/Kolkata"),
    CityLocation("Raipur", "रायपुर", "Chhattisgarh", "छत्तीसगढ़", 21.2514, 81.6296, "Asia/Kolkata"),
    CityLocation("Bhubaneswar", "भुवनेश्वर", "Odisha", "ओडिशा", 20.2961, 85.8245, "Asia/Kolkata"),
    CityLocation("Guwahati", "गुवाहाटी", "Assam", "असम", 26.1445, 91.7362, "Asia/Kolkata"),
    CityLocation("Kathmandu", "काठमांडू", "Bagmati", "नेपाल", 27.7172, 85.3240, "Asia/Kathmandu"),
    CityLocation("London", "लंदन", "United Kingdom", "यूके", 51.5074, -0.1278, "Europe/London"),
    CityLocation("New York", "न्यूयॉर्क", "New York", "यूएसए", 40.7128, -74.0060, "America/New_York"),
    CityLocation("Dubai", "दुबई", "UAE", "यूएई", 25.2048, 55.2708, "Asia/Dubai"),
    CityLocation("Singapore", "सिंगापुर", "Singapore", "सिंगापुर", 1.3521, 103.8198, "Asia/Singapore")
)

/**
 * Calculates or estimates the timezone string based on latitude, longitude, and optional country.
 */
fun calculateTimezone(lat: Double, lon: Double, country: String? = null): String {
    val normCountry = country?.lowercase(Locale.ROOT) ?: ""
    if (normCountry.contains("india") || (lat in 6.0..38.0 && lon in 68.0..97.5)) {
        return "Asia/Kolkata"
    }
    if (normCountry.contains("nepal") || (lat in 26.0..31.0 && lon in 80.0..89.0)) {
        return "Asia/Kathmandu"
    }
    if (normCountry.contains("bangladesh") || (lat in 20.5..26.7 && lon in 88.0..92.7)) {
        return "Asia/Dhaka"
    }
    if (normCountry.contains("pakistan") || (lat in 23.5..37.0 && lon in 60.5..77.0)) {
        return "Asia/Karachi"
    }
    if (normCountry.contains("sri lanka") || (lat in 5.8..9.9 && lon in 79.5..82.0)) {
        return "Asia/Colombo"
    }
    if (normCountry.contains("united arab emirates") || normCountry.contains("uae") || (lat in 22.5..26.5 && lon in 51.0..56.5)) {
        return "Asia/Dubai"
    }
    if (normCountry.contains("united kingdom") || normCountry == "uk" || (lat in 49.8..60.9 && lon in -8.0..2.0)) {
        return "Europe/London"
    }
    if (normCountry.contains("singapore")) {
        return "Asia/Singapore"
    }
    if (normCountry.contains("united states") || normCountry == "usa") {
        return when {
            lon < -115.0 -> "America/Los_Angeles"
            lon < -100.0 -> "America/Denver"
            lon < -85.0 -> "America/Chicago"
            else -> "America/New_York"
        }
    }
    // Longitude-based estimation to standard GMT offset
    val rawOffsetHours = (lon / 15.0)
    val roundedHalfHours = Math.round(rawOffsetHours * 2.0) / 2.0
    val wholeHours = roundedHalfHours.toInt()
    val minutes = if (Math.abs(roundedHalfHours - wholeHours) > 0.1) 30 else 0
    val sign = if (roundedHalfHours >= 0) "+" else "-"
    return String.format(Locale.US, "GMT%s%02d:%02d", sign, Math.abs(wholeHours), minutes)
}

/**
 * Searches places via Photon Geocoding API (powered by OpenStreetMap).
 */
suspend fun searchPhotonPlaces(query: String): List<PhotonPlace> = withContext(Dispatchers.IO) {
    val encoded = URLEncoder.encode(query.trim(), "UTF-8")
    val url = URL("https://photon.komoot.io/api/?q=$encoded&limit=5")
    val conn = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 6000
        readTimeout = 6000
        setRequestProperty("User-Agent", "KundaliGuru/2.0 (Android)")
    }
    try {
        val code = conn.responseCode
        if (code in 200..299) {
            val responseText = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(responseText)
            val features = root.optJSONArray("features") ?: JSONArray()
            val list = mutableListOf<PhotonPlace>()
            for (i in 0 until features.length()) {
                val item = features.optJSONObject(i) ?: continue
                val geom = item.optJSONObject("geometry")
                val coords = geom?.optJSONArray("coordinates")
                // Photon returns [longitude, latitude]
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
            list
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
    isHindi: Boolean,
    onPlaceSelected: (name: String, lat: Double, lon: Double, tzId: String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLat by remember { mutableDoubleStateOf(if (currentLat != 0.0) currentLat else 28.6139) }
    var selectedLon by remember { mutableDoubleStateOf(if (currentLon != 0.0) currentLon else 77.2090) }
    var selectedTz by remember { mutableStateOf(if (currentTz.isNotBlank()) currentTz else "Asia/Kolkata") }
    var selectedPlaceName by remember { mutableStateOf(if (currentPlace.isNotBlank()) currentPlace else "New Delhi, Delhi") }

    var manualLatText by remember { mutableStateOf(selectedLat.toString()) }
    var manualLonText by remember { mutableStateOf(selectedLon.toString()) }

    var photonResults by remember { mutableStateOf<List<PhotonPlace>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchErrorMessage by remember { mutableStateOf<String?>(null) }
    var showAdvancedOverride by remember { mutableStateOf(false) }

    // Holds exactly one selected place object at a time for unique selection highlighting
    var selectedPlace by remember { mutableStateOf<PhotonPlace?>(null) }
    var selectedCity by remember { mutableStateOf<CityLocation?>(null) }

    // Photon autocomplete with 500ms debounce
    LaunchedEffect(searchQuery) {
        // Clear previous selected state when a new search query is entered
        selectedPlace = null
        selectedCity = null

        val query = searchQuery.trim()
        if (query.length < 2) {
            photonResults = emptyList()
            isSearching = false
            searchErrorMessage = null
            return@LaunchedEffect
        }

        delay(500)
        isSearching = true
        searchErrorMessage = null
        try {
            val results = searchPhotonPlaces(query)
            photonResults = results
            if (results.isEmpty()) {
                searchErrorMessage = if (isHindi) {
                    "कोई परिणाम नहीं मिला। कृपया मैन्युअल दर्ज करें।"
                } else {
                    "No places found. Please enter coordinates manually."
                }
            }
        } catch (e: Exception) {
            searchErrorMessage = if (isHindi) {
                "स्थान खोजने में त्रुटि। कृपया मैन्युअल दर्ज करें।"
            } else {
                "Could not search places. Please enter coordinates manually."
            }
        } finally {
            isSearching = false
        }
    }

    val suggestedCities = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            MAJOR_CITIES
        } else {
            MAJOR_CITIES.filter {
                it.nameEn.contains(searchQuery, ignoreCase = true) ||
                it.nameHi.contains(searchQuery, ignoreCase = true) ||
                it.stateEn.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isHindi) "जन्म स्थान चुनें" else "Select Birth Place",
                style = MaterialTheme.typography.titleLarge,
                color = DeepIndigo,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                // Text input field for searching a city/village
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(if (isHindi) "शहर या गाँव खोजें..." else "Search city or village...")
                    },
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
                                Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepIndigo,
                        unfocusedBorderColor = CreamBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("birth_place_search_input")
                )

                // Mandatory attribution text
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Powered by OpenStreetMap / Photon",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Spacer(Modifier.height(8.dp))

                // Error message if search fails or no results
                if (searchErrorMessage != null && searchQuery.trim().length >= 2) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
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
                                text = searchErrorMessage.orEmpty(),
                                fontSize = 12.sp,
                                color = Color(0xFF856404)
                            )
                        }
                    }
                }

                // Currently selected badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = CreamBackground),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Lat: ${"%.4f".format(selectedLat)}", fontSize = 11.sp, color = DeepIndigo)
                            Text("Lon: ${"%.4f".format(selectedLon)}", fontSize = 11.sp, color = DeepIndigo)
                            Text("TZ: $selectedTz", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }

                // Autocomplete results or suggested cities
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (searchQuery.trim().length >= 2 && photonResults.isNotEmpty()) {
                        // Photon API search results with unique key = { it.id }
                        items(photonResults, key = { it.id }) { place ->
                            val isSelected = selectedPlace?.id == place.id
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) SaffronAccent.copy(alpha = 0.2f) else Color.White,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) SaffronDark else CreamBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
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
                                            text = "Lat: ${"%.4f".format(place.latitude)}°, Lon: ${"%.4f".format(place.longitude)}°",
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
                        // Quick directory of major cities with unique key = { it.id }
                        items(suggestedCities, key = { it.id }) { city ->
                            val isSelected = selectedCity?.id == city.id
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) SaffronAccent.copy(alpha = 0.2f) else Color.White,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) SaffronDark else CreamBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCity = city
                                        selectedPlace = null
                                        selectedPlaceName = "${city.nameEn}, ${city.stateEn}"
                                        selectedLat = city.latitude
                                        selectedLon = city.longitude
                                        manualLatText = city.latitude.toString()
                                        manualLonText = city.longitude.toString()
                                        selectedTz = city.timezoneId
                                    }
                                    .testTag("city_item_${city.nameEn}")
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
                                            text = if (isHindi) "${city.nameHi} (${city.nameEn})" else "${city.nameEn}, ${city.stateEn}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DeepIndigo
                                        )
                                        Text(
                                            text = "Lat: ${"%.2f".format(city.latitude)}°N, Lon: ${"%.2f".format(city.longitude)}°E",
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

                // Advanced manual lat/long override - ALWAYS accessible
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdvancedOverride = !showAdvancedOverride }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (showAdvancedOverride) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = DeepIndigo
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (isHindi) "उन्नत: अक्षांश / देशांतर मैनुअल दर्ज करें" else "Advanced: Manual Lat/Long/Timezone",
                        style = MaterialTheme.typography.labelMedium,
                        color = DeepIndigo,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (showAdvancedOverride) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = manualLatText,
                            onValueChange = {
                                manualLatText = it
                                it.toDoubleOrNull()?.let { lat ->
                                    selectedLat = lat
                                    selectedPlace = null
                                    selectedCity = null
                                    selectedPlaceName = "Custom (${"%.4f".format(selectedLat)}, ${"%.4f".format(selectedLon)})"
                                }
                            },
                            label = { Text("Lat") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_lat_input")
                        )
                        OutlinedTextField(
                            value = manualLonText,
                            onValueChange = {
                                manualLonText = it
                                it.toDoubleOrNull()?.let { lon ->
                                    selectedLon = lon
                                    selectedPlace = null
                                    selectedCity = null
                                    selectedPlaceName = "Custom (${"%.4f".format(selectedLat)}, ${"%.4f".format(selectedLon)})"
                                }
                            },
                            label = { Text("Lon") },
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
                        label = { Text("Timezone (e.g. Asia/Kolkata)") },
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
                    onPlaceSelected(selectedPlaceName, selectedLat, selectedLon, selectedTz)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepIndigo, contentColor = SaffronAccent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("place_confirm_btn")
            ) {
                Text(if (isHindi) "चुनें (Select)" else "Select Place", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "रद्द करें" else "Cancel", color = DeepIndigo)
            }
        },
        containerColor = CreamBackground,
        shape = RoundedCornerShape(16.dp)
    )
}
