package org.team2471.frc2019.actions

import org.team2471.frc.lib.control.PDController
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc.lib.motion.following.stop
import org.team2471.frc2019.Drive
import org.team2471.frc2019.Jevois

const val DISTANCE_OFFSET = 24 //inches
const val ANGLE_OFFSET = 4 //degrees
const val SKEW_OFFSET = 5 //degrees

suspend fun driveToTarget() = use(Drive, Jevois, name = "Drive To Target") {
    Jevois.isLightEnabled = true
    val turnController = PDController(0.01, 4.0)
    val distanceController = PDController(0.01, 0.02)
    val strafeController = PDController(0.005, 0.0)

    periodic {
        val target = Jevois.target ?: return@periodic Drive.stop()
        val turn = turnController.update(target.angle.asDegrees - ANGLE_OFFSET)
        val distance = distanceController.update(target.distance.asInches - DISTANCE_OFFSET) + 0.1
        val strafe = strafeController.update(target.skew.asDegrees - SKEW_OFFSET)

        println(target)
        Drive.drive(Vector2(strafe, distance), turn, false)

    }
}