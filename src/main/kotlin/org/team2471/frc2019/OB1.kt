package org.team2471.frc2019

import org.team2471.frc.lib.actuators.TalonSRX
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.asDegrees

object OB1: Subsystem("OB1") {
    private val pivotMotors = TalonSRX(Talons.OB1_PIVOT_MASTER, Victors.OB1_PIVOT_SLAVE).config {
        currentLimit(25, 0, 0)
    }

    private val intakeMotor = TalonSRX(Talons.OB1_INTAKE)

    fun pivot(angle: Angle) {
        pivotMotors.setMotionMagicSetpoint(angle.asDegrees)
    }

    fun intake(power: Double) {
        intakeMotor.setPercentOutput(power)
    }


}
