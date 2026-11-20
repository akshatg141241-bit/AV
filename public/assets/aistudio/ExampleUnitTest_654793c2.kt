package com.aryaleelastudios.kundaliguru

import com.aryaleelastudios.kundaliguru.astro.AshtaKootaEngine
import com.aryaleelastudios.kundaliguru.astro.AstroEngine
import com.aryaleelastudios.kundaliguru.data.model.AyanamsaType
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `verify astro calculations`() {
        val jd = AstroEngine.calculateJulianDay(1995, 8, 15, 14, 30, 0, 5.5)
        assertTrue("Julian day must be positive", jd > 2440000.0)

        val ayanamsa = AstroEngine.calculateAyanamsa(jd, AyanamsaType.LAHIRI)
        assertTrue("Ayanamsa should be around 23.8", ayanamsa in 23.0..24.5)

        val lagna = AstroEngine.calculateLagna(jd, 25.3176, 82.9739, ayanamsa)
        assertNotNull(lagna)
        assertTrue(lagna.rashiIndex in 0..11)
        assertTrue(lagna.nakshatraIndex in 0..26)
    }

    @Test
    fun `verify ashta koota gunas`() {
        val result = AshtaKootaEngine.matchHoroscopes(
            groomName = "Devendra",
            groomRashiIdx = 3,
            groomNakshatraIdx = 7,
            groomIsManglik = false,
            brideName = "Sita",
            brideRashiIdx = 11,
            brideNakshatraIdx = 26,
            brideIsManglik = false
        )
        assertNotNull(result)
        assertEquals(8, result.kootas.size)
        assertTrue(result.totalScore in 0.0..36.0)
    }
}
