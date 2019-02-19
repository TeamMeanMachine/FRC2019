package org.team2471.frc2019

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.team2471.frc.lib.coroutines.halt
import org.team2471.frc.lib.framework.use

suspend fun intakeCargo(): Nothing = use(Armavator, OB1) {
    OB1.intake(1.0)
    Animation.HOME_TO_CARGO_GROUND_PICKUP.play()
    try {
        halt()
    } finally {
        withContext(NonCancellable) {
            OB1.intake(0.0)
            Animation.HOME_TO_CARGO_GROUND_PICKUP.reverse().play()
        }
    }
}