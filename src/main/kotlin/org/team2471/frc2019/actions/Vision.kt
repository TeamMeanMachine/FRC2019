package org.team2471.frc2019.actions

import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc2019.Drive
import org.team2471.frc2019.Jevois

suspend fun turnToTarget() = use(Drive, Jevois, name="Turn To Target"){
    Jevois.isLightEnabled = true
    periodic {
        val kTurn = 0.01
        var turnError = Jevois.targets[0].angle
        Drive.drive(Vector2(0.0,0.0), kTurn * turnError.asDegrees)
    }

}