package org.team2471.frc2019

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import edu.wpi.first.networktables.NetworkTableInstance
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.TalonID
import org.team2471.frc.lib.actuators.VictorID
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.degrees
import org.team2471.frc2019.Talons.OB_PIVOT_LEFT
import org.team2471.frc2019.Talons.OB_PIVOT_RIGHT
import org.team2471.frc2019.Victors.OB_CLIMB_ROLLERS

private const val PIVOT_F = 50.0

object OB : Subsystem("OB") {
    private val table = NetworkTableInstance.getDefault().getTable(name)
    private val leftPivotMotor = MotorController(TalonID(OB_PIVOT_LEFT)).config {
        encoderType(FeedbackDevice.Analog)
        encoderContinuous(false)
        sensorPhase(true)
        feedbackCoefficient = 1/2.6
        rawOffset(750)
        pid {
            p(8.0)
            d(8.0)
            f(PIVOT_F)
        }
    }
    private val rightPivotMotor = MotorController(TalonID(OB_PIVOT_RIGHT)).config {
        encoderType(FeedbackDevice.Analog)
        encoderContinuous(false)
        inverted(true)
        sensorPhase(true)
        feedbackCoefficient = 1/2.6
        rawOffset(-250 )
        pid {
            p(8.0)
            d(8.0)
            f(PIVOT_F)
        }
    }

    private val climbDriveMotors = MotorController(VictorID(OB_CLIMB_ROLLERS))

    val leftAngle
        get() = leftPivotMotor.position.degrees

    val rightAngle
        get() = rightPivotMotor.position.degrees

    var angleSetpoint: Angle = (leftAngle + rightAngle) / 2.0
        set(value) {
            leftPivotMotor.setPositionSetpoint(value.asDegrees)
            rightPivotMotor.setPositionSetpoint(value.asDegrees)
        }

    init {
        GlobalScope.launch(MeanlibDispatcher) {
            val leftArmTable = table.getEntry("Left Arm Angle")
            val rightArmTable = table.getEntry("Right Arm Angle")
            periodic {
                leftArmTable.setDouble(leftAngle.asDegrees)
                rightArmTable.setDouble(rightAngle.asDegrees)

            }
        }
    }

    fun climbDrive(power: Double) {
        climbDriveMotors.setPercentOutput(power)
    }

    fun pivotRaw(power: Double)  {
        rightPivotMotor.setPercentOutput(power)
        leftPivotMotor.setPercentOutput(power)
    }
    override fun reset() {
        climbDriveMotors.stop()
    }
}