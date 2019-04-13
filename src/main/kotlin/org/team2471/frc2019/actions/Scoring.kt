package org.team2471.frc2019.actions

import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.input.Controller
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.units.seconds
import org.team2471.frc2019.*
import kotlin.math.abs

suspend fun scoreLow() = score(ScoringPosition.ROCKET_LOW, false)
suspend fun scoreMed() = score(ScoringPosition.ROCKET_MED, false)
suspend fun scoreHigh() = score(ScoringPosition.ROCKET_HIGH, false)
suspend fun scoreCargoShip() = score(ScoringPosition.CARGO_SHIP, false)
suspend fun autoScoreHigh() = score(ScoringPosition.ROCKET_HIGH, true)

private suspend fun score(position: ScoringPosition, isAuto: Boolean) {
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

        if (gamePiece == GamePiece.CARGO) Armavator.intake(Armavator.HOLDING_INTAKE_POWER)

        goToPose(scorePose)

        when (gamePiece) {
            GamePiece.HATCH_PANEL -> {
//                suspendUntil { Math.abs(Armavator.angleSetpoint.asDegrees - Armavator.angle.asDegrees) < 2.0 }
                suspendUntil {
                    //This is the best line of code I have ever written.
                    //-Justine
//                    ((Limelight.isAtTarget(position) && OI.driverController.leftTrigger > 0.2) || OI.usePiece) ||
//                            (isAuto && (Limelight.area > ((if (position == ScoringPosition.ROCKET_MED) Limelight.MED_HATCH_AREA else 8.3))))
                    //don't think about it too hard
                    OI.driverController.rightTrigger > 0.2 /*|| (OI.driverController.leftTrigger > 0.2 && Limelight.isAtTarget(position)) */
                }
                Armavator.isExtending = true
                Armavator.isPinching = true
                delay(0.5)
                Armavator.isExtending = false
            }
            GamePiece.CARGO -> {
                Armavator.isCarryingBall = true
                suspendUntil { OI.usePiece }
                val placePosition = Drive.position
                Armavator.isCarryingBall = false

                periodic {
                    Armavator.intake(OI.driverController.rightTrigger * if (position == ScoringPosition.CARGO_SHIP) -0.6 else -1.0)

                    if (Drive.position.distance(placePosition) > 0.5) stop()
                }
                Armavator.intake(0.0)

            }
        }

        val placePosition = Drive.position
        val placeHeading = Drive.heading
        if (!isAuto) {
            Drive.driveTime(Vector2(0.0, -0.3), 0.35.seconds)
            suspendUntil { Drive.position.distance(placePosition) > 1.5 || abs(Drive.heading.asDegrees - placeHeading.asDegrees) > 60.0 }
            goToPose(Pose.HOME)
        }
    }
}

suspend fun placeHatch() {
//    val gamePiece = Armavator.gamePiece ?: return
    use(Armavator, name = "Score") {
        Armavator.isExtending = true
        Armavator.isPinching = true
        delay(0.5)
        Armavator.isExtending = false

    }
}


enum class ScoringPosition { ROCKET_LOW, ROCKET_MED, ROCKET_HIGH, CARGO_SHIP }