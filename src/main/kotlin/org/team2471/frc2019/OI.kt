package org.team2471.frc2019

import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.XboxController
import org.team2471.frc.lib.framework.*
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.math.deadband
import org.team2471.frc.lib.math.squareWithSign
import org.team2471.frc2019.actions.*

object OI {
    val driverController = XboxController(0)
    val operatorController = XboxController(1)

    val driveTranslationX: Double
        get() = driverController.getRawAxis(0).deadband(0.125).squareWithSign()

    val driveTranslationY: Double
        get() = -driverController.getRawAxis(1).deadband(0.125).squareWithSign()

    val driveTranslation: Vector2
        get() = Vector2(driveTranslationX, driveTranslationY)

    val driveRotation: Double
        get() = (driverController.getRawAxis(4).deadband(0.125)).squareWithSign() * 0.5

    val operatorLeftYStick: Double
        get() = -operatorController.getRawAxis(1).deadband(0.15)

    val operatorRightYStick: Double
        get() = -operatorController.getRawAxis(5).deadband(0.15)

    val obiControl: Double
        get() = operatorController.getTriggerAxis(GenericHID.Hand.kRight) -
                operatorController.getTriggerAxis(GenericHID.Hand.kLeft)

    val rightYStick: Double
        get() = -driverController.getRawAxis(5).deadband(0.2)

    val ejectPiece: Boolean
        get() = driverController.getTriggerAxis(GenericHID.Hand.kRight) > 0.5

    val activate: Boolean
        get() = driverController.getBumper(GenericHID.Hand.kRight)

    val startClimb: Boolean
        get() = driverController.startButton

    init {
//        operatorController.createMappings {
//            yToggle { OB1.intakeHatch() }
//            bToggle { OB1.intakeCargo() }
//            aHold { OB1.intake(1.0)}
//        }
        driverController.createMappings {
            leftBumperToggle { intakeCargo() }

            rightBumperToggle { intakeHatch() }

            aPress { pickupFeederStation() }

            bPress { forceReset() }

            backPress { Drive.zeroGyro() }

            yPress { climb() }  // put climb on dpad for safety
        }

        operatorController.createMappings {
            rightBumperPress {
                Armavator.isPinching = !Armavator.isPinching
            }
            leftBumperPress {
                Armavator.isClamping = !Armavator.isClamping
            }

            backPress {
                Armavator.elevatorMotors.position = 0.0
            }

            startPress { returnHome() }

            aPress { scoreLow() }
            bPress { scoreMed() }
            yPress { scoreHigh() }
            xPress {
                scoreCargoShip()

//                Animation.CURRENT_TO_HOME.play()
            }
        }
    }
}