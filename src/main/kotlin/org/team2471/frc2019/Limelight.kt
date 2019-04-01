package org.team2471.frc2019

import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion.following.drive


object Limelight: Subsystem("Limelight") {
    private val table = NetworkTableInstance.getDefault().getTable("limelight")
    private val xEntry = table.getEntry("tx")
    private val areaEntry = table.getEntry("ta")
    private val camModeEntry = table.getEntry("camMode")
    private val ledModeEntry = table.getEntry("ledMode")

    var isCamEnabled = false
        set(value) {
            field = value
//            camModeEntry.setDouble(if (field) 0.0 else 1.0)
//            ledModeEntry.setDouble(if (field) 0.0 else 1.0)
            camModeEntry.setDouble(0.0)
            ledModeEntry.setDouble(0.0)
        }

    val xTranslation
        get() = xEntry.getDouble(0.0)

    val area
        get() = areaEntry.getDouble(0.0)

    init {
        isCamEnabled = false
//            camModeEntry.setDouble(0.0)
//            ledModeEntry.setDouble(0.0)
    }

    override fun reset() {
//        isCamEnabled = false
    }
}

private val angles = doubleArrayOf(-135.0, -90.0, -45.0, 0.0, 45.0, 90.0, 135.0)

suspend fun visionDrive() = use(Drive, Limelight, name = "Vision Drive"){
    Limelight.isCamEnabled = true
    val xTranslationK = 0.03

    periodic {
        val visionVector = Vector2(Limelight.xTranslation * xTranslationK, OI.driverController.leftTrigger * 0.5)
        Drive.drive(
            OI.driveTranslation,
            OI.driveRotation,
            SmartDashboard.getBoolean("Use Gyro", true) && !DriverStation.getInstance().isAutonomous,
            OI.operatorTranslation + visionVector,
            OI.operatorRotation
        )
    }
}