package org.team2471.frc2019

import com.squareup.moshi.Moshi
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.SerialPort
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.TalonID
import org.team2471.frc.lib.actuators.VictorID
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.Angle.Companion.cos
import org.team2471.frc.lib.units.Angle.Companion.sin
import org.team2471.frc.lib.units.Length

object Jevois {
    data class Target(val distance: Length, val angle: Angle, val skew: Angle) {
        val position: Vector2
            get() = Vector2(distance.asInches * sin(angle), distance.asInches * cos(angle))
    }

    val ledRingLight = MotorController(VictorID(Victors.LED_RING_LIGHT))

    var isEnabled = false
        set(value) {
            field = value
            ledRingLight.setPercentOutput(if (value) 0.5 else 0.0)
        }

    var targets: Array<Target> = emptyArray()
        private set

    init {
        GlobalScope.launch(MeanlibDispatcher) {
            val serialPort =
                SerialPort(115200, SerialPort.Port.kUSB1, 8, SerialPort.Parity.kNone, SerialPort.StopBits.kOne)
            serialPort.enableTermination()

            // setup
            while (true) {
                println("Starting jevois...")
                serialPort.writeString("setmapping2 YUYV 640 480 30 TeamMeanMachine DeepSpace\n")
                delay(5.0)

                val response = serialPort.readString()
                if (response.startsWith("OK")) {
                    break
                }
                DriverStation.reportWarning(
                    if (response.isEmpty()) {
                        "No response from jevois"
                    } else {
                        "Jevois failed to initialize. Received '$response'"
                    }, false
                )
            }

            println("Jevois started")
            serialPort.writeString("setpar serout USB\n")
            serialPort.writeString("streamon\n")

            val dataAdapter = Moshi.Builder().build().adapter(Array<Target>::class.java)

            while (true) {
                if (serialPort.bytesReceived == 0) {
                    delay(1.0 / 30.0)
                    continue
                }

                val data = serialPort.readString().takeWhile { it != '\n' }
                try {
                    targets = dataAdapter.fromJson(data)!!
                } catch (_: Throwable) {
                    println("Jevois: $data")
                }
            }
        }
    }
}

suspend fun driveToTarget() {

    var lastTarget = Jevois.targets.minBy { it.distance.asInches }
        ?: return DriverStation.reportWarning("There isn't a target, ya dingus", false)

    use(Drive) {
        periodic {
            val target = Jevois.targets.minBy { it.position.distance(lastTarget.position) } ?: return@periodic
            val (distance, angle, skew) = target
            lastTarget = target

            Drive.drive(target.position, angle.asDegrees, false)
        }
    }
}