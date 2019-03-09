package org.team2471.frc2019

import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.*
import org.team2471.frc.lib.input.XboxController
import org.team2471.frc.lib.input.toggleWhenTrue
import org.team2471.frc.lib.input.whenTrue
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.math.deadband
import org.team2471.frc.lib.math.squareWithSign
import org.team2471.frc.lib.units.Length
import org.team2471.frc2019.actions.intakeCargo
import org.team2471.frc2019.actions.intakeHatch

object OI {
    val driverController = XboxController(0)
    val operatorController = XboxController(1)

    private val driveTranslationX: Double
        get() = driverController.leftThumbstickX.deadband(0.125).squareWithSign()

    private val driveTranslationY: Double
        get() = -driverController.leftThumbstickY.deadband(0.125).squareWithSign()

    val driveTranslation: Vector2
        get() = Vector2(driveTranslationX, driveTranslationY)

    val driveRotation: Double
        get() = (driverController.rightThumbstickX.deadband(0.125)).squareWithSign() * 0.5

    val operatorLeftYStick: Double
        get() = -operatorController.leftThumbstickY.deadband(0.15)

    val operatorRightYStick: Double
        get() = -operatorController.rightThumbstickY.deadband(0.15)

    val obiControl: Double
        get() = operatorController.rightTrigger -
                operatorController.leftTrigger

    val ejectPiece: Boolean
        get() = driverController.rightTrigger > 0.5

    val activate: Boolean
        get() = driverController.rightBumper

    val startClimb: Boolean
        get() = driverController.start

    init {
        driverController::leftBumper.toggleWhenTrue { intakeCargo() }
        driverController::rightBumper.toggleWhenTrue { intakeHatch() }

//        operatorController.createMappings {
//            yToggle { OB1.intakeHatch() }
//            bToggle { OB1.intakeCargo() }
//            aHold { OB1.intake(1.0)}
//        }
//        driverController.createMappings {
//            leftBumperToggle { intakeCargo() }
//
////            bPress { ejectPiece() }
//
//            rightBumperToggle { intakeHatch() }
//
//            aPress { pickupFeederStation() }
//
//            backToggle { climb() }
//
//            startPress { Drive.zeroGyro() }
//            startPress { goToPose(Pose.STARTING_POSITION) }
//            aPress { goToPose(Pose.HATCH_LOW) }
//            xPress { goToPose(Pose.HATCH_MED) }
//            yPress { goToPose(Pose.HATCH_HIGH) }
//            bPress { goToPose(Pose.HOME) }
//        }

//        operatorController.createMappings {
//            rightBumperPress {
//                Armavator.isPinching = !Armavator.isPinching
//            }
//            leftBumperPress {
//                Armavator.isClamping = !Armavator.isClamping
//            }
//
//            backPress {
//                Armavator.elevatorMotors.position = 0.0
//            }
//
//            startPress { returnHome() }
//
//            aPress { scoreLow() }
//            bPress { scoreMed() }
//            yPress { scoreHigh() }
//            xPress {
//                scoreCargoShip()
//
////                Animation.CURRENT_TO_HOME.play()
//            }
//        }
    }
}

suspend fun goToHeight(height: Length) = use(Armavator) {
    periodic {
        Armavator.heightSetpoint = height
        if (Math.abs(Armavator.height.asInches - height.asInches) < 3.0) stop()
    }
}
