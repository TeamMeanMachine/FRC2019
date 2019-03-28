package org.team2471.frc2019

import org.team2471.frc.lib.units.*

data class Pose(val elevatorHeight: Length, val armAngle: Angle) {

    companion object {
        val current: Pose
            get() = Pose(Armavator.height, Armavator.angle)

        //        val STARTING_POSITION = Pose(0.inches, (-74).degrees, 145.degrees, true)
        val HOME = Pose(0.inches, (-78).degrees)
        val HATCH_LOW = Pose((0).inches, (-63).degrees)
        val HATCH_MED = Pose(17.inches, (-40).degrees)
        val HATCH_HIGH = Pose(5.inches, 48.degrees)
        val HATCH_FEEDER_PICKUP = Pose((-6).inches, (-48).degrees)

        //        val BEFORE_CLIMB = Pose(6.inches, (-74).degrees, 6.degrees, true)
//        val CLIMB_START = Pose(1.inches, 18.degrees, 120.degrees, false, true,  true)
//        val CLIMB_START2 = Pose(1.inches, 18.degrees, 50.degrees, false, true,  true)
        val LIFTED = Pose((-21.5).inches, 64.degrees) //21.5
//        val LIFTED2 = Pose((-9.25).inches, 64.degrees, (-3).degrees, false, true, true)
//        val CLIMB_LIFT_ELEVATOR = Pose(0.inches, -17.5.degrees, 30.degrees, false, true, true)

        val CARGO_GROUND_PICKUP = Pose((-9.5).inches, (-50).degrees)
        val CARGO_LOW = Pose(7.inches, (-40).degrees)
        val CARGO_MED = Pose((0).inches, (32).degrees)
        val CARGO_HIGH = Pose(17.inches, (57).degrees)
        val CARGO_SHIP_SCORE = Pose((-7).inches, (20).degrees)
    }
}
