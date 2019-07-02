package org.team2471.frc2019

import org.team2471.frc.lib.units.*

data class Pose(val elevatorHeight: Length, val armAngle: Angle) {

    companion object {
        val current: Pose
            get() = Pose(Armavator.height, Armavator.angle)

        //        val STARTING_POSITION = Pose(0.inches, (-74).degrees, 145.degrees, true)
        val HOME = Pose(2.inches, (-74).degrees)
        val HATCH_LOW = Pose((-9).inches, (-51).degrees) //0, -63
        val HATCH_MED = Pose((.2).inches, -(13).degrees) //21.5
        val HATCH_HIGH = Pose(12.inches, 8.degrees) //5
        val HATCH_FEEDER_PICKUP = Pose((-6.5).inches, (-48).degrees)

        val BEFORE_BEFORE_CLIMB = Pose((-8.5).inches, 45.degrees)
        val BEFORE_CLIMB = Pose((-8.5).inches, 20.degrees)
        val BEFORE_CLIMB2 = Pose((-8.5).inches, 20.degrees) //values need to be changed
//        val CLIMB_START = Pose(1.inches, 18.degrees, 120.degrees, false, true,  true)
//        val CLIMB_START2 = Pose(1.inches, 18.degrees, 50.degrees, false, true,  true)
        val LIFTED = Pose((-28.5).inches, 65.degrees) //21.5
        val AFTER_LIFTED = Pose(-8.5.inches, 0.degrees)
        val LIFTED2 = Pose((-17.5).inches, 65.degrees) //21.5 values need to be changed
        val AFTER_LIFTED2 = Pose(-8.5.inches, 0.degrees) // values need to be changed
        val JITB_CLIMB = Pose((-10.0).inches, 16.degrees) /**Double check the height**/
//        val LIFTED2 = Pose((-9.25).inches, 64.degrees, (-3).degrees, false, true, true)
//        val CLIMB_LIFT_ELEVATOR = Pose(0.inches, -17.5.degrees, 30.degrees, false, true, true)

        val CARGO_GROUND_PICKUP = Pose((-10.0).inches, (-59.0).degrees)
        val CARGO_LOW = Pose(7.inches, (-40).degrees)
        val CARGO_MED = Pose((-1.9).inches, (17).degrees)
        val CARGO_HIGH = Pose(15.inches, (42).degrees)
        val CARGO_SHIP_SCORE = Pose((-10.5).inches, (17).degrees)
    }
}
