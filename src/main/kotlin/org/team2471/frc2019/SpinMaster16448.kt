package org.team2471.frc2019

import edu.wpi.first.wpilibj.SerialPort
import edu.wpi.first.wpilibj.interfaces.Gyro
import org.team2471.frc.lib.coroutines.periodic
import kotlin.concurrent.thread

class SpinMaster16448 : Gyro {
    private var yaw = 0.0
    private var yawOffset = 0.0

    override fun calibrate() = Unit

    override fun getAngle() = yaw + yawOffset

    override fun getRate() = 0.0

    override fun reset() {
        yawOffset = -yaw
    }

    override fun close() = Unit

    override fun free() = Unit

    init {
//        thread {
//            val serialPort = SerialPort(115200, SerialPort.Port.kMXP).apply {
//                enableTermination()
//            }
//            while (true) {
//                val message = serialPort.readString()
//                yaw = message.toDoubleOrNull() ?: continue
//            }
//        }
    }
}
