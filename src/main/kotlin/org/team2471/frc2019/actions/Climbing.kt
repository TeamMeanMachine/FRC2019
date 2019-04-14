package org.team2471.frc2019.actions

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Timer
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc.lib.motion.following.stop
import org.team2471.frc.lib.motion_profiling.MotionCurve
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.inches
import org.team2471.frc2019.*

private var isClimbing = false

suspend fun climb() {
    if (isClimbing) return
    isClimbing = true
    use(Armavator, OB) {
        try {
            goToPose(Pose.BEFORE_CLIMB)
            OB.angleSetpoint = 120.0.degrees
            delay(2.0)
            suspendUntil { OI.driverController.x }
            Armavator.isClimbing = true
            val armCurve = MotionCurve().apply {
                storeValue(0.0, Pose.BEFORE_CLIMB.armAngle.asDegrees)
                storeValue(1.5, Pose.LIFTED.armAngle.asDegrees)
            }
            val obCurve = MotionCurve().apply {
                storeValue(0.0, 120.0)
                storeValue(2.5, 0.0)
            }

            val elevatorCurve = MotionCurve().apply {
                storeValue(0.0, Pose.BEFORE_CLIMB.elevatorHeight.asInches)
                storeValue(1.5, Pose.LIFTED.elevatorHeight.asInches)
            }

            val timer = Timer().apply { start() }
            val gyroAngle = Drive.heading
            val startingPitch = Drive.gyro!!.getNavX().pitch
            var leftIncrease = 0.0.degrees
            var rightIncrease = 0.0.degrees
            use(Drive) {
                periodic {
                    val time = timer.get()//.coerceAtMost(2.0)
                    val pitchError = startingPitch - Drive.gyro!!.getNavX().pitch
                    val elevatorOffset = pitchError * 1.5

                    Armavator.heightSetpoint = elevatorCurve.getValue(time).inches + elevatorOffset.inches
                    Armavator.angleSetpoint = armCurve.getValue(time).degrees
                    OB.climbLeft(obCurve.getValue(time).degrees + leftIncrease)
                    OB.climbRight(obCurve.getValue(time).degrees + rightIncrease)

                    if (obCurve.getValue(time).degrees < 5.0.degrees) {
                        val error = (gyroAngle - Drive.heading).wrap()
                        if (Math.abs(error.asDegrees) > 5.0) {
                            if (error > 0.0.degrees) {
                                rightIncrease = 5.0.degrees

                            } else {
                                leftIncrease = 5.0.degrees
                            }
                        } else {
                            leftIncrease = 0.0.degrees
                            rightIncrease = 0.0.degrees
                        }

                    }



                    OB.climbDrive(1.0)
                    Drive.drive(Vector2(0.0, 0.45), 0.0, fieldCentric = false)
                    if (OI.driverController.x && time > obCurve.tailKey.time)
                        stop()
                    if (OI.driverController.b)
                        stop()

                }
                Drive.stop()
                Armavator.elevatorMotors.setPercentOutput(0.0)
            }

            val armCurve2 = MotionCurve().apply {
                storeValue(0.0, Pose.LIFTED.armAngle.asDegrees)
                storeValue(2.0, Pose.AFTER_LIFTED.armAngle.asDegrees)
            }

            val elevatorCurve2 = MotionCurve().apply {
                storeValue(0.0, Pose.LIFTED.elevatorHeight.asInches)
                storeValue(2.0, Pose.AFTER_LIFTED.elevatorHeight.asInches)
            }

            val obCurve2 = MotionCurve().apply {
                storeValue(0.0, 0.0)
                storeValue(1.0, 10.0)
            }

            use(Drive) {
                val timer2 = Timer().apply { start() }
                periodic {
                    val time = timer2.get()//.coerceAtMost(2.0)
                    OB.climbDrive(1.0)
                    Drive.drive(OI.driveTranslation.apply {
                        x /= 2
                        y = y.coerceAtMost(0.0)
                    }, 0.0)
                    Armavator.heightSetpoint = elevatorCurve2.getValue(time).inches
                    Armavator.angleSetpoint = armCurve2.getValue(time).degrees
                    OB.angleSetpoint = obCurve2.getValue(time).degrees
                    if (OI.driverController.b)
                        stop()
                }

            }
        } finally {
            isClimbing = false
            withContext(NonCancellable) {
                OB.angleSetpoint = 180.degrees
                suspendUntil {
                    println("Got to finally")
                    DriverStation.getInstance().isDisabled ||
                            Math.abs(((OB.leftAngle + OB.rightAngle) / 2.0 - OB.angleSetpoint).asDegrees) < 5
                }
            }
        }
    }
}

    suspend fun climb2() {
        if (isClimbing) return
        isClimbing = true
        use(Armavator, OB) {
            try {
                goToPose(Pose.BEFORE_CLIMB2)
                OB.angleSetpoint = 120.0.degrees
                delay(2.0)
                suspendUntil { OI.driverController.x }
                Armavator.isClimbing = true
                val obCurve = MotionCurve().apply {
                    storeValue(0.0, 120.0)
                    storeValue(2.0, 0.0)
                }

                val elevatorCurve = MotionCurve().apply {
                    storeValue(0.0, Pose.BEFORE_CLIMB2.elevatorHeight.asInches)
                    storeValue(1.5, Pose.LIFTED2.elevatorHeight.asInches)
                }

                val armCurve = MotionCurve().apply {
                    storeValue(0.0, Pose.BEFORE_CLIMB2.armAngle.asDegrees)
                    storeValue(2.0, Pose.LIFTED2.armAngle.asDegrees)
                }

                val timer = Timer().apply { start() }
                val startingPitch = Drive.gyro!!.getNavX().pitch
                use(Drive) {
                    periodic {
                        val time = timer.get()//.coerceAtMost(2.0)
                        val pitchError = startingPitch - Drive.gyro!!.getNavX().pitch
                        val elevatorOffset = pitchError * 1.5
                        Armavator.heightSetpoint = elevatorCurve.getValue(time).inches + elevatorOffset.inches
                        Armavator.angleSetpoint = armCurve.getValue(time).degrees
                        OB.climb(obCurve.getValue(time).degrees)

                        OB.climbDrive(1.0)
                        Drive.drive(Vector2(0.0, 0.45), 0.0, fieldCentric = false)
                        if (OI.driverController.x && time > obCurve.tailKey.time)
                            stop()
                        if (OI.driverController.b)
                            stop()

                    }
                    Drive.stop()
                }

                val armCurve2 = MotionCurve().apply {
                    storeValue(0.0, Pose.LIFTED2.armAngle.asDegrees)
                    storeValue(1.2, Pose.AFTER_LIFTED2.armAngle.asDegrees)
                }

                val elevatorCurve2 = MotionCurve().apply {
                    storeValue(0.0, Pose.LIFTED2.elevatorHeight.asInches)
                    storeValue(1.5, Pose.AFTER_LIFTED2.elevatorHeight.asInches)
                }

                use(Drive) {
                    val timer2 = Timer().apply { start() }
                    periodic {
                        val time = timer2.get()//.coerceAtMost(2.0)
                        OB.climbDrive(1.0)
                        Armavator.heightSetpoint = elevatorCurve2.getValue(time).inches
                        Armavator.angleSetpoint = armCurve2.getValue(time).degrees
                        Drive.drive(OI.driveTranslation.apply {
                            x /= 2
                            y = y.coerceAtMost(0.0)
                        }, OI.driveRotation * 0.75)
                        if (OI.driverController.b)
                            stop()
                    }
                }
            } finally {
                isClimbing = false
                withContext(NonCancellable) {
                    OB.angleSetpoint = 180.degrees
                    suspendUntil {
                        DriverStation.getInstance().isDisabled ||
                                Math.abs(((OB.leftAngle + OB.rightAngle) / 2.0 - OB.angleSetpoint).asDegrees) < 5
                    }
                }
            }
        }
    }