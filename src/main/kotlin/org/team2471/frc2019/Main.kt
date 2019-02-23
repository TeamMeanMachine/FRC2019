@file:JvmName("Main")

package org.team2471.frc2019

import edu.wpi.first.wpilibj.Compressor
import edu.wpi.first.wpilibj.SerialPort
import edu.wpi.first.wpilibj.Solenoid
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.RobotProgram
import org.team2471.frc.lib.framework.initializeWpilib
import org.team2471.frc.lib.framework.runRobotProgram
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc.lib.units.asDegrees
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.inches
import kotlin.concurrent.thread

object Robot: RobotProgram {
    override suspend fun enable() {
        Armavator.heightSetpoint = Armavator.height
        Armavator.angleSetpoint = Armavator.angle
        OB1.pivotSetpoint = OB1.angle
        Armavator.enable()
        OB1.enable()
        Drive.enable()
    }

    override suspend fun autonomous() {
    }

    override suspend fun teleop() {
        Drive.zeroGyro()
        periodic {
            println("Arm: ${Armavator.angle}, Elevator: ${Armavator.height}, OB1: ${OB1.angle}")
//            println(Pose.current.clawHeight<Pose.SAFETY_POSE.clawHeight)

        }
    }

    override suspend fun test() {
        use(Drive) {
            periodic {
                Drive.drive(Vector2(0.0, 0.2), 0.0)
            }
        }


//        val startingHeight = Armavator.height
//        val startingAngle = Armavator.angle
//        repeat(0) {
//            Armavator.animate(6.0.inches, 0.degrees)
//            Armavator.animate(startingHeight, startingAngle)
//        }
//
//        OB1.animateToAngle(90.0.degrees)
//        OB1.animateToAngle(0.degrees)
    }

    override suspend fun disable() {
        Armavator.disable()
        OB1.disable()
        Drive.disable()

//        periodic {
//            println("Arm: ${Armavator.angle}, Elevator: ${Armavator.height}, OB1: ${OB1.angle}")
//        }
    }
}

fun main() {
    initializeWpilib()

    Drive
    Armavator
    OB1

    runRobotProgram(Robot)
}