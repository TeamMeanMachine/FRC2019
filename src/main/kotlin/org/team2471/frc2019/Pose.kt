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

    fun closeTo(other: Pose): Boolean {
        return abs(other.elevatorHeight.asInches - elevatorHeight.asInches) < 6.0 &&
                abs(other.armAngle.asDegrees - armAngle.asDegrees) < 9.0 &&
                abs(other.ob1Angle.asDegrees - ob1Angle.asDegrees) < 9.0
    }

    companion object {
        val current
            get () = Pose(
                Armavator.height,
                Armavator.angle,
                OB1.angle,
                Armavator.isClamping,
                Armavator.isPinching,
                Armavator.isClimbing
            )
        val STARTING_POSITION = Pose(0.inches, (-72).degrees, 90.degrees, true)
        val HOME = Pose(0.inches, (-72).degrees, 0.degrees, true)
        val SAFETY_POSE = Pose(14.inches, (-72).degrees, 0.degrees, false)
        val CARGO_SAFETY_POSE = Pose(14.inches, (-72).degrees, 0.degrees, true)

        val HATCH_GROUND_PICKUP = Pose(14.inches, (-72).degrees, (-2).degrees, false, true)
        val HATCH_HANDOFF = Pose(2.inches, (-15).degrees, 105.degrees, false, false)
        val HATCH_CARRY = Pose(11.inches, (-69).degrees, 1.degrees, false, false)
        val HATCH_INTERMEDIATE = Pose(7.inches, (-40).degrees, 6.degrees, false, false)
        val HATCH_LOW = Pose(0.inches, (-13).degrees, 6.degrees, false)
        val HATCH_MED = Pose(6.inches, 19.degrees, 6.degrees, false)
        val HATCH_HIGH = Pose(25.8.inches, 22.3.degrees, 6.degrees, false)
        val HATCH_FEEDER_PICKUP = Pose(0.inches, (-35).degrees, 6.degrees, false, true)

        val CLIMB_START = Pose(1.inches, 18.degrees, 120.degrees, false, isClimbing = true)
        val LIFTED = Pose((-20).inches, 64.degrees, (-2).degrees, false, isClimbing = true)
        val CLIMB_LIFT_ELEVATOR = Pose(0.inches, 64.degrees, (-2).degrees, false, isClimbing = true)

        val CARGO_GROUND_PICKUP = Pose(0.inches, (-72).degrees, 62.degrees, true, false)
        val CARGO_LOW = Pose(17.inches, (-57).degrees, 6.degrees, true, false)
        val CARGO_MED = Pose(0.inches, (57).degrees, 6.degrees, true, false)
        val CARGO_HIGH = Pose(26.inches, (64).degrees, 6.degrees, true, false)
        val CARGO_SHIP_SCORE = Pose(0.inches, (30).degrees, 6.degrees, true, false)
    }
}
