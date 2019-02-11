package org.team2471.frc2019

import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.XboxController
import kotlinx.coroutines.yield
import org.team2471.frc.lib.coroutines.halt
import org.team2471.frc.lib.coroutines.parallel
import org.team2471.frc.lib.framework.*
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.math.deadband
import org.team2471.frc.lib.math.squareWithSign
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.seconds

object OI {
    private val driverController = XboxController(0)
    private val operatorController = XboxController(1)

    val driveTranslationX: Double
        get() = driverController.getRawAxis(0).deadband(0.2).squareWithSign()

    val driveTranslationY: Double
        get() = -driverController.getRawAxis(1).deadband(0.2).squareWithSign()

    val driveTranslation: Vector2
        get() = Vector2(driveTranslationX, driveTranslationY)

    val driveRotation: Double
        get() = (driverController.getRawAxis(4).deadband(0.2)).squareWithSign()


    val leftYStick: Double
        get() = operatorController.getRawAxis(0)

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
            leftBumperToggle { OB1.intakeCargo() }
        }
    }
}