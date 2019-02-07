package org.team2471.frc2019

import org.team2471.frc.lib.framework.RobotProgram
import org.team2471.frc.lib.framework.initializeWpilib
object Robot: RobotProgram {
    override suspend fun autonomous() {
        super.autonomous()
    }

    override suspend fun teleop() {
        super.teleop()
    }

    override suspend fun disable() {
        Drive.stop()
    }

    override suspend fun test() {
        super.test()
    }
}

fun main() {
    initializeWpilib()
    Drive.enable()
    Elevator.enable()
    OB1.enable()
}