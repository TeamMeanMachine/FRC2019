@file:JvmName("Main")

package org.team2471.frc2019

import edu.wpi.first.wpilibj.*
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.halt
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.RobotProgram
import org.team2471.frc.lib.framework.initializeWpilib
import org.team2471.frc.lib.framework.runRobotProgram
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.inches
import org.team2471.frc.lib.units.seconds
import org.team2471.frc2019.actions.intakeHatch
import org.team2471.frc2019.actions.scoreCargoShip
import org.team2471.frc2019.actions.scoreLow
import org.team2471.frc2019.testing.steeringTests
import kotlin.concurrent.thread

val PDP = PowerDistributionPanel()

object Robot: RobotProgram {

    init {
        Drive.zeroGyro()
        Drive.heading = 0.0.degrees
    }

    override suspend fun enable() {
        Armavator.heightSetpoint = Armavator.height
        Armavator.angleSetpoint = Armavator.angle
        if (Armavator.height.asInches < 0.0) Armavator.reset()
        Armavator.enable()
        Drive.enable()
        OB.enable()
        Limelight.enable()
//        Jevois.enable()
    }

    override suspend fun autonomous() {
        Drive.zeroGyro()
//        AutoChooser.autonomous()
        goToPose(Pose.HOME)
        Armavator.isPinching = false
//        intakeHatch()
//        scoreCargoShip()
    }

    override suspend fun teleop() {
        periodic {
   //         println("Arm: ${Armavator.angle}, Elevator: ${Armavator.height}, OB1: ${OB1.angle}")
//            println(Pose.current.clawHeight<Pose.SAFETY_POSE.clawHeight)
    //        println("BL: = ${Drive.backLeftModule.currDistance}, BR: = ${Drive.backRightModule.currDistance}, FL: = ${Drive.frontLeftModule.currDistance}, FR: = ${Drive.frontRightModule.currDistance},")


        }
    }

    override suspend fun test() {
        use(OB) {
            periodic {
                OB.climbDrive(1.0)
            }
        }
//        use(Jevois) {
//            Jevois.isLightEnabled = true
//            halt()
//        }
//        use(Drive) {
//            pe0riodic {
//                Drive.drive(Vector2(0.0, 0.2), 0.0)
//            }
//        }

//        Drive.steeringTests()

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
        Drive.disable()
        OB.disable()
        Limelight.disable()
//        Jevois.disable()

//            if (Jevois.target.isNotEmpty()) println(Jevois.target.joinToString())
//            println("Arm: ${Armavator.angle}, Elevator: ${Armavator.height}, OB1: ${OB1.angle}")
    }
}

fun main() {
    initializeWpilib()

    Drive
    Armavator
//    Jevois
    Limelight
    OB
    OI

    AutoChooser

    runRobotProgram(Robot)
}