@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.team2471.frc2019.testing

import kotlinx.coroutines.withTimeout
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.asDegrees
import org.team2471.frc.lib.units.degrees
import org.team2471.frc2019.Drive
import kotlin.math.absoluteValue

private const val TOLERANCE = 2.0 // degrees

private suspend fun Drive.steerToAngles(
    frontLeftAngle: Angle,
    frontRightAngle: Angle,
    backLeftAngle: Angle,
    backRightAngle: Angle
) = use(Drive) {
    withTimeout(2000) {
        periodic {
            driveOpenLoop(
                0.0, 0.0, 0.0, 0.0,
                frontLeftAngle, frontRightAngle, backLeftAngle, backRightAngle
            )

            val frontLeftError = frontLeftAngle - Drive.frontLeftAngle
            val frontRightError = frontRightAngle - Drive.frontRightAngle
            val backLeftError = backLeftAngle - Drive.backLeftAngle
            val backRightError = backRightAngle - Drive.backRightAngle

            if (
                frontLeftError.asDegrees.absoluteValue < TOLERANCE &&
                frontRightError.asDegrees.absoluteValue < TOLERANCE &&
                backLeftError.asDegrees.absoluteValue < TOLERANCE &&
                backRightError.asDegrees.absoluteValue < TOLERANCE
            ) {
                stop()
            }
        }
    }
    delay(0.5)
}

suspend fun Drive.turnTest() = use(this) {
    // reset positions
    steerToAngles(0.degrees, 0.degrees,0.degrees,0.degrees)

    // front left
    steerToAngles(90.degrees, 0.degrees,0.degrees,0.degrees)
    steerToAngles(180.degrees, 0.degrees,0.degrees,0.degrees)
    steerToAngles(270.degrees, 0.degrees,0.degrees,0.degrees)
    steerToAngles(360.degrees, 0.degrees,0.degrees,0.degrees)

    // front right
    steerToAngles(0.degrees, 90.degrees,0.degrees,0.degrees)
    steerToAngles(0.degrees, 180.degrees,0.degrees,0.degrees)
    steerToAngles(0.degrees, 270.degrees,0.degrees,0.degrees)
    steerToAngles(0.degrees, 360.degrees,0.degrees,0.degrees)

    // back left
    steerToAngles(0.degrees, 0.degrees,90.degrees,0.degrees)
    steerToAngles(0.degrees, 0.degrees,180.degrees,0.degrees)
    steerToAngles(0.degrees, 0.degrees,270.degrees,0.degrees)
    steerToAngles(0.degrees, 0.degrees,360.degrees,0.degrees)

    // back right
    steerToAngles(0.degrees, 0.degrees,0.degrees,90.degrees)
    steerToAngles(0.degrees, 0.degrees,0.degrees,180.degrees)
    steerToAngles(0.degrees, 0.degrees,0.degrees,270.degrees)
    steerToAngles(0.degrees, 0.degrees,0.degrees,360.degrees)
}