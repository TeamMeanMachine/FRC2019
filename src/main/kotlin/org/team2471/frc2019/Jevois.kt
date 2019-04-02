package org.team2471.frc2019

import com.squareup.moshi.Moshi
import edu.wpi.cscore.MjpegServer
import edu.wpi.cscore.UsbCamera
import edu.wpi.cscore.VideoMode
import edu.wpi.first.wpilibj.DigitalOutput
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.SerialPort
import edu.wpi.first.wpilibj.Timer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.VictorID
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion.following.SwerveDrive
import org.team2471.frc.lib.motion.following.lookupPose
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.Angle.Companion.cos
import org.team2471.frc.lib.units.Angle.Companion.sin
import org.team2471.frc.lib.units.Length
import org.team2471.frc.lib.units.Time
import org.team2471.frc.lib.units.asRadians

object Jevois : Subsystem("Jevois") {
    val connected
        get() = pongTimer.get() < 5.0

    private const val PING_INTERVAL = 1.0

    private val pingTimer = Timer().apply { start() }
    private val pongTimer = Timer().apply { start() }

    private val serialPort = try {
        SerialPort(115200, SerialPort.Port.kUSB1).apply {
            enableTermination()
        }
    } catch (_: Throwable) {
        null
    }

    private val ledRingLight = MotorController(VictorID(Victors.LED_RING_LIGHT))

    var isLightEnabled = false
        set(value) {
            if (field != value) {
                if (value) {
                    serialPort?.writeString("setcam absexp 3\n")
                    serialPort?.writeString("setcam gain 16\n")
                } else {
                    serialPort?.writeString("setcam absexp 1000\n")
                    serialPort?.writeString("setcam gain 100\n")
                }
            }

            field = value
            ledRingLight.setPercentOutput(if (value) 0.5 else 0.0)
        }

    var data: Data? = null
        private set

    override fun reset() {
        isLightEnabled = false
    }

    private val camera = UsbCamera("Jevois", 0).apply {
        setResolution(320, 240)
        setFPS(30)
//        setPixelFormat(VideoMode.PixelFormat.kYUYV)
        setPixelFormat(VideoMode.PixelFormat.kMJPEG)
    }

    private val server = MjpegServer("Camera Server", 5805).apply {
        source = camera
    }

    init {
        GlobalScope.launch(MeanlibDispatcher) {
            if (serialPort == null) return@launch
            serialPort.enableTermination()
            // setup
            println("Starting jevois...")
            serialPort.writeString("setmapping2 YUYV 640 480 30 TeamMeanMachine DeepSpace\n")
            delay(5.0)

            serialPort.writeString("setpar serout USB\n")
            serialPort.writeString("streamon\n")

            val dataAdapter = Moshi.Builder().build().adapter(Data::class.java)

            while (true) {
                redOutput.set(!connected)

                if (pingTimer.get() > PING_INTERVAL) {
                    serialPort.writeString("PING\n")
                    pingTimer.reset()
                }

                if (serialPort.bytesReceived == 0) {
                    delay(2)
                    continue
                }

                val data = serialPort.readString().takeWhile { it != '\n' }

                when {
                    data.startsWith("PONG") -> pongTimer.reset()
                    data.startsWith("TIME") -> serialPort.writeString("TIME ${Timer.getFPGATimestamp()}\n")
                    data.startsWith("DATA") -> try {
                        this@Jevois.data = dataAdapter.fromJson(data.drop(5))!!
                    } catch (_: Throwable) {
                        println("Failed to parse $data")
                    }
                }
            }
        }
    }

    data class Data(val time: Time, val target: Target?)

    data class Target(val distance: Length, val angle: Angle, val skew: Angle) {
        val position: Vector2
            get() = Vector2(distance.asInches * sin(angle), distance.asInches * cos(angle))
    }
}

//
//suspend fun driveToTarget() = use(Jevois) {
//
//    use(Drive) driving@{
//        Jevois.isLightEnabled = true
//        val translateYController = PDController(0.0, 0.0)
//        val translateXController = PDController(0.0, 0.0)
//        val turnController = PDController(0.01, 0.0)
//
//        val targetDistance = 1.858.feet
//        val targetAngle = 14.8.degrees
//        val targetSkew = 1.degrees
//
//        periodic {
//            val target = Jevois.target
//            if (target != null) {
//                val (d, a, s) = target
//                val distance = d
//                val angle = a
//                val skew = (s) * (1 - .1 * distance.asFeet)
//
//                val distError = targetDistance - distance
//                val angleError = targetAngle - angle
//                val skewError = targetSkew - skew
//
//                var translation = Vector2(0.0, 0.0)
//
//                val translateXError = (angleError + skewError).wrap()
//                val turnError = (skewError - angleError).wrap()
////                println("Turn Error: $turnError, Angle Error: $angleError, Skew Error: $skewError")
//
//                translation.x = translateXController.update(translateXError.asDegrees)
//                translation.y = translateYController.update(distError.asFeet)
//                val turn = turnController.update(turnError.asDegrees)
//
//                Drive.drive(translation, turn, false)
//
//                if (Math.abs(turnError.asDegrees) < 2.0) {
//                    return@periodic
//                }
//            }
//        }
//    }
//}

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
//            lateinit var target: Array<Jevois.Target>
//            withTimeout(1000) {
//                suspendUntil { target = Jevois.target; target.isNotEmpty() }
//            }
//            val target = target.minBy { it.distance.asInches }!!
//
////            val target = Jevois.target.minBy { it.position.distance(lastTarget.position) } ?: return@driving
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
