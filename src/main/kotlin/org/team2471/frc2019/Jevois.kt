package org.team2471.frc2019

import com.squareup.moshi.Moshi
import edu.wpi.first.wpilibj.SerialPort
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.VictorID
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion.following.driveAlongPath
import org.team2471.frc.lib.motion_profiling.Path2D
import org.team2471.frc.lib.units.*
import org.team2471.frc.lib.units.Angle.Companion.cos
import org.team2471.frc.lib.units.Angle.Companion.sin

object Jevois : Subsystem("Jevois") {
    data class Target(val distance: Length, val angle: Angle, val skew: Angle) {
        val position: Vector2
            get() = Vector2(distance.asInches * sin(angle), distance.asInches * cos(angle))
    }

    private val ledRingLight = MotorController(VictorID(Victors.LED_RING_LIGHT))

    var isLightEnabled = false
        set(value) {
            field = value
            ledRingLight.setPercentOutput(if (value) 1.0 else 0.0)
        }

    var targets: Array<Target> = emptyArray()
        private set

    override fun reset() {
        isLightEnabled = false
    }

    init {
        GlobalScope.launch(MeanlibDispatcher) {
            val serialPort = SerialPort(115200, SerialPort.Port.kUSB1)
            serialPort.enableTermination()

            // setup
            println("Starting jevois...")
            serialPort.writeString("setmapping2 YUYV 640 480 30 TeamMeanMachine DeepSpace\n")
            delay(5.0)

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
                }
            }
        }
    }
}

suspend fun driveToTarget() = use(Jevois) {
    Jevois.isLightEnabled = true
    lateinit var targets: Array<Jevois.Target>
    withTimeout(1000) {
        suspendUntil { targets = Jevois.targets; targets.isNotEmpty() }
    }
    var lastTarget = targets.minBy { it.distance.asInches }!!

    use(Drive) driving@ {
        val target = Jevois.targets.minBy { it.position.distance(lastTarget.position) } ?: return@driving
        val (distance, angle, skew) = target
        lastTarget = target
        println("Angle: $angle, skew: $skew, distance: ${distance.asFeet}")
        val position1 = Vector2(0.0, 0.0)
        val tangent1 = Vector2(0.0, (distance * 1.0).asFeet)

        val fieldAngle = angle - skew

        val position2 = Vector2(0.0, distance.asFeet).rotateDegrees(-angle.asDegrees) +
                Vector2(-8.0 / 12.0, 0.0) +
                Vector2(0.0, -24.0 / 12.0).rotateDegrees(-fieldAngle.asDegrees)

        println("position2: $position2")
        val tangent2 = Vector2(0.0, (distance * 1.0).asFeet).rotateDegrees(fieldAngle.asDegrees)
        println("tangent2: $tangent2")
        val robotPosition = RobotPosition(Drive.position, Drive.heading)
        println("robot position: ${robotPosition.position}, robot angle: ${robotPosition.angle}")
        val initialPathPoint = robotToField(RobotPathPoint(position1, tangent1), robotPosition)
        val finalPathPoint = robotToField(RobotPathPoint(position2, tangent2), robotPosition)

        val path = Path2D().apply {
            robotDirection = Path2D.RobotDirection.FORWARD
            addPointAndTangent(
                initialPathPoint.position.x, initialPathPoint.position.y,
                initialPathPoint.tangent.x, initialPathPoint.tangent.y
            )
            addPointAndTangent(
                finalPathPoint.position.x, finalPathPoint.position.y,
                finalPathPoint.tangent.x, finalPathPoint.position.y
            )
            val speed = 5.0
            val time = (distance.asFeet/speed).seconds
            addEasePoint(0.0, 0.0)
            addEasePoint(time.asSeconds, 1.0)
            addHeadingPoint(0.0, Drive.heading.asDegrees)
            addHeadingPoint(time.asSeconds, Drive.heading.asDegrees + fieldAngle.asDegrees)
        }

        println("Initial Path Point: ${initialPathPoint.position} , ${initialPathPoint.tangent}")
        println("Final Path Point: ${finalPathPoint.position} , ${finalPathPoint.tangent}")
        println("Field angle: ${fieldAngle.asDegrees}")
        Jevois.isLightEnabled = false
        Drive.driveAlongPath(path)
    }
}


data class RobotPosition(val position: Vector2, val angle: Angle)
data class RobotPathPoint(val position: Vector2, val tangent: Vector2)

fun robotToField(robotPathPoint: RobotPathPoint, robotPosition: RobotPosition): RobotPathPoint {
    return RobotPathPoint(
        robotPathPoint.position.rotateDegrees(-Drive.heading.asDegrees) + robotPosition.position,
        Vector2(0.0, 1.0).rotateRadians(-robotPathPoint.tangent.angle - robotPosition.angle.asRadians) *
                robotPathPoint.tangent.length
    )
}