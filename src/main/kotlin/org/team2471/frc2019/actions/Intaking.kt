package org.team2471.frc2019.actions

import javafx.geometry.Pos
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.halt
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.use
import org.team2471.frc2019.*

suspend fun intakeCargo(): Nothing = use(Armavator, OB1) {
    OB1.intake(1.0)
    Armavator.intake(0.75)
    goToPose(Pose.CARGO_GROUND_PICKUP)
    try {
        halt()
    } finally {
        println("Intake Cargo finally")
        Armavator.gamePiece = GamePiece.CARGO
        withContext(NonCancellable) {
            goToPose(Pose.HOME)
            Armavator.intake(0.0)
        }
    }
}

suspend fun intakeHatch() = use(Armavator, OB1) {
    OB1.intake(0.7)
    goToPose(Pose.HATCH_GROUND_PICKUP)
    suspendUntil { println(OB1.intakeCurrent); OB1.intakeCurrent > 12.5 } //30.0 for final
    delay(0.35)
    OB1.intake(0.25)
    goToPose(Pose.HATCH_HANDOFF)
    Armavator.gamePiece = GamePiece.HATCH_PANEL
    delay(0.5)
    OB1.intake(-0.2)

    goToPose(Pose.HATCH_CARRY)
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
    println("Current Pose: ${Pose.current}")
    check (Pose.current.closeTo(Pose.HOME))

    use(Armavator, OB1) {
        goToPose(Pose.HATCH_FEEDER_PICKUP)
        suspendUntil { OI.driverController.xButton }
        Armavator.isPinching = false
        Armavator.gamePiece = GamePiece.HATCH_PANEL
        delay(0.5)
        returnHome()
    }
}

suspend fun returnHome() = use(Armavator, OB1) {
    withContext(NonCancellable) {
        if (Armavator.gamePiece == GamePiece.HATCH_PANEL) {
            goToPose(Pose.HATCH_CARRY)
        } else {
            goToPose(Pose.HOME)
        }
    }
}

suspend fun ejectPiece() {
    val gamePiece = Armavator.gamePiece ?: return
    use(Armavator, OB1) {
        when(gamePiece) {
            GamePiece.CARGO -> Armavator.intake(-0.7)
            GamePiece.HATCH_PANEL -> Armavator.isPinching = true
        }
        Armavator.gamePiece = null
        delay(0.5)
        returnHome()
    }
}
