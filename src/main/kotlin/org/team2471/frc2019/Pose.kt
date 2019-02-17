package org.team2471.frc2019

import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.Length
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.inches

data class Pose(
    val elevatorHeight: Length,
    val armAngle: Angle,
    val ob1Angle: Angle,
    val isClamping: Boolean = true,
    val isPinching: Boolean = false
) {
    val clawHeight =
        Armavator.ELEVATOR_HEIGHT.inches + elevatorHeight + (Math.sin(armAngle.asRadians) * Armavator.ARM_LENGTH).inches

    companion object {
        val STARTING_POSITION = Pose(0.inches, (-74).degrees, 0.degrees)
        val HATCH_HANDOFF = Pose(4.inches, (-20).degrees, 97.degrees, false)
    }
}

