package org.team2471.frc2019

import com.analog.adis16448.frc.ADIS16448_IMU
import org.team2471.frc.lib.Unproven
import org.team2471.frc.lib.actuators.TalonSRX
import org.team2471.frc.lib.control.PDController
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.motion.following.SwerveDrive
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc.lib.motion_profiling.following.SwerveParameters
import org.team2471.frc.lib.units.*

@Unproven
object Drive: Subsystem("Drive"), SwerveDrive {
    private const val P = 0.0
    private const val D = 0.0
    private const val dt = .02

    private val frontLeftMotor = TalonSRX(Talons.DRIVE_FRONTLEFT)
    private val frontRightMotor = TalonSRX(Talons.DRIVE_FRONTRIGHT)

    private val backLeftMotor = TalonSRX(Talons.DRIVE_BACKLEFT)
    private val backRightMotor = TalonSRX(Talons.DRIVE_BACKRIGHT)

    private val frontLeftSteer = TalonSRX(Talons.STEER_FRONTLEFT)
    private val frontRightSteer = TalonSRX(Talons.STEER_FRONTRIGHT)

    private val backLeftSteer = TalonSRX(Talons.STEER_BACKLEFT)
    private val backRightSteer = TalonSRX(Talons.STEER_BACKRIGHT)

    private val frontLeftController = PDController(P, D, dt)
    private val frontRightController = PDController(P, D, dt)
    private val backLeftController = PDController(P, D, dt)
    private val backRightController = PDController(P, D, dt)

    private val gyro = ADIS16448_IMU()

    override val heading: Angle
        get() = gyro.angle.degrees

    override val headingRate: AngularVelocity
        get() = gyro.rate.degrees.perSecond

    override val frontLeftAngle: Angle
        get() = frontLeftSteer.position.degrees

    override val frontRightAngle: Angle
        get() = frontRightSteer.position.degrees

    override val backRightAngle: Angle
        get() = backRightSteer.position.degrees

    override val backLeftAngle: Angle
        get() = backLeftSteer.position.degrees

    override val parameters: SwerveParameters = SwerveParameters(TODO(), TODO(), TODO())

    override fun stop() {
        frontLeftMotor.stop()
        frontRightMotor.stop()
        backLeftMotor.stop()
        backRightMotor.stop()
        frontLeftSteer.stop()
        frontRightSteer.stop()
        backLeftMotor.stop()
        backRightMotor.stop()
    }

    override fun driveClosedLoop(
        frontLeftDistance: Length,
        frontRightDistance: Length,
        backRightDistance: Length,
        backLeftDistance: Length,
        frontLeftAngle: Angle,
        frontRightAngle: Angle,
        backRightAngle: Angle,
        backLeftAngle: Angle
    ) {
        frontLeftMotor.setMotionMagicSetpoint(frontLeftDistance.asInches)
        frontRightMotor.setMotionMagicSetpoint(frontRightDistance.asInches)

        backLeftMotor.setMotionMagicSetpoint(backLeftDistance.asInches)
        backRightMotor.setMotionMagicSetpoint(backRightDistance.asInches)

        frontLeftSteer.setMotionMagicSetpoint(frontLeftAngle.asDegrees)
        frontRightSteer.setMotionMagicSetpoint(frontRightAngle.asDegrees)

        backLeftSteer.setMotionMagicSetpoint(backLeftAngle.asDegrees)
        backRightSteer.setMotionMagicSetpoint(backRightAngle.asDegrees)
    }

    override fun driveOpenLoop(
        frontLeftPower: Double,
        frontRightPower: Double,
        backLeftPower: Double,
        backRightPower: Double,
        frontLeftAngle: Angle,
        frontRightAngle: Angle,
        backLeftAngle: Angle,
        backRightAngle: Angle
    ) {
        frontLeftMotor.setPercentOutput(frontLeftPower)
        frontRightMotor.setPercentOutput(frontRightPower)

        backLeftMotor.setPercentOutput(backLeftPower)
        backRightMotor.setPercentOutput(backRightPower)

        frontLeftSteer.setPercentOutput(frontLeftController.update(frontLeftAngle.asDegrees, frontLeftAngle.asDegrees))
        backLeftSteer.setPercentOutput(backLeftController.update(backLeftAngle.asDegrees, backLeftAngle.asDegrees))
        frontRightSteer.setPercentOutput(frontRightController.update(frontRightAngle.asDegrees, frontRightAngle.asDegrees))
        backRightSteer.setPercentOutput(backRightController.update(backRightAngle.asDegrees, backRightAngle.asDegrees))
//        frontLeftSteer.setMotionMagicSetpoint(frontLeftAngle.asDegrees)
//        frontRightSteer.setMotionMagicSetpoint(frontRightAngle.asDegrees)
//
//        backLeftSteer.setMotionMagicSetpoint(backLeftAngle.asDegrees)
//        backRightSteer.setMotionMagicSetpoint(backRightAngle.asDegrees)
    }

    override suspend fun default() {
        drive(OI.driveTranslation, OI.driveRotation)
    }
}