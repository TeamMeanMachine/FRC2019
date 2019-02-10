package org.team2471.frc2019

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.TalonID
import org.team2471.frc.lib.actuators.TalonSRX
import org.team2471.frc.lib.actuators.VictorID
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.DoubleRange
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.Length
import org.team2471.frc.lib.units.asDegrees
import org.team2471.frc.lib.units.inches
import org.team2471.frc2019.Talons.ARM_MASTER
import org.team2471.frc2019.Talons.ELEVATOR_MASTER
import org.team2471.frc2019.Victors.ARM_SLAVE
import org.team2471.frc2019.Victors.ELEVATOR_SLAVE

object Elevator: Subsystem("Elevator") {
    private val elevatorMotors = MotorController(TalonID(ELEVATOR_MASTER), VictorID(ELEVATOR_SLAVE)).config {
        encoderType(FeedbackDevice.Analog)
        feedbackCoefficient = .75 * Math.PI / 1023
        inverted(true)
        pid{
            p(0.001)
        }
    }

    private val armMotors = MotorController(TalonID(ARM_MASTER), VictorID(ARM_SLAVE))

    private val range: DoubleRange = 0.0..15.0 // inches

    val height: Length
        get() = elevatorMotors.position.inches

    init {
        elevatorMotors.position = 0.0
    }

    fun fourBar(angle: Angle) {
        armMotors.setMotionMagicSetpoint(angle.asDegrees)
    }

    fun elevate(height: Length) {
        println("Setpoint:$height, Current: ${this.height}, Power: ${elevatorMotors.output}")
        elevatorMotors.setPositionSetpoint(height.asInches.coerceIn(range))

    }

    fun elevateRaw(power: Double) {
        elevatorMotors.setPercentOutput(power)
    }
}