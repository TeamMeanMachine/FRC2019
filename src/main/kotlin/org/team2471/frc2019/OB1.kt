package org.team2471.frc2019

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.actuators.TalonSRX
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.asDegrees
import org.team2471.frc.lib.units.degrees
import javax.xml.bind.JAXBElement

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
suspend fun OB1.intakeHatch() = use(OB1) {
    periodic {
        pivot(0.degrees)
        intake(-0.5)
    }
}

suspend fun OB1.intakeCargo() = use(OB1) {
    periodic {
        pivot(60.degrees)
        intake(-0.5)
    }
}