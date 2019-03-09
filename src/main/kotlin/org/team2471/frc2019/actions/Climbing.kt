package org.team2471.frc2019.actions

import edu.wpi.first.wpilibj.Timer
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc2019.*

suspend fun climb() = use(Armavator, OB1) {

    goToPose(Pose.CLIMB_START)
    suspendUntil { OI.startClimb }

    // climbing cannot be canceled in this stage
    withContext(NonCancellable) {
        use(Drive) {
            goToPose(Pose.CLIMB_LIFT_ELEVATOR)
            val timer = Timer().apply { start() }
            periodic {
                 if (timer.get() >= 1.5) return@periodic stop()
                OB1.intake(-0.7)
                Drive.drive(Vector2(0.0, 0.6), 0.0, false)
            }
            goToPose(Pose.LIFTED)
        }
    }
}
