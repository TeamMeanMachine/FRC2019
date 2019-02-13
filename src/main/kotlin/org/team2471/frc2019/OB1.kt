package org.team2471.frc2019

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import edu.wpi.first.wpilibj.GenericHID
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
import org.team2471.frc.lib.testing.smoothDrivePosition
import org.team2471.frc.lib.units.*
import org.team2471.frc2019.Talons.OB1_INTAKE
import org.team2471.frc2019.Talons.OB1_PIVOT_MASTER
import org.team2471.frc2019.Victors.OB1_PIVOT_SLAVE
import javax.xml.bind.JAXBElement
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

    private val topExtent = Vector2(-8.0, -1.0)

    private val bottomExtent = Vector2(-8.0, -11.0)

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

    const val BALL_INTAKE_PRESET = 67.0

    fun pivot(angle: Angle) {
        pivotMotors.setPositionSetpoint(angle.asDegrees.coerceIn(pivotRange))
    }

    fun pivotRaw(power: Double) {
        pivotMotors.setPercentOutput(power)
        println(angle.asDegrees)
    }

    fun intake(power: Double) {
        intakeMotor.setPercentOutput(power)
    }

    suspend fun animateToAngle(angle: Angle, time: Time) = use(this) {
        pivotMotors.smoothDrivePosition(angle.asDegrees, time)
    }

    override suspend fun default() {
        var ob1Setpoint = OB1.angle
        periodic {
            ob1Setpoint += (OI.rightYStick * 80.0 * period).degrees
            OB1.pivot(ob1Setpoint)
            intake(OI.operatorController.getTriggerAxis(GenericHID.Hand.kLeft))
            val collisionZone = collisionZone
            println("%.3f -> %.3f..%.3f".format(angle.asDegrees, collisionZone.start, collisionZone.endInclusive))
        }
    }

}

suspend fun OB1.intakeHatch() = use(OB1) {
    periodic {
        pivot(0.degrees)
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