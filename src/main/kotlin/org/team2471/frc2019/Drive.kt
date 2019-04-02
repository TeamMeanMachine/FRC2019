package org.team2471.frc2019

import com.analog.adis16448.frc.ADIS16448_IMU
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.kauailabs.navx.frc.AHRS
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.SPI
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.interfaces.Gyro
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.TalonID
import org.team2471.frc.lib.control.PDController
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion.following.SwerveDrive
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc.lib.motion_profiling.following.SwerveParameters
import org.team2471.frc.lib.units.*
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.withSign

private var gyroOffset = 0.0.degrees

object Drive : Subsystem("Drive"), SwerveDrive {
    override val modules: Array<SwerveDrive.Module> = arrayOf(
        Module(
            MotorController(TalonID(Talons.DRIVE_FRONTLEFT)),
            MotorController(TalonID(Talons.STEER_FRONTLEFT)),
            false,
            Vector2(-10.0, 10.5),
            0.0.degrees
        ),

        Module(
            MotorController(TalonID(Talons.DRIVE_FRONTRIGHT)),
            MotorController(TalonID(Talons.STEER_FRONTRIGHT)),
            false,
            Vector2(10.0, 10.5),
            0.0.degrees
        ),

        Module(
            MotorController(TalonID(Talons.DRIVE_BACKLEFT)),
            MotorController(TalonID(Talons.STEER_BACKLEFT)),
            true,
            Vector2(-10.0, -10.5),
            0.0.degrees // could get rid of isBack and set this to 180 degrees
        ),

        Module(
            MotorController(TalonID(Talons.DRIVE_BACKRIGHT)),
            MotorController(TalonID(Talons.STEER_BACKRIGHT)),
            true,
            Vector2(10.0, -10.5),
            0.0.degrees
        )
    )

    //    private val gyro: SpinMaster16448? = SpinMaster16448()
//  private val gyro: Gyro? = null
//    private val gyro: Gyro? = ADISWrapper()
    private val gyro: NavxWrapper? = NavxWrapper()

    override var heading: Angle
        get() = gyroOffset - ((gyro?.angle ?: 0.0).degrees.wrap())
        set(value) {
            gyroOffset = value
            gyro?.reset()
        }

    override val headingRate: AngularVelocity
        get() = -(gyro?.rate ?: 0.0).degrees.perSecond

    override var velocity = Vector2(0.0, 0.0)

    override var position = Vector2(0.0, 0.0)

    override var robotPivot = Vector2(0.0, 0.0)

    override val parameters: SwerveParameters = SwerveParameters(
        gyroRateCorrection = 0.0,
        kPositionFeedForward = 0.06,
        kPosition = 0.2,
        kHeading = 0.013,
        kHeadingFeedForward = 0.00195
    )

    init {
        SmartDashboard.setPersistent("Use Gyro")

        SmartDashboard.putData("Gyro", gyro!!.getNavX())

        GlobalScope.launch(MeanlibDispatcher) {
            val table = NetworkTableInstance.getDefault().getTable(name)

            val headingEntry = table.getEntry("Heading")

            /*
            val flAngleEntry = table.getEntry("Front Left Angle")
            val frAngleEntry = table.getEntry("Front Right Angle")
            val blAngleEntry = table.getEntry("Back Left Angle")
            val brAngleEntry = table.getEntry("Back Right Angle")
            val flSPEntry = table.getEntry("Front Left SP")
            val frSPEntry = table.getEntry("Front Right SP")
            val blSPEntry = table.getEntry("Back Left SP")
            val brSPEntry = table.getEntry("Back Right SP")

            val flErrorEntry = table.getEntry("Front Left Error")
            val frErrorEntry = table.getEntry("Front Right Error")
            val blErrorEntry = table.getEntry("Back Left Error")
            val brErrorEntry = table.getEntry("Back Right Error")

        */
            periodic {
                /* flAngleEntry.setDouble(frontLeftModule.angle.asDegrees)
                   frAngleEntry.setDouble(frontRightModule.angle.asDegrees)
                   blAngleEntry.setDouble(backLeftModule.angle.asDegrees)
                   brAngleEntry.setDouble(backRightModule.angle.asDegrees)
                   flSPEntry.setDouble(frontLeftModule.setPoint.asDegrees)
                   frSPEntry.setDouble(frontRightModule.setPoint.asDegrees)
                   blSPEntry.setDouble(backLeftModule.setPoint.asDegrees)
                   brSPEntry.setDouble(backRightModule.setPoint.asDegrees)

                   flErrorEntry.setDouble(frontLeftModule.error.asDegrees)
                   frErrorEntry.setDouble(frontRightModule.error.asDegrees)
                   blErrorEntry.setDouble(backLeftModule.error.asDegrees)
                   brErrorEntry.setDouble(backRightModule.error.asDegrees)*/

                headingEntry.setDouble(heading.asDegrees)
            }
        }
    }

    fun zeroGyro() = gyro?.reset()

    override suspend fun default() {
        val limelightTable = NetworkTableInstance.getDefault().getTable("limelight")
        val xEntry = limelightTable.getEntry("tx")
        val angleEntry = limelightTable.getEntry("ts")
/*
        val table = NetworkTableInstance.getDefault().getTable(name)
        val positionXEntry = table.getEntry("positionX")
        val positionYEntry = table.getEntry("positionY")
*/
        periodic {
            drive(
                OI.driveTranslation,
                OI.driveRotation,
                SmartDashboard.getBoolean("Use Gyro", true) && !DriverStation.getInstance().isAutonomous,
                OI.operatorTranslation,
                OI.operatorRotation
            )
/*
            positionXEntry.setDouble(position.x)
            positionYEntry.setDouble(position.y)
*/
        }
    }

    class Module(
        private val driveMotor: MotorController,
        private val turnMotor: MotorController,
        isBack: Boolean,
        override val modulePosition: Vector2,
        override val angleOffset: Angle
    ) : SwerveDrive.Module {

        companion object {
            private const val ANGLE_MAX = 983
            private const val ANGLE_MIN = 47

            private const val P = 0.0075 //0.010
            private const val D = 0.00075
        }

        override val angle: Angle
            get() = ((turnMotor.position - ANGLE_MIN) / (ANGLE_MAX - ANGLE_MIN) * 360 + angleOffset.asDegrees).degrees.wrap()

        val driveCurrent: Double
            get() = driveMotor.current
        private val pdController = PDController(P, D)

        override val speed: Double
            get() = driveMotor.velocity

        override val currDistance: Double
            get() = driveMotor.position

        override var prevDistance: Double = 0.0

        override fun zeroEncoder() {
            driveMotor.position = 0.0
        }

        override var angleSetpoint = 0.degrees
            set(value) {
                field = value
                val current = this.angle
                val error = (field - current).wrap()
                val turnPower = pdController.update(error.asDegrees)
                turnMotor.setPercentOutput(turnPower)
//            println(
//                "Angle: %.3f\tTarget: %.3f\tError: %.3f\tPower: %.3f".format(
//                    current.asDegrees,
//                    angle.asDegrees,
//                    error.asDegrees,
//                    turnPower
//                )
//            )
            }

        override fun setDrivePower(power: Double) {
            driveMotor.setPercentOutput(power)
        }

        val error: Angle
            get() = turnMotor.closedLoopError.degrees

        init {
            turnMotor.config(20) {
                encoderType(FeedbackDevice.Analog)
                encoderContinuous(false)
                inverted(true)
                sensorPhase(true)
                currentLimit(30, 0, 0)
                openLoopRamp(0.2)
            }
            driveMotor.config {
                inverted(isBack)
                sensorPhase(isBack)
                brakeMode()
                feedbackCoefficient = 1 / (4687.5 * 15.0 / 12.0)
                currentLimit(30, 0, 0)
                openLoopRamp(0.15)
            }
            GlobalScope.launch(MeanlibDispatcher) {
                val table = NetworkTableInstance.getDefault().getTable(name)
                val flAngleEntry = table.getEntry("Front Left Angle")
                val frAngleEntry = table.getEntry("Front Right Angle")
                val blAngleEntry = table.getEntry("Back Left Angle")
                val brAngleEntry = table.getEntry("Back Right Angle")
//                val flSPEntry = table.getEntry("Front Left SP")
//                val frSPEntry = table.getEntry("Front Right SP")
//                val blSPEntry = table.getEntry("Back Left SP")
//                val brSPEntry = table.getEntry("Back Right SP")
                periodic {
                    flAngleEntry.setDouble(modules[0].angle.asDegrees)
                    frAngleEntry.setDouble(modules[1].angle.asDegrees)
                    blAngleEntry.setDouble(modules[2].angle.asDegrees)
                    brAngleEntry.setDouble(modules[3].angle.asDegrees)
//                    flSPEntry.setDouble(modules[0].setPoint.asDegrees)
//                    frSPEntry.setDouble(modules[1].setPoint.asDegrees)
//                    blSPEntry.setDouble(modules[2].setPoint.asDegrees)
//                    brSPEntry.setDouble(modules[3].setPoint.asDegrees)
                }
            }
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

suspend fun Drive.driveDistance(distance: Length, speed: Double) = use(Drive) {
    // TODO: fix this
    val initialPosition = position
    periodic {
        drive(Vector2(0.0, speed.withSign(distance.asInches)), 0.0, false)

        val traveled = initialPosition.distance(position)
        println("Position: $position, initial: $initialPosition, distance: $traveled")
        if (traveled > distance.asFeet.absoluteValue) stop()
    }
}

suspend fun Drive.driveTime(translation: Vector2, time: Time) = use(Drive) {
    val timer = Timer().apply { start() }
    periodic {
        drive(translation, 0.0, false)
        if (timer.get() > time.asSeconds) stop()
    }
}

suspend fun Drive.turnToAngle(angle: Angle) = use(this){
    val kTurn = 0.007
    periodic {
        val turnError = (angle - heading).wrap()
        Drive.drive(Vector2(0.0,0.0), turnError.asDegrees * kTurn)
    }
}