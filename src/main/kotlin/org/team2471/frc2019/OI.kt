package org.team2471.frc2019

import org.team2471.frc.lib.input.*
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.math.cube
import org.team2471.frc.lib.math.deadband
import org.team2471.frc.lib.math.squareWithSign
import org.team2471.frc.lib.motion_profiling.Path2D
import org.team2471.frc2019.actions.*

private val deadBandDriver = 0.05
private val deadBandOperator = 0.150

object OI {
    val driverController = XboxController(0)
    val operatorController = XboxController(1)

    private val driveTranslationX: Double
        get() = driverController.leftThumbstickX.deadband(deadBandDriver).squareWithSign()

    private val driveTranslationY: Double
        get() = -driverController.leftThumbstickY.deadband(deadBandDriver).squareWithSign()

    val driveTranslation: Vector2
        get() = Vector2(driveTranslationX, driveTranslationY)

    val driveRotation: Double
        get() = (driverController.rightThumbstickX.deadband(deadBandDriver)).cube() * 0.5

    val operatorTranslation: Vector2
        get() = Vector2(operatorLeftXStick, operatorLeftYStick) * 0.5

    val operatorRotation: Double
        get() = operatorRightXStick.squareWithSign() * 0.25

    val operatorLeftXStick: Double
        get() = operatorController.leftThumbstickX.deadband(deadBandOperator)

    val operatorLeftYStick: Double
        get() = -operatorController.leftThumbstickY.deadband(deadBandOperator)

    val operatorRightXStick: Double
        get() = operatorController.rightThumbstickX.deadband(deadBandOperator)

    val operatorRightYStick: Double
        get() = -operatorController.rightThumbstickY.deadband(deadBandOperator)

    val obiControl: Double
        get() = operatorController.rightTrigger - operatorController.leftTrigger

    val rightYStick: Double
        get() = -driverController.rightThumbstickY.deadband(deadBandOperator)

    val ejectPiece: Boolean
        get() = driverController.rightTrigger > 0.5

    val activate: Boolean
        get() = driverController.rightBumper

    val startClimb: Boolean
        get() = driverController.start

    val pickupFromFeederStation: Boolean
        get() = driverController.x

    val rightTriggerDown: Boolean
        get() = operatorController.rightTrigger > 0.2

    init {
        // owen mappings
        driverController::leftBumper::toggleWhenTrue { intakeCargo() }
        driverController::rightBumper::toggleWhenTrue { intakeHatch() }
        driverController::b.whenTrue { goToPose(Pose.HOME) }
        driverController::back.whenTrue { Drive.zeroGyro() }

//        driverController::y.whenTrue {
//            val position1 = Vector2(0.0, 0.0)
//            val tangent1 = Vector2(0.0, 3.0)
//            val robotPosition = RobotPosition(Drive.position, Drive.heading)
//            val initialPathPoint = robotToField(RobotPathPoint(position1, tangent1), robotPosition)
//            val examplePath = Path2D().apply {
//                robotDirection = Path2D.RobotDirection.FORWARD
//                addPointAndTangent(initialPathPoint.position.x, initialPathPoint.position.y, 0.0, 1.0)
//                addPointAndTangent(initialPathPoint.position.x + 3.0, initialPathPoint.position.y + 1.0, 0.0, 4.0)
//                addEasePoint(0.0, 0.0)
//                addEasePoint(3.0, 1.0)
//                addHeadingPoint(0.0, Drive.heading.asDegrees)
//                addHeadingPoint(3.0, 45.0)
//            }
//            driveToTarget()
//        }

        // justine mappings
        operatorController::a.whenTrue { scoreLow() }
        operatorController::b.whenTrue { scoreMed() }
        operatorController::x.whenTrue { scoreCargoShip() }
        operatorController::y.whenTrue { scoreHigh() }
        operatorController::leftBumper.whenTrue{ Armavator.toggleExtention()}
        operatorController::rightBumper.whenTrue{ Armavator.togglePinching()}
        ({ operatorController.dPad == Controller.Direction.UP }).whenTrue { climb() }
        ({operatorController.dPad == Controller.Direction.DOWN}).whenTrue{ climb2() }

        operatorController::back.whileTrue{ driveToTarget() }
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
//                Armavator.isExtending = !Armavator.isExtending
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