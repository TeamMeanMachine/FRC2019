package org.team2471.frc2019

import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.XboxController
import kotlinx.coroutines.yield
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.halt
import org.team2471.frc.lib.coroutines.parallel
import org.team2471.frc.lib.framework.*
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.math.deadband
import org.team2471.frc.lib.math.squareWithSign
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.inches
import org.team2471.frc.lib.units.seconds

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

    init {
//        operatorController.createMappings {
//            yToggle { OB1.intakeHatch() }
//            bToggle { OB1.intakeCargo() }
//            aHold { OB1.intake(1.0)}
//        }
        driverController.createMappings {
            //            xPress { animateToPose(Pose(0.degrees, 11.inches, 0.degrees)) }
//            bPress { animateToPose(Pose((-74).degrees, 0.inches, 150.degrees)) }
            leftBumperToggle { OB1.intakeCargo() }

            xPress {
                if (OB1.angle < 30.degrees) {
                    Animation.CURRENT_TO_HATCH_CARRY.play()
                } else {
                    println("OB1 is not in a safe place!")
                }
            }
            aPress {
                Animation.HOME_TO_START_CLIMB.play()
            }
            bPress {
                Animation.START_CLIMB_TO_LIFTED.play()
            }

        }

        operatorController.createMappings {
            rightBumperPress {
                Armavator.isPinching = !Armavator.isPinching
            }
            leftBumperPress {
                Armavator.isClamping = !Armavator.isClamping
            }

            xPress {
                use(Armavator, OB1) {
                    Animation.START_TO_HANDOFF.play()
                    delay(1.0)
                    Animation.HANDOFF_TO_HATCH_CARRY.play()
                }
            }
            aPress {
                use(Armavator, OB1) {
                    if (OB1.angle < 30.degrees) {
                        Animation.SCORE_1.play()
                    } else {
                        println("OB1 is not in a safe place!")
                    }
                }
            }
            bPress {
                use(Armavator, OB1) {
                    Animation.SCORE_2.play()
                }
            }
            yPress {
                use(Armavator, OB1) {
                    Animation.SCORE_3.play()
                }
            }


        }
    }
}