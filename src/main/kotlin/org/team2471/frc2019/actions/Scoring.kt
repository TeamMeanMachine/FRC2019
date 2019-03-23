package org.team2471.frc2019.actions

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.parallel
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.input.Controller
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.units.inches
import org.team2471.frc.lib.units.seconds
import org.team2471.frc2019.*

suspend fun scoreLow() = score(ScoringPosition.ROCKET_LOW)
suspend fun scoreMed() = score(ScoringPosition.ROCKET_MED)
suspend fun scoreHigh() = score(ScoringPosition.ROCKET_HIGH)
suspend fun scoreCargoShip() =
    score(ScoringPosition.CARGO_SHIP)

private suspend fun score(position: ScoringPosition) {
//    val gamePiece = Armavator.gamePiece ?: return
    val gamePiece = when (OI.operatorController.dPad) {
        Controller.Direction.LEFT -> GamePiece.CARGO
        Controller.Direction.RIGHT -> GamePiece.HATCH_PANEL
        else -> Armavator.gamePiece ?: GamePiece.CARGO
    }
    use(Armavator, OB1, name = "Score") {
        goToPose(
            when (gamePiece) {
                GamePiece.HATCH_PANEL -> when (position) {
                    ScoringPosition.ROCKET_LOW -> Pose.HATCH_LOW
                    ScoringPosition.ROCKET_MED -> Pose.HATCH_MED
                    ScoringPosition.ROCKET_HIGH -> Pose.HATCH_HIGH
                    ScoringPosition.CARGO_SHIP -> Pose.HATCH_LOW
                }
                GamePiece.CARGO -> when (position) {
                    ScoringPosition.ROCKET_LOW -> Pose.CARGO_LOW
                    ScoringPosition.ROCKET_MED -> Pose.CARGO_MED
                    ScoringPosition.ROCKET_HIGH -> Pose.CARGO_HIGH
                    ScoringPosition.CARGO_SHIP -> Pose.CARGO_SHIP_SCORE
                }
            }
        )

        when (gamePiece) {
            GamePiece.HATCH_PANEL -> {
/*
                var scorePosition: Vector2? = null
                do {
                    if (scorePosition == null) {
                        suspendUntil { OI.ejectPiece }
                        Armavator.isPinching = true
                        scorePosition = Drive.position
                        suspendUntil { !OI.ejectPiece }
                    } else {
                        if (OI.ejectPiece) {
                            scorePosition = null
                            Armavator.isPinching = false
                        }
                    }
                    delay(0.1)
                } while (scorePosition != null && Drive.position.distance(scorePosition) < 1.5)
*/

                suspendUntil { OI.ejectPiece }
                Armavator.isPinching = true
            }
            GamePiece.CARGO -> {
                suspendUntil { OI.ejectPiece }
                Armavator.intake(-0.8)
                Armavator.isPinching = true
                delay(0.2)
            }
        }

        parallel({
            //            Drive.driveDistance((-5).inches, 0.2)
//            Drive.driveTime(Vector2(0.0, -0.4), 0.75.seconds)
            Armavator.gamePiece = null
            val drivePosition = Drive.position
            suspendUntil { Drive.position.distance(drivePosition) > 1.5 }
        }, {
            Armavator.heightSetpoint = Armavator.height - 2.inches
        })
        Armavator.intake(-0.7)
        returnHome()
        Armavator.intake(0.0)
    }
}


private enum class ScoringPosition { ROCKET_LOW, ROCKET_MED, ROCKET_HIGH, CARGO_SHIP }