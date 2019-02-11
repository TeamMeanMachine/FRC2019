@file:JvmName("Main")

package org.team2471.frc2019

import edu.wpi.first.wpilibj.Compressor
import edu.wpi.first.wpilibj.Solenoid
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.RobotProgram
import org.team2471.frc.lib.framework.initializeWpilib
import org.team2471.frc.lib.framework.runRobotProgram
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc.lib.units.asDegrees
import org.team2471.frc.lib.units.degrees

object Robot: RobotProgram {
    override suspend fun autonomous() {
    }

    override suspend fun teleop() {
        println("Finished")
        periodic {
            // TODO: move this drive thing into Drive.default()
            Drive.drive(OI.driveTranslation, OI.driveRotation)
        }
    }

    override suspend fun test() {
    }

    override suspend fun disable() {
    }
}

fun main() {
    initializeWpilib()

    Drive.enable()
    Armavator.enable()
    OB1.enable()

    runRobotProgram(Robot)
}