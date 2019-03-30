package org.team2471.frc2019.actions

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.math.linearMap
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc.lib.motion.following.stop
import org.team2471.frc.lib.motion_profiling.MotionCurve
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.inches
import org.team2471.frc2019.*
import java.util.*
import kotlin.math.max


//suspend fun climb() = use(Armavator) {
/* val heightOffset = (-2.8).inches
 val pivotToRollers = 13.inches
 val thetaOffset = 58.degrees
 val heightStep = 8.inches


 goToPose(Pose.BEFORE_CLIMB)
 goToPose(Pose.CLIMB_START)
 suspendUntil { OI.startClimb }

//    // climbing cannot be canceled in this stage
 use(Drive) {
     OB1.isClimbing = true
     periodic {
         Armavator.isClimbing = true
         OB1.intake(-0.7)
         Armavator.heightSetpoint = Pose.LIFTED.elevatorHeight
//                OB1.angleSetpoint = Math.asin(
//                    (Armavator.heightSetpoint.asInches -
//                            heightOffset.asInches + heightStep.asInches) / pivotToRollers.asInches
//                ).degrees
         val height = Armavator.height
         val obiSetpoint =
             thetaOffset + Angle.asin((height.asInches - heightOffset.asInches + heightStep.asInches) / pivotToRollers.asInches)
         OB1.climb(obiSetpoint, -0.7 * (OB1.angle - thetaOffset).cos() - 0.1)

         Armavator.angleSetpoint = Pose.LIFTED.armAngle
         if (height < Armavator.heightSetpoint + 2.inches && OB1.angle < OB1.angleSetpoint + 2.degrees) {
             stop()
         }
     }

    / periodic {
         OB1.climb(Pose.LIFTED.obiAngle, -0.3 * (OB1.angle - thetaOffset).cos())
         OB1.intake(-0.7)
         Drive.drive(Vector2(0.0, 0.4 * OI.driverController.rightTrigger), OI.driveRotation * 0.4, false)
         Armavator.heightSetpoint = Pose.LIFTED.elevatorHeight
         Armavator.angleSetpoint = Pose.LIFTED.armAngle
         if (OI.driverController.x) stop()
     }

     periodic {
         OB1.climb(Pose.LIFTED.obiAngle, -0.3 * (OB1.angle - thetaOffset).cos())
         OB1.intake(-0.7)
         Drive.drive(Vector2(0.0, 0.15), 0.0, false)
         Armavator.isClimbing = false
         OB1.isClimbing = false
         Armavator.heightSetpoint = Pose.CLIMB_LIFT_ELEVATOR.elevatorHeight
         Armavator.angleSetpoint = Pose.CLIMB_LIFT_ELEVATOR.armAngle
//            Armavator.angleSetpoint = Armavator.height.asInches.linearMap(
//                Pose.LIFTED.armAngle.asDegrees..Pose.HOME.armAngle.asDegrees,
//                Pose.LIFTED.elevatorHeight.asInches..Pose.HOME.elevatorHeight.asInches
//            ).degrees
     }
 }*/
//}
//            goToPose(Pose.CLIMB_LIFT_ELEVATOR)
//            val timer = Timer().apply { start() }
//            periodic {
//                 if (timer.get() >= 1.5) return@periodic stop()
//                OB1.intake(-0.7)
//                Drive.drive(Vector2(0.0, 0.4), 0.0, false)
//            }
//            goToPose(Pose.LIFTED)

//suspend fun climb2() {
//   val body: suspend CoroutineScope.() -> Unit = {
/* goToPose(Pose.BEFORE_CLIMB)
goToPose(Pose.CLIMB_START)
suspendUntil { OI.startClimb }

use(Drive) {
periodic {
    Drive.drive(Vector2(0.0, 0.4 * OI.driverController.rightTrigger), OI.driveRotation * 0.4, false)
    OB1.intake(-0.4)
    OB1.isClimbing = true
    Armavator.isClimbing = true
    Armavator.heightSetpoint = Pose.LIFTED2.elevatorHeight
    OB1.angleSetpoint = Pose.LIFTED2.obiAngle
    Armavator.angleSetpoint = Pose.LIFTED2.armAngle
    if (OI.driverController.x) stop()
}
periodic {
    OB1.intake(-0.5)
    Drive.drive(Vector2(0.0, 0.15), 0.0, false)
//            Armavator.isClimbing = false
    OB1.isClimbing = false
    OB1.angleSetpoint = Pose.LIFTED2.obiAngle
    Armavator.heightSetpoint = Pose.CLIMB_LIFT_ELEVATOR.elevatorHeight
    Armavator.angleSetpoint = Pose.CLIMB_LIFT_ELEVATOR.armAngle
}
}*/

//   }
//}

suspend fun climb() = use(Armavator, OB) {
    try {
        goToPose(Pose.BEFORE_CLIMB)
        OB.angleSetpoint = 120.0.degrees
        delay(2.0)
        suspendUntil { OI.driverController.x }
        Armavator.isClimbing = true
        val obCurve = MotionCurve().apply {
            storeValue(0.0, 120.0)
            storeValue(2.5, 0.0)
        }

        val elevatorCurve = MotionCurve().apply {
            storeValue(0.0, Pose.BEFORE_CLIMB.elevatorHeight.asInches)
            storeValue(1.5, Pose.LIFTED.elevatorHeight.asInches)
        }

        val timer = Timer().apply { start() }
        use(Drive) {
            periodic {
                val time = timer.get()//.coerceAtMost(2.0)
                Armavator.heightSetpoint = elevatorCurve.getValue(time).inches
                OB.climb(obCurve.getValue(time).degrees)

                OB.climbDrive(1.0)
                Drive.drive(Vector2(0.0, 0.45), 0.0, fieldCentric = false)
                if (OI.driverController.x && time > obCurve.tailKey.time)
                    stop()
                if(OI.driverController.b)
                    stop()

            }
            Drive.stop()
        }

        val armCurve = MotionCurve().apply {
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
                Drive.drive(Vector2(0.0, OI.driveClimbDrive), 0.0, false)
                Armavator.heightSetpoint = elevatorCurve2.getValue(time).inches
                Armavator.angleSetpoint = armCurve.getValue(time).degrees
                OB.angleSetpoint = obCurve2.getValue(time).degrees
                if (OI.driverController.b)
                    stop()
            }
        }
    } finally {
        withContext(NonCancellable) {
            OB.angleSetpoint = 180.degrees
            suspendUntil {
                DriverStation.getInstance().isDisabled ||
                        Math.abs(((OB.leftAngle + OB.rightAngle) / 2.0 - OB.angleSetpoint).asDegrees) < 5
            }
        }
    }
}

suspend fun climb2() = use(Armavator, OB) {
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
            storeValue(1.0, Pose.LIFTED2.elevatorHeight.asInches)
        }

        val timer = Timer().apply { start() }
        use(Drive) {
            periodic {
                val time = timer.get()//.coerceAtMost(2.0)
                Armavator.heightSetpoint = elevatorCurve.getValue(time).inches
                OB.climb(obCurve.getValue(time).degrees)

                OB.climbDrive(1.0)
                Drive.drive(Vector2(0.0, 0.45), 0.0, fieldCentric = false)
                if (OI.driverController.x && time > obCurve.tailKey.time)
                    stop()
                if(OI.driverController.b)
                    stop()

            }
            Drive.stop()
        }

        val armCurve = MotionCurve().apply {
            storeValue(0.0, Pose.LIFTED2.armAngle.asDegrees)
            storeValue(1.5, Pose.AFTER_LIFTED2.armAngle.asDegrees)
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
                Armavator.angleSetpoint = armCurve.getValue(time).degrees
                Drive.drive(Vector2(0.0, OI.driveClimbDrive), 0.0, false)
                if (OI.driverController.b)
                    stop()
            }
        }
    } finally {
        withContext(NonCancellable) {
            OB.angleSetpoint = 180.degrees
            suspendUntil {
                DriverStation.getInstance().isDisabled ||
                        Math.abs(((OB.leftAngle + OB.rightAngle) / 2.0 - OB.angleSetpoint).asDegrees) < 5
            }
        }
    }
}