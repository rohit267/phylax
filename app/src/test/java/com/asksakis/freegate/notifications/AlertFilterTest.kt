package com.asksakis.freegate.notifications

import org.json.JSONObject
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AlertFilterTest {
    private val gate = Regex("gate", RegexOption.IGNORE_CASE)

    private fun review(camera: String, vararg zones: String): JSONObject {
        val zoneJson = zones.joinToString(",") { "\"$it\"" }
        return JSONObject(
            """{"payload":{"type":"new","after":{"id":"r1","camera":"$camera","severity":"alert",
            "start_time":100.0,"data":{"zones":[$zoneJson],"objects":["person"],"detections":[]}}}}""",
        )
    }

    /** [muteOthers] = at home with outside mode on; false = outside (or mode off). */
    private fun filter(
        pattern: Regex? = gate,
        muteOthers: Boolean,
        zoneAllowlist: Set<String> = emptySet(),
    ) = AlertFilter(
        allowAlerts = true,
        allowDetections = false,
        cameraAllowlist = emptySet(),
        zoneAllowlist = zoneAllowlist,
        zonePickerOpened = zoneAllowlist.isNotEmpty(),
        alwaysZonePattern = pattern,
        muteNonAlwaysZones = muteOthers,
    )

    private fun pass(f: AlertFilter, camera: String, vararg zones: String) =
        assertNotNull("$camera ${zones.toList()} should notify", f.evaluate("reviews", review(camera, *zones)))

    private fun drop(f: AlertFilter, camera: String, vararg zones: String) =
        assertNull("$camera ${zones.toList()} should be muted", f.evaluate("reviews", review(camera, *zones)))

    @Test fun atHome_gateZonePasses() = pass(filter(muteOthers = true), "Front", "gate")

    @Test fun atHome_patternIsCaseInsensitiveAndMatchesAnywhere() {
        val f = filter(muteOthers = true)
        pass(f, "East", "EastGate")
        pass(f, "East", "eastgate")
    }

    @Test fun atHome_otherZonesAreMuted() {
        val f = filter(muteOthers = true)
        drop(f, "Front", "yard")
        drop(f, "West", "back_garden")
    }

    @Test fun atHome_reviewWithoutZonesIsMuted() = drop(filter(muteOthers = true), "Front")

    @Test fun atHome_oneGateZoneAmongOthersPasses() = pass(filter(muteOthers = true), "Front", "yard", "gate")

    @Test fun outside_everythingPasses() {
        val f = filter(muteOthers = false)
        pass(f, "Front", "gate")
        pass(f, "Front", "yard")
        pass(f, "Front")
    }

    @Test fun outside_zonePickerIsIgnored() {
        val f = filter(muteOthers = false, zoneAllowlist = setOf("Front:yard"))
        pass(f, "Front", "gate")
        pass(f, "Front", "yard")
        pass(f, "Front", "porch")
        pass(f, "Front")
    }

    @Test fun atHome_zonePickerIsIgnoredToo() {
        val f = filter(muteOthers = true, zoneAllowlist = setOf("Front:yard"))
        pass(f, "Front", "gate")
        drop(f, "Front", "yard")
    }

    @Test fun modeOff_zonePickerStillApplies() {
        val f = filter(pattern = null, muteOthers = false, zoneAllowlist = setOf("Front:yard"))
        pass(f, "Front", "yard")
        drop(f, "Front", "porch")
    }

    @Test fun modeOff_behavesLikeBefore() {
        val f = filter(pattern = null, muteOthers = false)
        pass(f, "Front", "yard")
        pass(f, "Front")
    }
}
