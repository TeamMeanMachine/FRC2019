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
const val ANGLE_OFFSET = 9 //degrees
const val SKEW_OFFSET = 5 //degrees

suspend fun driveToTarget() = use(Drive, Jevois, name = "Drive To Target") {
    Jevois.isLightEnabled = true
    val turnController = PDController(0.008, 0.0)
    val distanceController = PDController(0.008, 0.02)
    val strafeController = PDController(0.005, 0.0)

    periodic {
        val data = Jevois.data
        val target = data!!.target
        if (target == null) {
            Jevois.redOutput.set(false)
            Drive.stop()
            return@periodic
        }
        Jevois.redOutput.set(true)
        val turnError = target.angle - ANGLE_OFFSET.degrees
        var turn = turnController.update(turnError.asDegrees)
        val distance = 0.2 // distanceController.update(target.distance.asInches - DISTANCE_OFFSET) + 0.05
        val strafe = strafeController.update(target.skew.asDegrees - SKEW_OFFSET)

        println(target)

        val powerCap = 0.3
        val x = (turnError.sin() * distance).coerceIn(-powerCap..powerCap)
        val y = (turnError.cos() * distance).coerceIn(0.0..powerCap)
        turn = turn.coerceIn(-powerCap..powerCap)

        Drive.drive(Vector2(x, y), turn, false)
    }
}