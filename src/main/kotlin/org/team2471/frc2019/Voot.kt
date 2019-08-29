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
import org.team2471.frc.lib.coroutines.halt
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


object Voot : Subsystem("Voot") {

    val elevatorMotors = MotorController(TalonID(ELEVATOR_MASTER), VictorID(ELEVATOR_SLAVE)).config {
        encoderType(FeedbackDevice.Analog)
    }

    // TODO: need vacuum pressure sensor
    val vacuumMotor = MotorController(TalonID(VACUUM_MOTOR)).config {
        encoderType(FeedbackDevice.Analog)
    }
//
//   private val gearShifter = Solenoid(SHIFTER)

    private val vacuumSolenoid = Solenoid(VACUUM)
    private val extenderSolenoid = Solenoid(EXTENDER)

    private val table = NetworkTableInstance.getDefault().getTable(name)

    var isClimbing = false // TODO: isClimbing is false!
//        set(value) {
//            if (value != isClimbing) elevatorMotors.config(0) {
//                pid {
//                    if (value) {
//                        motionMagic(ELEVATOR_CLIMB_ACCELERATION, ELEVATOR_CLIMB_VELOCITY)
//                    } else {
//                        motionMagic(ELEVATOR_ACCELERATION, ELEVATOR_VELOCITY)
//                    }
//                }
//            }
//            gearShifter.set(value)
//            field = value
//        }

    val heightRange: DoubleRange
        get() = if (!isClimbing) min(
            Pose.CARGO_GROUND_PICKUP.elevatorHeight.asInches,
            height.asInches
        )..0.0 else Pose.LIFTED.elevatorHeight.asInches..0// inches // TODO: Elevator height?

    val height: Length
        get() = elevatorMotors.position.inches

    var isSucking: Boolean
        get() = vacuumSolenoid.get()
        set(value) {
            vacuumSolenoid.set(value)
        }

    var isExtending: Boolean
        get() = extenderSolenoid.get()
        set(value) {
            extenderSolenoid.set(value)
        }

    var isLocked: Boolean = false


    fun setElevatorPower(power: Double) {
        elevatorMotors.setPercentOutput(power)
        table.getEntry("elevatorPowerset").setDouble(power)
        table.getEntry("elevatorPowerget").getDouble(elevatorMotors.output)

    }


    override fun reset() {
        isClimbing = false
        setElevatorPower(0.0)
    }

    init {

    }
}

