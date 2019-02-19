package org.team2471.frc2019.actions

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.team2471.frc.lib.coroutines.halt
import org.team2471.frc.lib.framework.use
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
            println("yeet")
            keyFrames.add(
                KeyFrame(
                    1.5.seconds,
                    Pose.CARGO_SAFETY_POSE
                )
            )
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
    }
}

private enum class ScoringPosition { ROCKET_LOW, ROCKET_MED, ROCKET_HIGH, CARGO_SHIP }