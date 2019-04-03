package org.team2471.frc2019

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.TalonID
import org.team2471.frc.lib.actuators.VictorID
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
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
import org.team2471.frc2019.Victors.ARM_INTAKE
import org.team2471.frc2019.Victors.ARM_SLAVE
import org.team2471.frc2019.Victors.ELEVATOR_SLAVE
import kotlin.math.min


object Armavator : Subsystem("Armavator") {
    private const val ARM_OFFSET = -119.22
    private const val ELEVATOR_FEED_FORWARD = 0.0
    const val ELEVATOR_HEIGHT = 21.5 //inches
    const val ARM_LENGTH = 28.0 //inches

    private const val ELEVATOR_VELOCITY = 50.0
    private const val ELEVATOR_ACCELERATION = 120.0
    private const val ELEVATOR_CLIMB_VELOCITY = 10.0
    private const val ELEVATOR_CLIMB_ACCELERATION = 10.0

    val elevatorMotors = MotorController(TalonID(ELEVATOR_MASTER), VictorID(ELEVATOR_SLAVE)).config {
        encoderType(FeedbackDevice.Analog)
        feedbackCoefficient = .75 * Math.PI / 1023
        inverted(true)
        pid(0) {
            p(0.0012)
            f(0.004)
            motionMagic(ELEVATOR_ACCELERATION, ELEVATOR_VELOCITY)
        }

        currentLimit(25, 0, 0)
    }

    private val armMotors = MotorController(TalonID(ARM_MASTER), VictorID(ARM_SLAVE)).config {
        encoderType(FeedbackDevice.Analog)
        encoderContinuous(false)
        inverted(true)
        sensorPhase(true)
        brakeMode()
        feedbackCoefficient = 0.2586
        (ctreController as TalonSRX)
            .sensorCollection
            .setAnalogPosition((ARM_OFFSET / feedbackCoefficient).toInt(), 20)

        pid {
            p(2.0)
            d(1.0)

            f(7.0)

            motionMagic(360.0, 120.0)
        }
        currentLimit(15, 0, 0)
    }

    private val intakeMotors = MotorController(VictorID(ARM_INTAKE)).config {
        inverted(true)
    }

    private val gearShifter = Solenoid(SHIFTER)
    private val extensionSolenoid = Solenoid(BALL_INTAKE)
    private val pinchSolenoid = Solenoid(HATCH_INTAKE)

    private val table = NetworkTableInstance.getDefault().getTable(name)

    var isClimbing = false
        set(value) {
            if (value != isClimbing) elevatorMotors.config(0) {
                pid {
                    if (value) {
                        motionMagic(ELEVATOR_CLIMB_ACCELERATION, ELEVATOR_CLIMB_VELOCITY)
                    } else {
                        motionMagic(ELEVATOR_ACCELERATION, ELEVATOR_VELOCITY)
                    }
                }
            }
            gearShifter.set(value)
            field = value
        }

    private val heightRange: DoubleRange
        get() = if (!isClimbing) min(
            Pose.CARGO_GROUND_PICKUP.elevatorHeight.asInches,
            height.asInches
        )..18.0 else Pose.LIFTED.elevatorHeight.asInches..26.0// inches

    private val armRange: DoubleRange = -77.0..76.0 // degrees

    private var elevatorOffset = 0.inches

    val height: Length
        get() = elevatorMotors.position.inches

    val angle: Angle
        get() = armMotors.position.degrees + ARM_OFFSET.degrees

    val intakeCurrent: Double
        get() = PDP.getCurrent(Victors.ARM_INTAKE)

    val isCarryingHatch: Boolean
        get() = !isPinching

    var isExtending: Boolean
        get() = extensionSolenoid.get()
        set(value) {
            extensionSolenoid.set(value)
        }

    var isPinching: Boolean
        get() = pinchSolenoid.get()
        set(value) = pinchSolenoid.set(value)

    var angleSetpoint: Angle = angle
        set(value) {
            table.getEntry("Arm Error").setDouble(armMotors.closedLoopError)
            table.getEntry("Arm Output").setDouble(armMotors.output)

            field = value.asDegrees.coerceIn(armRange).degrees
        }

    var heightSetpoint: Length = height
        set(value) {
//            println("Height: $value from ${Thread.currentThread().stackTrace.drop(2).first()}")
            table.getEntry("Elevator Error").setDouble(elevatorMotors.closedLoopError)
            table.getEntry("Elevator Output").setDouble(elevatorMotors.output)

            field = (value.asInches + elevatorOffset.asInches).coerceIn(heightRange).inches
        }

    init {
        elevatorMotors.position = 0.0
        GlobalScope.launch(MeanlibDispatcher) {
            val heightEntry = table.getEntry("Height")
            val angleEntry = table.getEntry("Angle")
            val heightSetpointEntry = table.getEntry("Height Setpoint")
            val angleSetpointEntry = table.getEntry("Angle Setpoint")
            periodic {
                heightEntry.setDouble(height.asInches)
                angleEntry.setDouble(angle.asDegrees)
                heightSetpointEntry.setDouble(heightSetpoint.asInches)
                angleSetpointEntry.setDouble(angleSetpoint.asDegrees)

                if (DriverStation.getInstance().isEnabled) {
                    armMotors.setMotionMagicSetpoint((angleSetpoint.asDegrees - ARM_OFFSET))
                    elevatorMotors.setMotionMagicSetpoint(heightSetpoint.asInches, ELEVATOR_FEED_FORWARD)
                } else {
                    armMotors.stop()
                    elevatorMotors.stop()
                }
            }
        }
    }

    fun intake(power: Double) {
        intakeMotors.setPercentOutput(power)
    }

    fun elevateRaw(power: Double) {
        elevatorMotors.setPercentOutput(power + ELEVATOR_FEED_FORWARD)
    }

    fun setArmRaw(power: Double) {
        armMotors.setPercentOutput(power)
    }

    fun incrementOffset()  {
        elevatorOffset += 0.5.inches
    }

    fun decrementOffset()  {
        elevatorOffset -= 0.5.inches
    }

    fun printDebugInfo() {
        println("Arm Angle: %.3f\tArm Setpoint: %.3f".format(angle.asDegrees, angleSetpoint.asDegrees))
        println("Elevator Height: %.3f\tElevator Setpoint: %.3f".format(height.asInches, heightSetpoint.asInches))
    }

    fun zero() {
        elevatorMotors.position = 0.0
    }

    override fun reset() {
        isClimbing = false
        intake(0.0)
    }
}

suspend fun Armavator.smoothDrivePosition(height: Length, time: Time = 1.5.seconds) = use(this) {
    //TODO: do a smarter time default later
    val timer = Timer()
    timer.start()
    periodic {
        val position = cubicMap(0.0, time.asSeconds, Armavator.height.asInches, height.asInches, timer.get()).inches
        heightSetpoint = position

        if (timer.get().seconds >= time) {
            stop()
        }
    }
}

/**
 * goes to the specified position w/o consideration of the ob1
 */
suspend fun Armavator.animate(height: Length, angle: Angle, time: Time = 1.5.seconds) = use(this) {
    //TODO: do a smarter time default later
    println("Animating Armavator to $height, $angle")
    val timer = Timer()
    timer.start()
    val startingHeight = Armavator.height
    val startingAngle = Armavator.angle
    periodic {
        val t = timer.get()
        heightSetpoint = cubicMap(0.0, time.asSeconds, startingHeight.asInches, height.asInches, t).inches
        angleSetpoint = cubicMap(0.0, time.asSeconds, startingAngle.asDegrees, angle.asDegrees, t).degrees


        if (t.seconds >= time) {
            stop()
        }
    }
}

suspend fun Armavator.togglePinching() = use(this) {
    isPinching = !isPinching
}

suspend fun Armavator.toggleExtention() = use(this) {
    isExtending = !isExtending
}

enum class GamePiece { HATCH_PANEL, CARGO }