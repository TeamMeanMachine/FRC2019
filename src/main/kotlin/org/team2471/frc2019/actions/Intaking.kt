package org.team2471.frc2019.actions

import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.units.seconds
import org.team2471.frc2019.*
import java.util.concurrent.DelayQueue

suspend fun intakeCargo() = use(Armavator) {
    Armavator.isPinching = true // make sure there's no hatch
    Armavator.intake(0.5)
    goToPose(Pose.CARGO_GROUND_PICKUP)
    delay(0.2)
    suspendUntil { println(Armavator.intakeCurrent);Armavator.intakeCurrent > 15.0 }
    goToPose(Pose.HOME)
}

suspend fun intakeHatch() = use(Armavator) {
    Armavator.isPinching = true
    goToPose(Pose.HATCH_FEEDER_PICKUP)
    Armavator.isExtending = true
    suspendUntil { OI.pickupFromFeederStation }
    Armavator.isPinching = false
    delay(0.5)
    Armavator.isExtending = false
    Armavator.intake(-0.2)
    Drive.driveTime(Vector2(0.0, -0.4), 0.75.seconds)
    goToPose(Pose.HOME)
    Armavator.intake(0.0)
}

suspend fun ejectPiece() = use(Armavator) {
    Armavator.isPinching = true
    Armavator.intake(-0.5)
    delay(0.5)
    Armavator.intake(0.0)
}
