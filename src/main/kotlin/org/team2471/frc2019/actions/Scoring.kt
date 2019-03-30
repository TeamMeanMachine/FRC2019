package org.team2471.frc2019.actions

import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.parallel
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.input.Controller
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.units.inches
import org.team2471.frc.lib.units.seconds
import org.team2471.frc2019.*
import kotlin.math.abs

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
        else -> if (Armavator.isCarryingHatch) GamePiece.HATCH_PANEL else GamePiece.CARGO
    }

    use(Armavator, name = "Score") {
        val scorePose = when (gamePiece) {
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

        goToPose(scorePose)

        when (gamePiece) {
            GamePiece.HATCH_PANEL -> {
                suspendUntil { OI.ejectPiece }
                Armavator.isExtending = true
                Armavator.isPinching = true
                delay(0.5)
                Armavator.isExtending = false
            }
            GamePiece.CARGO -> {
                suspendUntil { OI.ejectPiece }
                Armavator.intake(OI.driverController.rightTrigger * -0.8)
                delay(0.35)
                Armavator.intake(0.0)
            }
        }

        parallel({
            val placePosition = Drive.position
            val placeHeading = Drive.heading
            Drive.driveTime(Vector2(0.0, -0.3), 0.35.seconds)
            suspendUntil { Drive.position.distance(placePosition) > 1.5 || abs(Drive.heading.asDegrees - placeHeading.asDegrees) > 60.0 }
        }, {
            Armavator.heightSetpoint = Armavator.height - 2.inches
        })
        goToPose(Pose.HOME)
    }
}


private enum class ScoringPosition { ROCKET_LOW, ROCKET_MED, ROCKET_HIGH, CARGO_SHIP }