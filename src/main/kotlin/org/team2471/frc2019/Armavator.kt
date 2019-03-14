package org.team2471.frc2019

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.networktables.NetworkTable
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
import org.team2471.frc2019.actions.returnHome
import kotlin.math.abs



object Armavator : Subsystem("Armavator") {
    private const val ARM_OFFSET = -119.22
    private const val ELEVATOR_FEED_FORWARD = 0.1
    const val ELEVATOR_HEIGHT = 21.5 //inches
    const val ARM_LENGTH = 28.0 //inches

    private const val ELEVATOR_VELOCITY = 50.0
    private const val ELEVATOR_ACCELERATION = 150.0
    private const val ELEVATOR_CLIMB_VELOCITY = ELEVATOR_VELOCITY * 0.2
    private const val ELEVATOR_CLIMB_ACCELERATION = ELEVATOR_ACCELERATION * 0.2

    val elevatorMotors = MotorController(TalonID(ELEVATOR_MASTER), VictorID(ELEVATOR_SLAVE)).config {
        encoderType(FeedbackDevice.Analog)
        feedbackCoefficient = .75 * Math.PI / 1023
        inverted(true)
        pid(0) {
            p(0.001)
            f(0.0065)
            motionMagic(ELEVATOR_ACCELERATION, ELEVATOR_VELOCITY)
        }

        currentLimit(15,0,0)
        brakeMode()
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
            p(0.75)
            d(1.0)

            f(13.0)

            motionMagic(560.0, 180.0)
        }
        currentLimit(15,0,0)
    }

    private val intakeMotors = MotorController(VictorID(ARM_INTAKE))

    private val gearShifter = Solenoid(SHIFTER)
    private val clawSolenoid = Solenoid(BALL_INTAKE)
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
        get() = if(!isClimbing) 0.0..26.0 else Pose.LIFTED.elevatorHeight.asInches..26.0// inches

    private val armRange: DoubleRange = -74.0..64.0 // degrees

    val height: Length
        get() = elevatorMotors.position.inches

    val angle: Angle
        get() = armMotors.position.degrees + ARM_OFFSET.degrees

    val intakeCurrent: Double
        get() = PDP.getCurrent(Victors.ARM_INTAKE)

    var isClamping: Boolean
        get() = !clawSolenoid.get()
        set(value) {
//            if (value != isClamping) println("Clamping: $value from ${Thread.currentThread().stackTrace.drop(2).first()}")
            clawSolenoid.set(!value)
        }

    var isPinching: Boolean
        get() = !pinchSolenoid.get()
        set(value) = pinchSolenoid.set(!value)

    var gamePiece: GamePiece? = null

    var angleSetpoint: Angle = angle
        set(value) {
            table.getEntry("Arm Error").setDouble(armMotors.closedLoopError)
            table.getEntry("Arm Output").setDouble(armMotors.output)

            field = value.asDegrees.coerceIn(armRange).degrees
            armMotors.setMotionMagicSetpoint((field.asDegrees - ARM_OFFSET))
        }

    var heightSetpoint: Length = height
        set(value) {
//            println("Height: $value from ${Thread.currentThread().stackTrace.drop(2).first()}")
            table.getEntry("Elevator Error").setDouble(elevatorMotors.closedLoopError)
            table.getEntry("Elevator Output").setDouble(elevatorMotors.output)
            field = value.asInches.coerceIn(heightRange).inches
            elevatorMotors.setMotionMagicSetpoint(field.asInches, ELEVATOR_FEED_FORWARD)
        }

    init {
        elevatorMotors.position = 0.0
        GlobalScope.launch(MeanlibDispatcher) {
            val heightEntry = table.getEntry("Height")
            val angleEntry = table.getEntry("Angle")
            periodic {
                heightEntry.setDouble(height.asInches)
                angleEntry.setDouble(angle.asDegrees)
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

    fun printDebugInfo() {
        println("Arm Angle: %.3f\tArm Setpoint: %.3f".format(angle.asDegrees, angleSetpoint.asDegrees))
        println("Elevator Height: %.3f\tElevator Setpoint: %.3f".format(height.asInches, heightSetpoint.asInches))
    }

    fun zero() {
       elevatorMotors.position = 0.0
    }

    override fun reset() {
        isClimbing = false
        angleSetpoint = angle
        heightSetpoint = height
        intake(0.0)
    }

    override suspend fun default() {
        periodic {
            if(gamePiece == GamePiece.CARGO)  intake(-0.15)
        }
    }
}

suspend fun Armavator.smoothDrivePosition(height: Length, time: Time = 1.5.seconds) = use(this) { //TODO: do a smarter time default later
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
suspend fun Armavator.animate(height: Length, angle: Angle, time: Time = 1.5.seconds) = use(this){//TODO: do a smarter time default later
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

suspend fun Armavator.togglePinching() = use(this){
    isPinching = !isPinching
}

suspend fun Armavator.toggleClamping() = use(this){
    isClamping = !isClamping
}

enum class GamePiece { HATCH_PANEL, CARGO }