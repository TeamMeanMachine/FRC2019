@file:JvmName("Main")

package org.team2471.frc2019

import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.RobotProgram
import org.team2471.frc.lib.framework.initializeWpilib
import org.team2471.frc.lib.framework.runRobotProgram

object Robot: RobotProgram {
    override suspend fun enable() {
        Armavator.heightSetpoint = Armavator.height
        Armavator.angleSetpoint = Armavator.angle
        OB1.angleSetpoint = OB1.angle
        Armavator.enable()
        OB1.enable()
        Drive.enable()
    }

    override suspend fun autonomous() {
        Drive.zeroGyro()
        AutoChooser.autonomous()
    }

    override suspend fun teleop() {
        Drive.zeroGyro()
        periodic {
   //         println("Arm: ${Armavator.angle}, Elevator: ${Armavator.height}, OB1: ${OB1.angle}")
//            println(Pose.current.clawHeight<Pose.SAFETY.clawHeight)
    //        println("BL: = ${Drive.backLeftModule.currDistance}, BR: = ${Drive.backRightModule.currDistance}, FL: = ${Drive.frontLeftModule.currDistance}, FR: = ${Drive.frontRightModule.currDistance},")


        }
    }

    override suspend fun test() {
//        use(Drive) {
//            periodic {
//                Drive.drive(Vector2(0.0, 0.2), 0.0)
//            }
//        }

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


//            println("Arm: ${Armavator.angle}, Elevator: ${Armavator.height}, OB1: ${OB1.angle}")

//        periodic{
//            println("Front Left: = ${Drive.frontLeftModule.angle} " +
//                    "Front Right: = ${Drive.frontRightModule.angle} " +
//                    "Back Left: = ${Drive.backLeftModule.angle} " +
//                    "Back Right: = ${Drive.backRightModule.angle}")
//            println("Arm: ${Armavator.angle}, Elevator: ${Armavator.height}, OB1: ${OB1.angle}")
//            println("BL: = ${Drive.backLeftModule.currDistance}, BR: = ${Drive.backRightModule.currDistance}, FL: = ${Drive.frontLeftModule.currDistance}, FR: = ${Drive.frontRightModule.currDistance},")


//        }

    }
}

fun main() {
    initializeWpilib()

    Drive
    Armavator
    OB1
    OI

//    AutoChooser

    runRobotProgram(Robot)
}