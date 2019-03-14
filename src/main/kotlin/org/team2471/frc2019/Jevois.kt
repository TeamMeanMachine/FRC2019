package org.team2471.frc2019

import com.squareup.moshi.Moshi
import edu.wpi.cscore.MjpegServer
import edu.wpi.cscore.UsbCamera
import edu.wpi.cscore.VideoMode
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.SerialPort
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.VictorID
import org.team2471.frc.lib.control.PDController
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion.following.drive
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
            ledRingLight.setPercentOutput(if (value) 0.6 else 0.0)
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

            val camera = UsbCamera("Jevois", 0).apply {
                setResolution(320,240)
                setFPS(20)
                setPixelFormat(VideoMode.PixelFormat.kMJPEG)
            }

            val stream = MjpegServer("Server", 5810).apply {
                source = camera
            }

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
    val table = NetworkTableInstance.getDefault().getTable(Jevois.name)

    use(Drive) driving@{
        Jevois.isLightEnabled = true
        val translateYController = PDController(0.0, 0.0)
        val translateXController = PDController(0.0, 0.0)
        val turnController = PDController(0.01, 0.0)

        val targetDistance = 1.858.feet
        val targetAngle = 14.8.degrees
        val targetSkew = 1.degrees

        periodic {
            val targets: Array<Jevois.Target> = Jevois.targets
            if (targets.isNotEmpty()) {
                val target = targets.minBy { it.distance.asInches }!!
                val (d, a, s) = target
                val distance = d
                val angle = a
                val skew = (s) * (1 - .1 * distance.asFeet)

                val turn = a - s

                val targetTurn = targetAngle - targetSkew

                val distError = targetDistance - distance
                val angleError = targetAngle - angle
                val skewError = targetSkew - skew

                var translation = Vector2(0.0, 0.0)

                val translateXError = (angleError + skewError).wrap()
                val turnError = (targetTurn - turn).wrap()
                table.getEntry("Turn Error").setDouble(turnError.asDegrees)
                println("Turn Error: $turnError, Angle Error: $angleError, Skew Error: $skewError")

                translation.x = translateXController.update(translateXError.asDegrees)
                translation.y = translateYController.update(distError.asFeet)
                val rotation = turnController.update(turnError.asDegrees)

                Drive.drive(translation, rotation, false)

                if (Math.abs(turnError.asDegrees) < 2.0) {
                    stop()
                }
            }
        }
    }
}

//
//suspend fun driveToTarget() = use(Jevois) {
//
//    use(Drive) driving@{
//
//        val targetPositions = ArrayList<RobotPathPoint>()
//        Jevois.isLightEnabled = true
//        var totalDistance = 0.feet
//        var totalAngle = 0.degrees
//        var totalSkew = 0.degrees
//        repeat(5) {
//            lateinit var targets: Array<Jevois.Target>
//            withTimeout(1000) {
//                suspendUntil { targets = Jevois.targets; targets.isNotEmpty() }
//            }
//            val target = targets.minBy { it.distance.asInches }!!
//
////            val target = Jevois.targets.minBy { it.position.distance(lastTarget.position) } ?: return@driving
//            val (d, a, s) = target
////            lastTarget = target
//            totalDistance += d
//            totalAngle += a
//            totalSkew += s
//            println("SKEW: $s")
//            delay(100)
//        }
//        val distance = totalDistance/5.0
//        val angle = totalAngle/5.0
//        val skew = (totalSkew/5.0) * (1 - .1 * distance.asFeet)
//        println("Angle: $angle, skew: $skew, distance: ${distance.asFeet}")
//        val position1 = Vector2(0.0, 0.0)
//        val tangent1 = Vector2(0.0, (distance * 0.0).asFeet)
//
//        val fieldAngle = angle - skew
//
//        val position2 = Vector2(0.0, distance.asFeet).rotateDegrees(-angle.asDegrees) +
//                Vector2(-8.0 / 12.0, 0.0) +
//                Vector2(0.0, -24.0 / 12.0).rotateDegrees(-fieldAngle.asDegrees)
//
//        println("position2: $position2")
//        val tangent2 = Vector2(0.0, (distance * 0.0).asFeet).rotateDegrees(-fieldAngle.asDegrees)
//        println("tangent2: $tangent2")
//        val robotPosition = RobotPosition(Drive.position, Drive.heading)
//        println("robot position: ${robotPosition.position}, robot angle: ${robotPosition.angle}")
//
//        val initialPathPoint = robotToField(RobotPathPoint(position1, tangent1), robotPosition)
//        var finalPathPoint = robotToField(RobotPathPoint(position2, tangent2), robotPosition)
//
//        targetPositions.add(finalPathPoint)
//
//        finalPathPoint = RobotPathPoint(Vector2(0.0, 0.0), Vector2(0.0, 0.0))
//        for (i in 0..(targetPositions.size - 1)) {
//            finalPathPoint = RobotPathPoint(
//                finalPathPoint.position + targetPositions[i].position,
//                finalPathPoint.tangent + targetPositions[i].tangent
//            )
//        }
//        finalPathPoint = RobotPathPoint(
//            finalPathPoint.position / targetPositions.size.toDouble(),
//            finalPathPoint.tangent / targetPositions.size.toDouble()
//        )
//
//        val path = Path2D().apply {
//            robotDirection = Path2D.RobotDirection.FORWARD
//            addPointAndTangent(
//                initialPathPoint.position.x, initialPathPoint.position.y,
//                initialPathPoint.tangent.x, initialPathPoint.tangent.y
//            )
//            addPointAndTangent(
//                finalPathPoint.position.x, finalPathPoint.position.y,
//                finalPathPoint.tangent.x, finalPathPoint.position.y
//            )
//            val speed = 2.0
//            val time = (distance.asFeet / speed + 0.5).seconds
//            addEasePoint(0.0, 0.0)
//            addEasePoint(time.asSeconds, 1.0)
//            addHeadingPoint(0.0, Drive.heading.asDegrees)
//            addHeadingPoint(time.asSeconds, Drive.heading.asDegrees + fieldAngle.asDegrees)
//        }
//
//        println("Initial Path Point: ${initialPathPoint.position} , ${initialPathPoint.tangent}")
//        println("Final Path Point: ${finalPathPoint.position} , ${finalPathPoint.tangent}")
//        println("Field angle: ${fieldAngle.asDegrees}")
//        Jevois.isLightEnabled = false
//        Drive.driveAlongPath(path)
//    }
//}


data class RobotPosition(val position: Vector2, val angle: Angle)
data class RobotPathPoint(val position: Vector2, val tangent: Vector2)

fun robotToField(robotPathPoint: RobotPathPoint, robotPosition: RobotPosition): RobotPathPoint {
    return RobotPathPoint(
        robotPathPoint.position.rotateDegrees(-Drive.heading.asDegrees) + robotPosition.position,
        Vector2(0.0, 1.0).rotateRadians(-robotPathPoint.tangent.angle - robotPosition.angle.asRadians) *
                robotPathPoint.tangent.length
    )
}