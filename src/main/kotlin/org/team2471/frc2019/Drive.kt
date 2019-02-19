package org.team2471.frc2019

import com.analog.adis16448.frc.ADIS16448_IMU
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.TalonID
import org.team2471.frc.lib.control.PDController
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion.following.SwerveDrive
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc.lib.motion_profiling.following.SwerveParameters
import org.team2471.frc.lib.units.*
import org.team2471.frc.lib.util.Timer

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

    private val gyro = SpinMaster16448()
//    private val gyro = ADIS16448_IMU()

    override val heading: Angle
        get() = -gyro.angle.degrees.wrap()   //getX .degrees.wrap()

    override val headingRate: AngularVelocity
        get() = gyro.rate.degrees.perSecond

    var myPosition = Vector2(0.0,0.0)

    override var position: Vector2
        get() = myPosition
        set(pos) {
            myPosition = pos
        }

    override val parameters: SwerveParameters = SwerveParameters(20.5, 21.0, 0.0)

    fun zeroGyro() = gyro.reset()

    override suspend fun default() {
        periodic {
            drive(OI.driveTranslation, OI.driveRotation)

            println( "Odometry: Heading=$heading Position: ${position.x}, ${position.y}")  // todo: send this to network tables to be displayed in visualizer
        }
    }

    class Module(val driveMotor: MotorController, private val turnMotor: MotorController, isBack: Boolean) :
        SwerveDrive.Module {

        companion object {
            private const val ANGLE_MAX = 983
            private const val ANGLE_MIN = 47

            private const val P = 0.010
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

        var myPrevDistance : Double = 0.0

        override var previousDistance: Double
            get() = myPrevDistance
            set(dist) {
                myPrevDistance = dist
            }
        override fun zeroEncoder() {
            driveMotor.position = 0.0
        }



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
                brakeMode()
                currentLimit(30, 0, 0)
            }
        }

        override fun drive(angle: Angle, power: Double) {
            driveMotor.setPercentOutput(power)
            val current = this.angle
            val error = (angle - current).wrap()
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
