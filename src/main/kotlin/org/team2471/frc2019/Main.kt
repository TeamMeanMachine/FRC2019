@file:JvmName("Main")

package org.team2471.frc2019

import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.RobotProgram
import org.team2471.frc.lib.framework.initializeWpilib
import org.team2471.frc.lib.framework.runRobotProgram
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc.lib.units.asDegrees
import org.team2471.frc.lib.units.inches
import org.team2471.frc.lib.units.radians
import org.team2471.frc2019.testing.driveTests
import org.team2471.frc2019.testing.steeringTests

object Robot: RobotProgram {
    override suspend fun autonomous() {
    }

    override suspend fun teleop() {
        periodic {
//            Drive.frontLeftModule.drive(OI.driveTranslation.angle.radians, 0.0)
            Drive.drive(OI.driveTranslation, OI.driveRotation)
        }
    }

    override suspend fun test() {
        Drive.steeringTests()
//        var setpoint = Elevator.height
//        periodic {
//            setpoint += (OI.driveTranslationY * 4).inches * period
//            Elevator.elevate(setpoint)
//        }
    }

    override suspend fun disable() {
        println("Disable")
        periodic {
//            println(Elevator.height)
        }
    }
}

fun main() {
    initializeWpilib()

    Drive.enable()
//    Elevator.enable()

    runRobotProgram(Robot)
}