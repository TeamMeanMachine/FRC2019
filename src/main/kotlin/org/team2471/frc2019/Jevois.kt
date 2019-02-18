package org.team2471.frc2019

import edu.wpi.first.wpilibj.DriverStation
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.Angle.Companion.cos
import org.team2471.frc.lib.units.Angle.Companion.sin
import org.team2471.frc.lib.units.Length
import org.team2471.frc.lib.units.asDegrees

object Jevois {
    data class Target(val distance: Length, val angle: Angle, val skew: Angle) {
        val position: Vector2 by lazy { Vector2(distance.asInches * sin(angle), distance.asInches * cos(angle)) }
    }

    val targets: Array<Target>
        get() = emptyArray()
}

suspend fun driveToTarget() {
    var lastTarget = Jevois.targets.minBy { it.distance.asInches }
        ?: return DriverStation.reportWarning("There isn't a target, ya dingus", false)

    use(Drive) {
        periodic {
            val target = Jevois.targets.minBy { it.position.distance(lastTarget.position) } ?: return@periodic
            val (distance, angle, skew) = target
            lastTarget = target

            Drive.drive(target.position, angle.asDegrees)
        }
    }
}