package org.team2471.frc2019

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.Timer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.TalonID
import org.team2471.frc.lib.actuators.VictorID
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.halt
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.cubicMap
import org.team2471.frc.lib.units.*
import org.team2471.frc2019.Talons.OB1_INTAKE
import org.team2471.frc2019.Talons.OB1_PIVOT_MASTER
import org.team2471.frc2019.Victors.OB1_PIVOT_SLAVE
import kotlin.math.abs

private const val PIVOT_F = 50.0

object OB1 : Subsystem("OB1") {
    private val pivotMotors = MotorController(TalonID(OB1_PIVOT_MASTER), VictorID(OB1_PIVOT_SLAVE)).config {
        currentLimit(35, 0, 0)
        encoderType(FeedbackDevice.Analog)
        feedbackCoefficient = 1 / 2.6
        sensorPhase(false)
        encoderContinuous(false)
        rawOffset(877)
        inverted(true)
        ctreFollowers[0].inverted = false

        pid {
            p(8.0)
            d(8.0)
            f(PIVOT_F)
            motionMagic(360.0, 300.0)
        }
    }

    private val intakeMotor = MotorController(TalonID(OB1_INTAKE)).config {
        inverted(true)
    }

    private val table = NetworkTableInstance.getDefault().getTable(name)


    private val pivotRange = -2.0..180.0

    var isClimbing = false
        set(value) {
            if (value != isClimbing) {
                pivotMotors.config {
                    pid {
                        f(if (value) 0.0 else PIVOT_F)
                    }
                }
            }
            field = value
        }

    init {
        GlobalScope.launch(MeanlibDispatcher) {
            val angleEntry = table.getEntry("Angle")
            val outputEntry = table.getEntry("Output")
            periodic {
                angleEntry.setDouble(angle.asDegrees)
                outputEntry.setDouble(pivotMotors.output)
                table.getEntry("Setpoint").setDouble(angleSetpoint.asDegrees)
            }
        }
    }

    val intakeCurrent: Double
        get() = intakeMotor.current

    val angle: Angle
        get() = pivotMotors.position.degrees

    val output: Double
        get() = pivotMotors.output

    const val BALL_INTAKE_PRESET = 63.0

    var angleSetpoint: Angle = angle
        set(value) {
            field = value.asDegrees.coerceIn(pivotRange).degrees
            pivotMotors.setMotionMagicSetpoint(field.asDegrees)
            table.getEntry("OB1 Error").setDouble(pivotMotors.closedLoopError)
            table.getEntry("OB1 Output").setDouble(pivotMotors.output)
        }

    fun climb(setpoint: Angle, feedForward: Double) {
        pivotMotors.setPositionSetpoint(setpoint.asDegrees, feedForward)
//        pivotMotors.setPercentOutput(if (setpoint < angle) -1.0 else feedForward)
    }

    fun pivotRaw(power: Double) {
        pivotMotors.setPercentOutput(power)
//        println(angle.asDegrees)
    }

    fun intake(power: Double) {
//        println("$power from ${Thread.currentThread().stackTrace.drop(2).first()}")
        intakeMotor.setPercentOutput(power)
    }

    override suspend fun default() {
        periodic {
            angleSetpoint = angleSetpoint
            angleSetpoint += (OI.obiControl * 80.0 * period).degrees
        }
    }

    override fun reset() {
        angleSetpoint = angle
        intake(0.0)
        isClimbing = false
    }


}

suspend fun OB1.animateToAngle(
    angle: Angle,
    time: Time = 1.5.seconds * (abs((this.angle - angle).asDegrees) / 180.0) // 1.5 seconds per 180 degrees
) = use(this) {
    println("OBI animating to $angle")
    val timer = Timer()
    timer.start()
    val startingAngle = OB1.angle
    periodic {
        angleSetpoint = cubicMap(0.0, time.asSeconds, startingAngle.asDegrees, angle.asDegrees, timer.get()).degrees

        if (timer.get().seconds >= time) {
            stop()
        }
    }
}

suspend fun OB1.intakeHatch() = use(OB1) {
    periodic {
        angleSetpoint = 0.degrees
        intake(-0.5)
    }
}

suspend fun OB1.intakeCargo(): Nothing = use(OB1) {
    try {
        OB1.intake(1.0)
        OB1.animateToAngle(OB1.BALL_INTAKE_PRESET.degrees, 1.5.seconds)
        halt()
    } finally {
        OB1.intake(0.0)
    }
}

suspend fun OB1.evacuateBall() = use(OB1) {
    periodic {
        angleSetpoint = Pose.CARGO_GROUND_PICKUP.obiAngle
        intake(-1.0)
        Armavator.intake(-1.0)
    }
}