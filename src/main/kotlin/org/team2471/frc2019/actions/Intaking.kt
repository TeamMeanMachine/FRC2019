package org.team2471.frc2019.actions

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
    Animation.HOME_TO_CARGO_GROUND_PICKUP.play()
    try {
        halt()
    } finally {
        Armavator.gamePiece = GamePiece.CARGO
        withContext(NonCancellable) {
            OB1.intake(0.0)
            Animation.HOME_TO_CARGO_GROUND_PICKUP.reverse().play()
            Armavator.intake(0.0)
        }
    }
}

suspend fun intakeHatch() = use(Armavator, OB1) {
    OB1.intake(0.7)
    Animation.HOME_TO_HATCH_GROUND_PICKUP.play()
    suspendUntil { println(OB1.intakeCurrent); OB1.intakeCurrent > 12.5 } //30.0 for final
    delay(0.35)
    OB1.intake(0.25)
    Animation.GROUND_PICKUP_TO_HATCH_HANDOFF.play()
    Armavator.gamePiece = GamePiece.HATCH_PANEL
    delay(0.5)
    OB1.intake(-0.2)

    Animation.HANDOFF_TO_HATCH_CARRY.play()
}

suspend fun initialHandoff() = use(Armavator, OB1) {
    OB1.intake(1.0)
    try {
        Animation.START_TO_HANDOFF.play()
        delay(1.0)
        Animation.HANDOFF_TO_HATCH_CARRY.play()
    } finally {
        Armavator.isClimbing = false
    }
}

suspend fun pickupFeederStation() {
    println("Current Pose: ${Pose.current}")
    check (Pose.current.closeTo(Pose.HOME))

    use(Armavator, OB1) {
        Animation.HOME_TO_FEEDER_STATION.play()
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
            Animation.CURRENT_TO_HATCH_CARRY
        } else {
            Animation.CURRENT_TO_HOME
        }.play()
    }
}

suspend fun forceReset() {
    use(Armavator, OB1) {
        when(Armavator.gamePiece) {
            GamePiece.CARGO -> {
                Armavator.intake(-0.7)
                delay(0.5)
            }
            GamePiece.HATCH_PANEL -> {
                Armavator.isPinching = true
                delay(0.5)
            }
        }
        Armavator.gamePiece = null
        returnHome()
    }
}
