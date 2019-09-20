@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.team2471.frc2019.testing

import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.motion.following.steerToAngle
import org.team2471.frc.lib.testing.testAverageAmperage
import org.team2471.frc.lib.units.Time
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.seconds
import org.team2471.frc2019.Drive

suspend fun Drive.steeringTests() = use(this) {

    for (i in 0..3) {
        for (j in 1..4) {
            Drive.modules[i].angleSetpoint = (j*90).degrees
            delay(0.75)
        }
    }

//        for (i in 0..3) {
//        for (j in 1..4) {
//            Drive.modules[i].angleSetpoint = (j * 90.0).degrees
//            delay(0.75)
//        }
//    }

}

suspend fun Drive.driveTests() = use(this) {
 /*   val flCurrent = Drive.frontLeftModule.driveMotor.testAverageAmperage(0.5, 0.25.seconds, 0.5.seconds)
    val frCurrent = Drive.frontRightModule.driveMotor.testAverageAmperage(0.5, 0.25.seconds, 0.5.seconds)
    val blCurrent = Drive.backLeftModule.driveMotor.testAverageAmperage(0.5, 0.25.seconds, 0.5.seconds)
    val brCurrent = Drive.backRightModule.driveMotor.testAverageAmperage(0.5, 0.25.seconds, 0.5.seconds)

    println("FL: %.3f, FR: %.3f, BL: %.3f, BR: %.3f".format(flCurrent, frCurrent, blCurrent, brCurrent))*/
}

