package org.team2471.frc2019.actions

import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.motion.following.stop
import org.team2471.frc2019.Armavator
import org.team2471.frc2019.Drive
import org.team2471.frc2019.OB

suspend fun panic() = use(Armavator, Drive, OB){
    println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAH!")
    Armavator.angleSetpoint = Armavator.angle
    Armavator.heightSetpoint = Armavator.height
    Drive.stop()
}