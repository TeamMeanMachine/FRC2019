package org.team2471.frc2019.actions

import org.team2471.frc.lib.control.PDController
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc.lib.motion.following.stop
import org.team2471.frc.lib.units.asRadians
import org.team2471.frc.lib.units.degrees
import org.team2471.frc2019.Drive
import org.team2471.frc2019.Jevois

const val DISTANCE_OFFSET = 18 //inches
const val ANGLE_OFFSET = 5 //degrees
const val SKEW_OFFSET = 5 //degrees

suspend fun driveToTarget() = use(Drive, Jevois, name = "Drive To Target") {
    Jevois.isLightEnabled = true
    val turnController = PDController(0.01, 6.0)
    val distanceController = PDController(0.008, 0.02)
    val strafeController = PDController(0.005, 0.0)

    periodic {
        val target = Jevois.target ?: return@periodic Drive.stop()
        val turnError = target.angle - ANGLE_OFFSET.degrees
        var turn = turnController.update(turnError.asDegrees)
        val distance = distanceController.update(target.distance.asInches - DISTANCE_OFFSET) + 0.0
        val strafe = strafeController.update(target.skew.asDegrees - SKEW_OFFSET)

        println(target)

        val powerCap = 0.2
        val x = Math.min(Math.max(turnError.sin()*distance, -powerCap), powerCap)
        val y = Math.min(Math.max(turnError.cos()*distance, 0.0), powerCap)
        turn = Math.min(Math.max(turn, -powerCap), powerCap)

        Drive.drive(Vector2(x, y), turn, false)
    }
}