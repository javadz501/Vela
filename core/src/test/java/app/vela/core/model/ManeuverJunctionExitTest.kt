package app.vela.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ManeuverJunctionExitTest {
    private fun maneuver(type: ManeuverType, lanes: List<Lane> = emptyList()) = Maneuver(
        type = type,
        instruction = "instruction",
        location = LatLng(0.0, 0.0),
        distanceMeters = 0.0,
        durationSeconds = 0.0,
        lanes = lanes,
    )

    @Test
    fun `right straight and left branches map to stable ordinals`() {
        assertEquals(1, maneuver(ManeuverType.TURN_RIGHT).junctionExitOrdinal)
        assertEquals(1, maneuver(ManeuverType.SLIGHT_RIGHT).junctionExitOrdinal)
        assertEquals(1, maneuver(ManeuverType.FORK_RIGHT).junctionExitOrdinal)
        assertEquals(1, maneuver(ManeuverType.KEEP_RIGHT).junctionExitOrdinal)

        assertEquals(3, maneuver(ManeuverType.TURN_LEFT).junctionExitOrdinal)
        assertEquals(3, maneuver(ManeuverType.SLIGHT_LEFT).junctionExitOrdinal)
        assertEquals(3, maneuver(ManeuverType.FORK_LEFT).junctionExitOrdinal)
        assertEquals(3, maneuver(ManeuverType.KEEP_LEFT).junctionExitOrdinal)
    }

    @Test
    fun `straight ordinal requires explicit genuine fork lane data`() {
        val forkLanes = listOf(
            Lane(indications = listOf("left"), valid = true),
            Lane(indications = listOf("straight"), valid = false),
        )
        assertEquals(2, maneuver(ManeuverType.CONTINUE, forkLanes).junctionExitOrdinal)
        assertEquals(2, maneuver(ManeuverType.STRAIGHT, forkLanes).junctionExitOrdinal)

        val straightWithLeftTurnBay = listOf(
            Lane(indications = listOf("straight"), valid = true),
            Lane(indications = listOf("left"), valid = false),
        )
        assertEquals(2, maneuver(ManeuverType.CONTINUE, straightWithLeftTurnBay).junctionExitOrdinal)

        val unmarkedAlternative = listOf(
            Lane(indications = listOf("straight"), valid = true),
            Lane(indications = listOf("none"), valid = false),
        )
        assertEquals(null, maneuver(ManeuverType.CONTINUE, unmarkedAlternative).junctionExitOrdinal)
        assertEquals(null, maneuver(ManeuverType.CONTINUE).junctionExitOrdinal)
        assertEquals(null, maneuver(ManeuverType.STRAIGHT).junctionExitOrdinal)
    }

    @Test
    fun `roundabouts signed ramps and u turns keep their existing instructions`() {
        assertEquals(null, maneuver(ManeuverType.ROUNDABOUT).junctionExitOrdinal)
        assertEquals(null, maneuver(ManeuverType.EXIT_ROUNDABOUT).junctionExitOrdinal)
        assertEquals(null, maneuver(ManeuverType.RAMP_RIGHT).junctionExitOrdinal)
        assertEquals(null, maneuver(ManeuverType.UTURN).junctionExitOrdinal)
    }
}
