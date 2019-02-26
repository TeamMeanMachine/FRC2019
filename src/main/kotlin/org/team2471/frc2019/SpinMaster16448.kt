package org.team2471.frc2019

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.SerialPort
import edu.wpi.first.wpilibj.interfaces.Gyro
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.delay

class SpinMaster16448 : Gyro {
    @Volatile
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
        GlobalScope.launch(MeanlibDispatcher) {
            while (true) {
                try {
                    val serialPort = SerialPort(115200, SerialPort.Port.kMXP).apply {
                        enableTermination()
                    }
                    while (true) {
                        val message = serialPort.readString()

                        if (message.isEmpty()) {
                            delay(0.01)
                        } else {
                            yield()
                        }

                        yaw = message.toDoubleOrNull() ?: continue
                    }
                } catch (e: Throwable) {
                    DriverStation.reportError("IMU failed", e.stackTrace)
                    delay(15.0)
                }
            }
        }
    }
}
