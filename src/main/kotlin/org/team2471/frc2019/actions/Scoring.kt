package org.team2471.frc2019.actions

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.halt
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc.lib.units.seconds
import org.team2471.frc2019.*

suspend fun scoreLow() = score(ScoringPosition.ROCKET_LOW)
suspend fun scoreMed() = score(ScoringPosition.ROCKET_MED)
suspend fun scoreHigh() = score(ScoringPosition.ROCKET_HIGH)
suspend fun scoreCargoShip() =
    score(ScoringPosition.CARGO_SHIP)

private suspend fun score(position: ScoringPosition) {
    val gamePiece = Armavator.gamePiece ?: return
    use(Armavator, OB1) {
        val keyFrames = mutableListOf<KeyFrame>()

        keyFrames.add(KeyFrame(0.seconds, Pose.current))
        if (Pose.current.clawHeight < Pose.SAFETY_POSE.clawHeight && gamePiece == GamePiece.CARGO) {
            keyFrames.add(KeyFrame(1.5.seconds, Pose.CARGO_SAFETY_POSE))
        } else if (gamePiece == GamePiece.HATCH_PANEL &&
            (position == ScoringPosition.ROCKET_LOW || position == ScoringPosition.CARGO_SHIP)) {
            keyFrames.add(KeyFrame( 1.seconds, Pose.SAFETY_POSE))
            keyFrames.add(KeyFrame(1.5.seconds, Pose.HATCH_INTERMEDIATE))
        }
        keyFrames.add(
            KeyFrame(
                keyFrames.last().time + 1.seconds, when (position) {
                    ScoringPosition.ROCKET_LOW -> when (gamePiece) {
                        GamePiece.CARGO -> Pose.CARGO_LOW
                        GamePiece.HATCH_PANEL -> Pose.HATCH_LOW
                    }
                    ScoringPosition.ROCKET_MED -> when (gamePiece) {
                        GamePiece.CARGO -> Pose.CARGO_MED
                        GamePiece.HATCH_PANEL -> Pose.HATCH_MED
                    }

                    ScoringPosition.ROCKET_HIGH -> when (gamePiece) {
                        GamePiece.CARGO -> Pose.CARGO_HIGH
                        GamePiece.HATCH_PANEL -> Pose.HATCH_HIGH
                    }
                    ScoringPosition.CARGO_SHIP -> when (gamePiece) {
                        GamePiece.CARGO -> Pose.CARGO_SHIP_SCORE
                        GamePiece.HATCH_PANEL -> Pose.HATCH_LOW
                    }
                }
            )
        )

        Animation(*keyFrames.toTypedArray()).play()
        suspendUntil { OI.ejectPiece }
        if (gamePiece == GamePiece.HATCH_PANEL)  {
            Armavator.isPinching = true
            Armavator.intake(-0.5)
            delay(0.5)
        } else {
            Armavator.isPinching = true
            Armavator.intake(-0.5)
            delay(0.5)
        }
        Armavator.gamePiece = null
        delay(0.5)
        returnHome()
        Armavator.intake(0.0)
    }
}

private enum class ScoringPosition { ROCKET_LOW, ROCKET_MED, ROCKET_HIGH, CARGO_SHIP }