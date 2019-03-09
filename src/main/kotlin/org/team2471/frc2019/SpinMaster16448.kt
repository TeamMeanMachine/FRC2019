package org.team2471.frc2019

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.SerialPort
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.interfaces.Gyro
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.delay
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.concurrent.thread

class SpinMaster16448 : Gyro {
    @Volatile
    private var yaw = 0.0
    @Volatile
    private var yawOffset = 0.0

    override fun calibrate() = Unit

    override fun getAngle() = yaw + yawOffset

    override fun getRate() = 0.0

    override fun reset() {
        yawOffset = -yaw
        println("Yaw: $yaw, yawOffset: $yawOffset")
    }

    override fun close() = Unit

    override fun free() = Unit

    init {
        thread {
            while (true) {
                try {
                    println("Initializing SpinMaster")
                    val serialPort = SerialPort(115200, SerialPort.Port.kMXP).apply {
                        enableTermination()
                    }
                    while (true) {
                        val message = serialPort.read(5)

                        yaw = if (message.size < 4) {
                            0.0
                        } else {
                            val buffer = ByteBuffer.wrap(message).order(ByteOrder.LITTLE_ENDIAN)
                            buffer.float.toDouble()
                        }
                    }
                } catch (e: Throwable) {
                    DriverStation.reportError("IMU Failure", e.stackTrace)
                    Timer.delay(2.0)
                }
            }
        }
    }
}
