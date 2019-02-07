package org.team2471.frc2019

import org.team2471.frc.lib.actuators.TalonSRX
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.Length
import org.team2471.frc.lib.units.asDegrees

object Elevator: Subsystem("Elevator") {
    private val elevatorMotors = TalonSRX(Talons.ELEVATOR_MASTER, Victors.ELEVATOR_SLAVE)

    private val armMotors = TalonSRX(Talons.ARM_MASTER, Victors.ARM_SLAVE)

    fun fourBar(angle: Angle) {
        armMotors.setMotionMagicSetpoint(angle.asDegrees)
    }

    fun elevate(height: Length) {
        elevatorMotors.setMotionMagicSetpoint(height.asInches)
    }

    suspend fun Elevator.startingPosition() = use(Elevator) {

    }

}