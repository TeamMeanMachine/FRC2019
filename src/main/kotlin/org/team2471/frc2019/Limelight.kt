package org.team2471.frc2019

import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DigitalOutput
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.control.PDController
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.math.linearMap
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc.lib.units.degrees
import org.team2471.frc2019.actions.ScoringPosition
import kotlin.math.absoluteValue
import kotlin.math.sqrt

object Limelight : Subsystem("Limelight") {
    private val table = NetworkTableInstance.getDefault().getTable("limelight")
    private val thresholdTable = table.getSubTable("thresholds")
    private val xEntry = table.getEntry("tx")
    private val areaEntry = table.getEntry("ta")
    private val camModeEntry = table.getEntry("camMode")
    private val ledModeEntry = table.getEntry("ledMode")
    private val targetValidEntry = table.getEntry("tv")

    private val useAutoPlaceEntry = table.getEntry("Use Auto Place").apply {
        setPersistent()
        setDefaultBoolean(true)
    }

    private val highHatchEntry = thresholdTable.getEntry("High Hatch").apply {
        setPersistent()
        setDefaultDouble(12.0)
    }
    private val middleHatchEntry = thresholdTable.getEntry("Middle Hatch").apply {
        setPersistent()
        setDefaultDouble(5.0)
    }
    private val lowHatchEntry = thresholdTable.getEntry("Low Hatch").apply {
        setPersistent()
        setDefaultDouble(10.0)
    }
    private val feederHatchEntry = thresholdTable.getEntry("Feeder Station Hatch").apply {
        setPersistent()
        setDefaultDouble(7.3)
    }

    var isCamEnabled = false
        set(value) {
            field = value
//            camModeEntry.setDouble(if (field) 0.0 else 1.0)
//            ledModeEntry.setDouble(if (field) 0.0 else 1.0)
            camModeEntry.setDouble(0.0)
//            ledModeEntry.setDouble(0.0)
        }

    var ledEnabled = false
        set(value) {
            field = value
            ledModeEntry.setDouble(if (value) 0.0 else 1.0)
        }

    val xTranslation
        get() = xEntry.getDouble(0.0)

    val area
        get() = areaEntry.getDouble(0.0)

    var hasValidTarget = false
        get() = targetValidEntry.getDouble(0.0) == 1.0

    init {
        isCamEnabled = false
//            camModeEntry.setDouble(0.0)
//            ledModeEntry.setDouble(0.0)
        GlobalScope.launch(MeanlibDispatcher) {
            periodic {
                if (hasValidTarget) { // target valid
                    setLEDColor(false, true, false)
                } else {
                    setLEDColor(true, false, false)
                }
            }
        }
    }

    fun isAtTarget(): Boolean {
        return area > feederHatchEntry.value.double
    }

    fun isAtTarget(position: ScoringPosition): Boolean {
        return useAutoPlaceEntry.value.boolean && area > when (position) {
            ScoringPosition.ROCKET_LOW -> lowHatchEntry.value.double
            ScoringPosition.ROCKET_MED -> middleHatchEntry.value.double
            ScoringPosition.ROCKET_HIGH -> highHatchEntry.value.double
            ScoringPosition.CARGO_SHIP -> lowHatchEntry.value.double
        }
    }

    override fun reset() {
//        isCamEnabled = false
    }
}

private val angles = doubleArrayOf(-150.0, -90.0, -30.0, 0.0, 30.0, 90.0, 150.0, 180.0)


suspend fun visionDrive() = use(Drive, Limelight, name = "Vision Drive") {
    Limelight.isCamEnabled = true
    val translationPDController = PDController(0.033, 0.0)
    val distanceK = 20.0
    val smallestAngle = angles.minBy { (Drive.heading - it.degrees).wrap().asDegrees.absoluteValue }!!
    val kTurn = 0.0 //0.007

    periodic {
        val speed = sqrt(Limelight.area).linearMap(sqrt(0.4)..sqrt(8.5), 0.4..0.15) //0.05

        val visionVector =
            Vector2(translationPDController.update(Limelight.xTranslation), OI.driverController.leftTrigger * speed)
        Vector2(translationPDController.update(Limelight.xTranslation), OI.driverController.leftTrigger * speed)
        val turnError = (smallestAngle.degrees - Drive.heading).wrap()
        println("Target angle: $smallestAngle, error: $turnError")

        Drive.drive(
            OI.driveTranslation,
            OI.driveRotation,
            SmartDashboard.getBoolean("Use Gyro", true) && !DriverStation.getInstance().isAutonomous,
            (OI.operatorTranslation + visionVector),
            OI.operatorRotation + turnError.asDegrees * kTurn
        )
    }
}

val blueOutput = DigitalOutput(0)
val redOutput = DigitalOutput(1)
val greenOutput = DigitalOutput(2)

fun setLEDColor(red: Boolean, green: Boolean, blue: Boolean) {
    redOutput.set(red)
    greenOutput.set(green)
    blueOutput.set(blue)
}

