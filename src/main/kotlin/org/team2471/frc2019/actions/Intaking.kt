package org.team2471.frc2019.actions

import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.units.inches
import org.team2471.frc.lib.units.seconds
import org.team2471.frc2019.*

suspend fun intakeCargo() = use(Armavator, OB1) {
    OB1.intake(1.0)
    Armavator.intake(0.75)
    goToPose(Pose.CARGO_GROUND_PICKUP)
    delay(0.2)
    println("Armavator current: ${Armavator.intakeCurrent}")
    suspendUntil { println(Armavator.intakeCurrent);Armavator.intakeCurrent > 8.0 }
    Armavator.gamePiece = GamePiece.CARGO
    returnHome()
}

suspend fun intakeHatch() = use(Armavator, OB1) {
    try {
        OB1.intake(0.7)
        goToPose(Pose.HATCH_GROUND_PICKUP)
        delay(0.3)
        suspendUntil { OB1.intakeCurrent > 25.0 } //20.0 for practice
        delay(0.4)
        OB1.intake(0.15)
        goToPose(Pose.HATCH_HANDOFF)
        Armavator.gamePiece = GamePiece.HATCH_PANEL
        delay(0.2)
        OB1.intake(-0.2)
        goToPose(Pose.HATCH_INTERMEDIATE)

        goToPose(Pose.HATCH_CARRY)
    } finally {
        Armavator.isClamping = false
//        withContext(NonCancellable) {
//            returnHome()
    }
//    }
}

suspend fun initialHandoff() = use(Armavator, OB1) {
    OB1.intake(1.0)
    try {
        goToPose(Pose.HATCH_HANDOFF)
        delay(1.0)
        goToPose(Pose.HATCH_CARRY)
    } finally {
        Armavator.isClimbing = false
    }
}

suspend fun pickupFeederStation() {
    use(Armavator, OB1) {
        goToPose(Pose.HATCH_FEEDER_PICKUP)
        suspendUntil { OI.pickupFromFeederStation }
        Armavator.isPinching = false
        Armavator.gamePiece = GamePiece.HATCH_PANEL
        delay(0.5)
//        Drive.driveTime(Vector2(0.0, -0.4), 0.75.seconds)
        returnHome()
    }
}

suspend fun returnHome(resetGamePiece: Boolean = false) = use(Armavator, OB1) {
    if (resetGamePiece) {
        Armavator.gamePiece = null
        goToPose(Pose.HOME)
        return@use
    }

    if (Armavator.gamePiece == GamePiece.HATCH_PANEL) {
        goToPose(Pose.HATCH_CARRY)
    } else {
        goToPose(Pose.HOME)
    }
}

suspend fun ejectPiece() {
    val gamePiece = Armavator.gamePiece ?: return
    use(Armavator, OB1) {
        when (gamePiece) {
            GamePiece.CARGO -> Armavator.intake(-0.7)
            GamePiece.HATCH_PANEL -> Armavator.isPinching = true
        }
        Armavator.gamePiece = null
        delay(0.5)
        returnHome()
    }
}
