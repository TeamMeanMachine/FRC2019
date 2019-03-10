package org.team2471.frc2019

import com.analog.adis16448.frc.ADIS16448_IMU
import edu.wpi.first.wpilibj.interfaces.Gyro

class ADISWrapper : Gyro {
    private val imu = ADIS16448_IMU()

    override fun getAngle(): Double = imu.angleY

    override fun getRate(): Double = imu.rateY

    override fun close() = imu.close()

    override fun calibrate() = imu.calibrate()

    override fun reset() = imu.reset()

    override fun free() = imu.free()
}