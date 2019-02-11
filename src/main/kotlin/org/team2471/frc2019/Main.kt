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
//            Armavator.setArmSetpoint((OI.rightYStick * 45).degrees)
//            Armavator.setArmRaw(OI.rightYStick)
            Drive.drive(OI.driveTranslation, OI.driveRotation)
//            println("Current pos: ${OB1.angle}, Setpoint: ${ob1Setpoint}, Output: ${OB1.pivotMotors.output}, Error: ${OB1.pivotMotors.closedLoopError}, Current: ${OB1.pivotMotors.current}")
//            OB1.pivotRaw(OI.rightYStick * 0.25)
        }
    }

    override suspend fun test() {
//        var setpoint = Armavator.height
        periodic {
//            setpoint += (OI.driveTranslationY * 4).inches * period
//            Armavator.elevate(setpoint)
        println(compressor.pressureSwitchValue)
        }
    }

    private val compressor = Compressor()

    override suspend fun disable() {

        periodic {
//            println(Armavator.angle.asDegrees)
//            println(Armavator.height)
//            println(OB1.pivotMotors.position)
//        println(OB1.angle.asDegrees)
        }
    }
}

fun main() {
    initializeWpilib()

    Drive.enable()
    Armavator.enable()
    OB1.enable()

    runRobotProgram(Robot)
}