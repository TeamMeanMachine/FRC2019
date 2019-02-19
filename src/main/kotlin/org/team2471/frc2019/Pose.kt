package org.team2471.frc2019

import org.team2471.frc.lib.units.*
import kotlin.math.abs

data class Pose(
    val elevatorHeight: Length,
    val armAngle: Angle,
    val ob1Angle: Angle,
    val isClamping: Boolean,
    val isPinching: Boolean = false,
    val isClimbing: Boolean = false
) {
    val clawHeight =
        Armavator.ELEVATOR_HEIGHT.inches + elevatorHeight + (Math.sin(armAngle.asRadians) * Armavator.ARM_LENGTH).inches

    fun closeTo(other: Pose) : Boolean {
        return abs(other.elevatorHeight.asInches - elevatorHeight.asInches) < 4.0 &&
                abs(other.armAngle.asDegrees - armAngle.asDegrees) < 7.0 &&
                abs(other.ob1Angle.asDegrees - ob1Angle.asDegrees) < 7.0
    }

    companion object {
        val STARTING_POSITION = Pose(0.inches, (-74).degrees, 90.degrees, true)
        val HOME = Pose(0.inches, (-74).degrees, 0.degrees, true)
        val SAFETY_POSE = Pose(14.inches, (-74).degrees, 0.degrees, false)
        val CARGO_SAFETY_POSE = Pose(14.inches, (-74).degrees, 0.degrees, true)

        val HATCH_GROUND_PICKUP = Pose(14.inches, (-74).degrees, (-2).degrees, false, true)
        val HATCH_HANDOFF = Pose(4.inches, (-20).degrees, 97.degrees, false, true)
        val HATCH_CARRY = Pose(7.inches, (-69).degrees, 1.degrees, false)
        val HATCH_SCORE_1 = Pose(0.inches, (-24).degrees, 6.degrees, false)
        val HATCH_SCORE_2 = Pose(4.2.inches, 19.degrees, 6.degrees, false)
        val HATCH_SCORE_3 = Pose(25.8.inches, 22.3.degrees, 6.degrees, false)

        val CLIMB_START = Pose(1.inches, 18.degrees, 120.degrees, false, isClimbing = true)
        val LIFTED = Pose((-20).inches, 64.degrees, (-2).degrees, false, isClimbing = true)

        val CARGO_GROUND_PICKUP = Pose(0.inches, (-74).degrees, 63.degrees, true, false)
        val CARGO_SCORE_1 = Pose(17.inches, (-57).degrees, 6.degrees, true, false)
        val CARGO_SCORE_2 = Pose(0.inches, (57).degrees, 6.degrees, true, false)
        val CARGO_SCORE_3 = Pose(26.inches, (64).degrees, 6.degrees, true, false)
        val CARGO_SHIP_SCORE = Pose(0.inches, (30).degrees, 6.degrees, true, false)
    }
}

