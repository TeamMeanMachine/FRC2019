package org.team2471.frc2019

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.Timer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.TalonID
import org.team2471.frc.lib.actuators.TalonSRX
import org.team2471.frc.lib.actuators.VictorID
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.halt
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.DoubleRange
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.math.cubicMap
import org.team2471.frc.lib.testing.smoothDrivePosition
import org.team2471.frc.lib.units.*
import org.team2471.frc2019.Talons.OB1_INTAKE
import org.team2471.frc2019.Talons.OB1_PIVOT_MASTER
import org.team2471.frc2019.Victors.OB1_PIVOT_SLAVE
import javax.xml.bind.JAXBElement
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object OB1 : Subsystem("OB1") {
    const val COLLISION_SAFETY_FACTOR = 3.0 //iNchES
    val pivotMotors = MotorController(TalonID(OB1_PIVOT_MASTER), VictorID(OB1_PIVOT_SLAVE)).config {
        currentLimit(10, 0, 0)
        encoderType(FeedbackDevice.Analog)
        feedbackCoefficient = 1 / 2.6
        sensorPhase(false)
        encoderContinuous(false)
        rawOffset(877)
        inverted(true)
        ctreFollowers[0].inverted = false

        pid {
            p(24.0)
        }
    }

    private val intakeMotor = MotorController(TalonID(OB1_INTAKE)).config {
        inverted(true)
    }

    private val pivotRange = -2.0..180.0

    private val topExtent = Vector2(8.0, -1.0)

    private val bottomExtent = Vector2(8.0, -11.0)

    val collisionZone: DoubleRange
        get() {
            val pivotAngle = angle
            val topExtentRotated = topExtent.rotateRadians(pivotAngle.asRadians)
            val bottomExtentRotated = bottomExtent.rotateRadians(pivotAngle.asRadians)
            val higher = max(topExtentRotated.y, bottomExtentRotated.y)
            val lower = min(topExtentRotated.y, bottomExtentRotated.y)
            return (lower - COLLISION_SAFETY_FACTOR)..(higher + COLLISION_SAFETY_FACTOR)
        }

    val angle: Angle
        get() = pivotMotors.position.degrees

    const val BALL_INTAKE_PRESET = 63.0

    var pivotSetpoint: Angle = angle
        set(value) {
            field = value.asDegrees.coerceIn(pivotRange).degrees
            pivotMotors.setPositionSetpoint(field.asDegrees)
        }

    fun pivotRaw(power: Double) {
        pivotMotors.setPercentOutput(power)
        println(angle.asDegrees)
    }

    fun intake(power: Double) {
        intakeMotor.setPercentOutput(power)
    }

    override suspend fun default() {
        periodic {
            pivotSetpoint += (OI.rightYStick * 80.0 * period).degrees
            intake(OI.operatorController.getTriggerAxis(GenericHID.Hand.kLeft))
//            println("%.3f -> %.3f..%.3f".format(angle.asDegrees, collisionZone.start, collisionZone.endInclusive))
        }
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
        pivotSetpoint = cubicMap(0.0, time.asSeconds, startingAngle.asDegrees, angle.asDegrees, timer.get()).degrees

        if (timer.get().seconds >= time) {
            stop()
        }
    }
}

suspend fun OB1.intakeHatch() = use(OB1) {
    periodic {
        pivotSetpoint = 0.degrees
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