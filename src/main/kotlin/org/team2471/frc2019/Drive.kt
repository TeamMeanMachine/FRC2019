package org.team2471.frc2019

import com.analog.adis16448.frc.ADIS16448_IMU
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.TalonID
import org.team2471.frc.lib.control.PDController
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion.following.SwerveDrive
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc.lib.motion_profiling.following.SwerveParameters
import org.team2471.frc.lib.units.*

object Drive : Subsystem("Drive"), SwerveDrive {
    override val frontLeftModule = Module(
        MotorController(TalonID(Talons.DRIVE_FRONTLEFT)),
        MotorController(TalonID(Talons.STEER_FRONTLEFT)),
        false
    )

    override val frontRightModule = Module(
        MotorController(TalonID(Talons.DRIVE_FRONTRIGHT)),
        MotorController(TalonID(Talons.STEER_FRONTRIGHT)),
        false
    )

    override val backLeftModule = Module(
        MotorController(TalonID(Talons.DRIVE_BACKLEFT)),
        MotorController(TalonID(Talons.STEER_BACKLEFT)),
        true
    )

    override val backRightModule = Module(
        MotorController(TalonID(Talons.DRIVE_BACKRIGHT)),
        MotorController(TalonID(Talons.STEER_BACKRIGHT)),
        true
    )

    //private val gyro = SpinMaster16448()
    private val gyro = ADIS16448_IMU()

    override val headingWithDashboardSwitch: Angle
        get() {
            if (SmartDashboard.getBoolean("Use Gyro", false)) {
                return -gyro.angleX.degrees.wrap()
            } else {
                return 0.0.degrees
            }
        }

    override val headingRateWithDashboardSwitch: AngularVelocity
        get() {
            if (SmartDashboard.getBoolean("Use Gyro", false))
                return gyro.rate.degrees.perSecond
            else
                return 0.0.degrees.perSecond
        }

    override val heading: Angle
        get() = -gyro.angleX.degrees.wrap()

    override val headingRate: AngularVelocity
        get() = -gyro.rate.degrees.perSecond

    var myPosition = Vector2(0.0, 0.0)

    override var position: Vector2
        get() = myPosition
        set(pos) {
            myPosition = pos
        }

    override var prevPosition = Vector2(0.0, 0.0)

    override var prevTime = 0.0

    override var velocity = Vector2(0.0, 0.0)

    override var prevPathPosition = Vector2(0.0, 0.0)

    override val parameters: SwerveParameters = SwerveParameters(
        20.5, 21.0, 0.0,
        kFeedForward = 0.12, kPosition = 0.2, kTurn = 0.013
    )

    fun zeroGyro() = gyro.reset()

    override suspend fun default() {
        periodic {
            drive(OI.driveTranslation, OI.driveRotation, false)

            //println( "Odometry: Heading=$heading Position: ${position}")  // todo: send this to network tables to be displayed in visualizer
        }
    }

    class Module(
        val driveMotor: MotorController,
        private val turnMotor: MotorController,
        isBack: Boolean
    ) : SwerveDrive.Module {

        companion object {
            private const val ANGLE_MAX = 983
            private const val ANGLE_MIN = 47

            private const val P = 0.0075 //0.010
            private const val D = 0.00075
        }

        override val angle: Angle
            get() = ((turnMotor.position - ANGLE_MIN) / (ANGLE_MAX - ANGLE_MIN) * 360).degrees.wrap()

        val driveCurrent: Double
            get() = driveMotor.current
        private val pdController = PDController(P, D)

        override val speed: Double
            get() = driveMotor.velocity

        override val currentDistance: Double
            get() = driveMotor.position

        var myPrevDistance: Double = 0.0

        override var previousDistance: Double
            get() = myPrevDistance
            set(dist) {
                myPrevDistance = dist
            }

        override fun zeroEncoder() {
            driveMotor.position = 0.0
        }

        var setPoint = Angle(0.0)

        init {
            turnMotor.config(20) {
                encoderType(FeedbackDevice.Analog)
                encoderContinuous(false)
                inverted(true)
                sensorPhase(true)
                currentLimit(30, 0, 0)
            }
            driveMotor.config {
                inverted(isBack)
                sensorPhase(isBack)
                brakeMode()
                feedbackCoefficient = 1 / 6000.0
                currentLimit(30, 0, 0)
            }
            GlobalScope.launch(MeanlibDispatcher) {
                val table = NetworkTableInstance.getDefault().getTable(name)
                val flAngleEntry = table.getEntry("Front Left Angle")
                val frAngleEntry = table.getEntry("Front Right Angle")
                val blAngleEntry = table.getEntry("Back Left Angle")
                val brAngleEntry = table.getEntry("Back Right Angle")
                val flSPEntry = table.getEntry("Front Left SP")
                val frSPEntry = table.getEntry("Front Right SP")
                val blSPEntry = table.getEntry("Back Left SP")
                val brSPEntry = table.getEntry("Back Right SP")
                periodic {
                    flAngleEntry.setDouble(frontLeftModule.angle.asDegrees)
                    frAngleEntry.setDouble(frontRightModule.angle.asDegrees)
                    blAngleEntry.setDouble(backLeftModule.angle.asDegrees)
                    brAngleEntry.setDouble(backRightModule.angle.asDegrees)
                    flSPEntry.setDouble(frontLeftModule.setPoint.asDegrees)
                    frSPEntry.setDouble(frontRightModule.setPoint.asDegrees)
                    blSPEntry.setDouble(backLeftModule.setPoint.asDegrees)
                    brSPEntry.setDouble(backRightModule.setPoint.asDegrees)
                }
            }
        }

        override fun drive(angle: Angle, power: Double) {
            driveMotor.setPercentOutput(power)
            setPoint = angle
            val current = this.angle
            val error = (setPoint - current).wrap()
            val turnPower = pdController.update(error.asDegrees)
//            println(
//                "Angle: %.3f\tTarget: %.3f\tError: %.3f\tPower: %.3f".format(
//                    current.asDegrees,
//                    angle.asDegrees,
//                    error.asDegrees,
//                    turnPower
//                )
//            )

            turnMotor.setPercentOutput(turnPower)
        }

        override fun driveWithDistance(angle: Angle, distance: Length) {
            driveMotor.setPositionSetpoint(distance.asFeet)
            val error = (angle - this.angle).wrap()
            pdController.update(error.asDegrees)
        }

        override fun stop() {
            driveMotor.stop()
            turnMotor.stop()
        }
    }
}
