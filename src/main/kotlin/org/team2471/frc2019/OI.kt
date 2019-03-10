package org.team2471.frc2019

import org.team2471.frc.lib.input.XboxController
import org.team2471.frc.lib.input.toggleWhenTrue
import org.team2471.frc.lib.input.whenTrue
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.math.deadband
import org.team2471.frc.lib.math.squareWithSign
import org.team2471.frc.lib.motion_profiling.Path2D
import org.team2471.frc2019.actions.*

object OI {
    private val driverController = XboxController(0)
    private val operatorController = XboxController(1)

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
        get() = operatorController.rightTrigger - operatorController.leftTrigger

    val rightYStick: Double
        get() = -driverController.rightThumbstickY.deadband(0.2)

    val ejectPiece: Boolean
        get() = driverController.rightTrigger > 0.5

    val activate: Boolean
        get() = driverController.rightBumper

    val startClimb: Boolean
        get() = driverController.start

    val pickupFromFeederStation: Boolean
        get() = driverController.x

    init {
        // owen mappings
        driverController::leftBumper::toggleWhenTrue { intakeCargo() }
        driverController::rightBumper::toggleWhenTrue { intakeHatch() }
        driverController::b.whenTrue { Armavator.gamePiece = null; returnHome() }
        driverController::a.whenTrue { pickupFeederStation() }
        driverController::start.whenTrue { Drive.zeroGyro() }

        driverController::y.whenTrue {
            val position1 = Vector2(0.0, 0.0)
            val tangent1 = Vector2(0.0, 3.0)
            val robotPosition = RobotPosition(Drive.position, Drive.heading)
            val initialPathPoint = robotToField(RobotPathPoint(position1, tangent1), robotPosition)
            val examplePath = Path2D().apply {
                robotDirection = Path2D.RobotDirection.FORWARD
                addPointAndTangent(initialPathPoint.position.x, initialPathPoint.position.y, 0.0, 1.0)
                addPointAndTangent(initialPathPoint.position.x + 3.0, initialPathPoint.position.y + 1.0, 0.0, 4.0)
                addEasePoint(0.0, 0.0)
                addEasePoint(3.0, 1.0)
                addHeadingPoint(0.0, Drive.heading.asDegrees)
                addHeadingPoint(3.0, 45.0)
            }
            driveToTarget()
        }

        // justine mappings
        operatorController::a.whenTrue { scoreLow() }
        operatorController::b.whenTrue { scoreMed() }
        operatorController::x.whenTrue { scoreCargoShip() }
        operatorController::y.whenTrue { scoreHigh() }

//        driverController.createMappings {
//            leftBumperToggle { intakeCargo() }
//
//            rightBumperToggle { intakeHatch() }
//
//            aPress { pickupFeederStation() }
//
//            bPress { goToPose(Pose.HOME) }
//
//            backPress { Drive.zeroGyro() }
//
//            yPress {
//                val position1 = Vector2(0.0, 0.0)
//                val tangent1 = Vector2(0.0, 3.0)
//                val robotPosition = RobotPosition(Drive.position, Drive.heading)
//                val initialPathPoint = robotToField(RobotPathPoint(position1, tangent1), robotPosition)
//                val examplePath = Path2D().apply {
//                    robotDirection = Path2D.RobotDirection.FORWARD
//                    addPointAndTangent(initialPathPoint.position.x, initialPathPoint.position.y, 0.0, 1.0)
//                    addPointAndTangent(initialPathPoint.position.x + 3.0, initialPathPoint.position.y + 1.0, 0.0, 4.0)
//                    addEasePoint(0.0, 0.0)
//                    addEasePoint(3.0, 1.0)
//                    addHeadingPoint(0.0, Drive.heading.asDegrees)
//                    addHeadingPoint(3.0, 45.0)
//                }
//                driveToTarget()
//            }


    }


    // put climb on dpad for safety
    // no idea how to map to dpad button

/*
        Events.whenActive( unit ->  driverController.dPad == Controller.Direction.UP, {
            climb()
        })
*/


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
//                Animation.CURRENT_TO_HOME.play()
//            }
//        }
//    }
}