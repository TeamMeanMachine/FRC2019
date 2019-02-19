package org.team2471.frc2019.actions

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.halt
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.use
import org.team2471.frc2019.*

suspend fun intakeCargo() = use(Armavator, OB1) {
    OB1.intake(1.0)
    Animation.HOME_TO_CARGO_GROUND_PICKUP.play()
    try {
        suspendUntil { OB1.intakeCurrent > 15.0  }
        delay(0.5)
        Armavator.gamePiece = GamePiece.CARGO
    } finally {
        withContext(NonCancellable) {
            OB1.intake(0.0)
            Animation.HOME_TO_CARGO_GROUND_PICKUP.reverse().play()
        }
    }
}

suspend fun intakeHatch() = use(Armavator, OB1) {
    OB1.intake(0.7)
    Animation.HOME_TO_HATCH_GROUND_PICKUP.play()
    suspendUntil { OB1.intakeCurrent > 10.0  }
    OB1.intake(0.5)
    Animation.GROUND_PICKUP_TO_HATCH_HANDOFF.play()
    Armavator.gamePiece = GamePiece.HATCH_PANEL
    delay(.5)
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

suspend fun returnHome() = use(Armavator, OB1) {
    if (Armavator.gamePiece == GamePiece.HATCH_PANEL) {
        Animation.CURRENT_TO_HATCH_CARRY
    } else {
        Animation.CURRENT_TO_HOME
    }.play()
}
