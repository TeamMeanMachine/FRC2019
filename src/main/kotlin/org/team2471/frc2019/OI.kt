package org.team2471.frc2019

import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.XboxController
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.*
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.math.deadband
import org.team2471.frc.lib.math.squareWithSign
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.util.Timer
import org.team2471.frc2019.actions.*

object OI {
    val driverController = XboxController(0)
    val operatorController = XboxController(1)

    val driveTranslationX: Double
        get() = driverController.getRawAxis(0).deadband(0.2).squareWithSign()

    val driveTranslationY: Double
        get() = -driverController.getRawAxis(1).deadband(0.2).squareWithSign()

    val driveTranslation: Vector2
        get() = Vector2(driveTranslationX, driveTranslationY)

    val driveRotation: Double
        get() = (driverController.getRawAxis(4).deadband(0.2)).squareWithSign()


    val operatorLeftYStick: Double
        get() = -operatorController.getRawAxis(1).deadband(0.15)

    val operatorRightYStick: Double
        get() = -operatorController.getRawAxis(5).deadband(0.15)

    val rightYStick: Double
        get() = -driverController.getRawAxis(5).deadband(0.2)


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

            rightBumperToggle{ intakeHatch() }
            bPress {
                Animation.START_CLIMB_TO_LIFTED.play()

                val timer = Timer()
                timer.start()
                periodic {
                    if (timer.get() >= 2.5) return@periodic stop()
                    OB1.intake(-1.0)
                    Drive.drive(Vector2(0.0, 0.8), 0.0, false)
                }
//               Animation.GROUND_PICKUP_TO_HATCH_HANDOFF.play()
            }
            xPress {
                if (OB1.angle < 30.degrees) {
                    Animation.CURRENT_TO_HATCH_CARRY.play()
                } else {
                    println("OB1 is not in a safe place!")
                }
            }
            yPress {
                Animation.HOME_TO_CARGO_GROUND_PICKUP.play()
            }
            backToggle { climb() }

            startPress{Drive.zeroGyro()}

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