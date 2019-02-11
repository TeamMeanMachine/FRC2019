package org.team2471.frc2019.testing

import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.testing.testAverageAmperage
import org.team2471.frc.lib.units.inches
import org.team2471.frc.lib.units.seconds
import org.team2471.frc2019.Armavator
import org.team2471.frc2019.smoothDrivePosition

suspend fun Armavator.openLoopTest() = use(this) {
    val upAmperage = elevatorMotors.testAverageAmperage(0.4, 0.25.seconds, 1.0.seconds)
    val downAmperage = elevatorMotors.testAverageAmperage(-0.2, 0.25.seconds, 1.0.seconds)
    println("Up Amperage: $upAmperage, Down Amperage: $downAmperage")
}

suspend fun Armavator.closedLoopTest() = use(this) {
    val startingHeight = height
    smoothDrivePosition(startingHeight + 12.inches, 2.0.seconds)
    smoothDrivePosition(startingHeight, 3.0.seconds)

}
