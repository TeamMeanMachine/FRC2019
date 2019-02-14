package org.team2471.frc2019

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.Solenoid
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.TalonID
import org.team2471.frc.lib.actuators.VictorID
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.DoubleRange
import org.team2471.frc.lib.math.cubicMap
import org.team2471.frc.lib.units.*
import org.team2471.frc.lib.util.Timer
import org.team2471.frc2019.Solenoids.BALL_INTAKE
import org.team2471.frc2019.Solenoids.HATCH_INTAKE
import org.team2471.frc2019.Solenoids.SHIFTER
import org.team2471.frc2019.Talons.ARM_MASTER
import org.team2471.frc2019.Talons.ELEVATOR_MASTER
import org.team2471.frc2019.Victors.ARM_SLAVE
import org.team2471.frc2019.Victors.ELEVATOR_SLAVE

object Armavator : Subsystem("Armavator") {
    private const val ARM_OFFSET = -119.22
    private const val ELEVATOR_FEED_FORWARD = 0.1
    private const val ELEVATOR_HEIGHT = 21.5 //inches
    private const val ARM_LENGTH = 28.0 //inches
    private const val COLLISION_SAFETY_FACTOR = 6.0 //inches

    val elevatorMotors = MotorController(TalonID(ELEVATOR_MASTER), VictorID(ELEVATOR_SLAVE)).config {
        encoderType(FeedbackDevice.Analog)
        feedbackCoefficient = .75 * Math.PI / 1023
        inverted(true)
        pid {
            p(0.001)
        }
        currentLimit(15,0,0)
        brakeMode()
    }

    private val armMotors = MotorController(TalonID(ARM_MASTER), VictorID(ARM_SLAVE)).config {
        encoderType(FeedbackDevice.Analog)
        encoderContinuous(false)
        inverted(true)
        sensorPhase(true)
        feedbackCoefficient = 0.2586
        (ctreController as TalonSRX)
            .sensorCollection
            .setAnalogPosition((ARM_OFFSET / feedbackCoefficient).toInt(), 20)

        pid {
            p(1.0)
        }
        currentLimit(15,0,0)
    }

    private val gearShifter = Solenoid(SHIFTER)
    private val clawSolenoid = Solenoid(BALL_INTAKE)
    private val pinchSolenoid = Solenoid(HATCH_INTAKE)

    private val heightRange: DoubleRange = -0.1..15.0 // inches

    val collisionZone: DoubleRange
        get() = (clawHeight.asInches - COLLISION_SAFETY_FACTOR)..(clawHeight.asInches + COLLISION_SAFETY_FACTOR)

    val height: Length
        get() = elevatorMotors.position.inches

    val angle: Angle
        get() = armMotors.position.degrees + ARM_OFFSET.degrees

    var isClamping: Boolean
        get() = !clawSolenoid.get()
        set(value) = clawSolenoid.set(!value)

    var isPinching: Boolean
        get() = pinchSolenoid.get()
        set(value) = pinchSolenoid.set(value)

    private val clawHeight: Length
        get() = ELEVATOR_HEIGHT.inches + height + (Math.sin(angle.asRadians) * ARM_LENGTH).inches

    init {
        elevatorMotors.position = 0.0
    }

    fun setArmSetpoint(angle: Angle) {
//        println("Setpoint:${angle.asDegrees}, Current: ${this.angle.asDegrees}, Power: ${armMotors.output}")
        armMotors.setPositionSetpoint(angle.asDegrees - ARM_OFFSET)
    }

    fun elevate(height: Length) {
//        println("Setpoint:$height, Current: ${this.height}, Power: ${elevatorMotors.output}")
        elevatorMotors.setPositionSetpoint(height.asInches.coerceIn(heightRange))
    }

    fun elevateRaw(power: Double) {
        elevatorMotors.setPercentOutput(power + ELEVATOR_FEED_FORWARD)
    }

    fun setArmRaw(power: Double) {
        armMotors.setPercentOutput(power)
    }

    override suspend fun default() {
        var armSetpoint = angle
        var elevatorSetpoint = height
        periodic {
            armSetpoint += (OI.operatorRightYStick * 50.0 * period).degrees
            elevatorSetpoint += (OI.operatorLeftYStick * 7 * period).inches
            elevate(elevatorSetpoint)
            setArmSetpoint(armSetpoint)
            isPinching = OI.operatorController.getBumper(GenericHID.Hand.kRight)
            isClamping = !OI.operatorController.getBumper(GenericHID.Hand.kLeft)
            val clawHeight = clawHeight
            val collisionZone = collisionZone
            println(clawHeight)
        }
    }
}

suspend fun Armavator.smoothDrivePosition(height: Length, time: Time) = use(this) {
    val timer = Timer()
    timer.start()
    periodic {
        val position = cubicMap(0.0, time.asSeconds, Armavator.height.asInches, height.asInches, timer.get()).inches
        elevate(position)

        if (timer.get().seconds >= time) {
            stop()
        }
    }
}
