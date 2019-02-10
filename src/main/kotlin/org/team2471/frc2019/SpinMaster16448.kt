package org.team2471.frc2019

import edu.wpi.first.wpilibj.interfaces.Gyro

class SpinMaster16448 : Gyro {
    override fun calibrate() = Unit

    override fun getAngle() = 0.0

    override fun getRate() = 0.0

    override fun reset() = Unit

    override fun close() = Unit

    override fun free() = Unit
}
