package org.team2471.frc2019.actions

import edu.wpi.first.wpilibj.Timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.math.linearMap
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.inches
import org.team2471.frc2019.*


suspend fun climb() = use(Armavator) {
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

        periodic {
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
}
//            goToPose(Pose.CLIMB_LIFT_ELEVATOR)
//            val timer = Timer().apply { start() }
//            periodic {
//                 if (timer.get() >= 1.5) return@periodic stop()
//                OB1.intake(-0.7)
//                Drive.drive(Vector2(0.0, 0.4), 0.0, false)
//            }
//            goToPose(Pose.LIFTED)

suspend fun climb2() {
   val body: suspend CoroutineScope.() -> Unit = {
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

   }
}