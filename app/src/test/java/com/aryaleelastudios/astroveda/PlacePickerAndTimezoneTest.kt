package com.aryaleelastudios.astroveda

import com.aryaleelastudios.astroveda.ui.components.DEFAULT_PLACE_TZ
import com.aryaleelastudios.astroveda.ui.components.MAJOR_CITIES
import com.aryaleelastudios.astroveda.ui.components.calculateTimezone
import com.aryaleelastudios.astroveda.ui.components.parsePhotonResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PlacePickerAndTimezoneTest {

    @Test
    fun majorCitiesDirectory_hasAtLeast50CitiesIncludingNewDelhi() {
        assertTrue("City list must contain at least 50 cities", MAJOR_CITIES.size >= 50)
        val delhi = MAJOR_CITIES.find { it.name == "New Delhi" }
        assertTrue(delhi != null)
        assertEquals("Asia/Kolkata", delhi!!.timezoneId)
    }

    @Test
    fun calculateTimezone_resolvesRegionsAndFallsBackSafelyToAsiaKolkata() {
        assertEquals("Asia/Kolkata", calculateTimezone(28.6139, 77.2090, "India"))
        assertEquals("Asia/Kathmandu", calculateTimezone(27.7172, 85.3240, "Nepal"))
        assertEquals("Europe/London", calculateTimezone(51.5074, -0.1278, "United Kingdom"))
        assertEquals("America/New_York", calculateTimezone(40.7128, -74.0060, "United States"))
        assertEquals(DEFAULT_PLACE_TZ, calculateTimezone(0.0, 0.0, null))
        assertEquals(DEFAULT_PLACE_TZ, calculateTimezone(Double.NaN, Double.NaN, null))
    }

    @Test
    fun parsePhotonResponse_extractsGeoJsonCoordinatesAndProperties() {
        val sampleGeoJson = """
            {
              "features": [
                {
                  "geometry": { "coordinates": [75.7873, 26.9124] },
                  "properties": {
                    "osm_id": 123456,
                    "name": "Jaipur",
                    "state": "Rajasthan",
                    "country": "India",
                    "city": "Jaipur"
                  }
                }
              ]
            }
        """.trimIndent()

        val places = parsePhotonResponse(sampleGeoJson)
        assertEquals(1, places.size)
        val jaipur = places.first()
        assertEquals("Jaipur", jaipur.name)
        assertEquals("Rajasthan", jaipur.state)
        assertEquals("India", jaipur.country)
        assertEquals(26.9124, jaipur.latitude, 0.0001)
        assertEquals(75.7873, jaipur.longitude, 0.0001)
    }
}
