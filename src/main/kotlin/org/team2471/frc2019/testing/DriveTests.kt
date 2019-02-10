@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.team2471.frc2019.testing

import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.motion.following.steerToAngle
import org.team2471.frc.lib.units.Time
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.seconds
import org.team2471.frc2019.Drive

private suspend fun Drive.Module.testAverageAmperage(
    power: Double,
    rampTime: Time,
    sampleTime: Time,
    numSamples: Int = 10
): Double {
    var accum = 0.0
    try {
        setRampRate(rampTime / power)
        drive(angle, power)
        delay(rampTime / power)

        repeat(numSamples) {
            accum += driveCurrent
            delay(sampleTime / numSamples.toDouble())
        }

        drive(angle, 0.0)
        delay(rampTime / power)
    } finally {
        setRampRate(0.seconds)
    }

    return accum / numSamples
}

suspend fun Drive.steeringTests() = use(this) {
    // front left
    Drive.frontLeftModule.steerToAngle(90.degrees)
    Drive.frontLeftModule.steerToAngle(180.degrees)
    Drive.frontLeftModule.steerToAngle(270.degrees)
    Drive.frontLeftModule.steerToAngle(360.degrees)

    // front right
    Drive.frontRightModule.steerToAngle(90.degrees)
    Drive.frontRightModule.steerToAngle(180.degrees)
    Drive.frontRightModule.steerToAngle(270.degrees)
    Drive.frontRightModule.steerToAngle(360.degrees)

    // back left
    Drive.backLeftModule.steerToAngle(90.degrees)
    Drive.backLeftModule.steerToAngle(180.degrees)
    Drive.backLeftModule.steerToAngle(270.degrees)
    Drive.backLeftModule.steerToAngle(360.degrees)

    // back right
    Drive.backRightModule.steerToAngle(90.degrees)
    Drive.backRightModule.steerToAngle(180.degrees)
    Drive.backRightModule.steerToAngle(270.degrees)
    Drive.backRightModule.steerToAngle(360.degrees)
}

suspend fun Drive.driveTests() = use(this) {
    val flCurrent = Drive.frontLeftModule.testAverageAmperage(0.5, 0.25.seconds, 0.5.seconds)
    val frCurrent = Drive.frontRightModule.testAverageAmperage(0.5, 0.25.seconds, 0.5.seconds)
    val blCurrent = Drive.backLeftModule.testAverageAmperage(0.5, 0.25.seconds, 0.5.seconds)
    val brCurrent = Drive.backRightModule.testAverageAmperage(0.5, 0.25.seconds, 0.5.seconds)

    println("FL: %.3f, FR: %.3f, BL: %.3f, BR: %.3f".format(flCurrent, frCurrent, blCurrent, brCurrent))
}

