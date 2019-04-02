package org.team2471.frc2019

import edu.wpi.first.networktables.EntryListenerFlags
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import kotlinx.coroutines.coroutineScope
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.parallel
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.motion.following.driveAlongPath
import org.team2471.frc.lib.motion_profiling.Autonomi
import org.team2471.frc.lib.util.measureTimeFPGA
import java.io.File

private lateinit var autonomi: Autonomi

enum class Side {
    LEFT,
    RIGHT;

    operator fun not(): Side = when (this) {
        LEFT -> RIGHT
        RIGHT -> LEFT
    }
}

private var startingSide = Side.RIGHT

object AutoChooser {
    private val cacheFile = File("/home/lvuser/autonomi.json")

    private val sideChooser = SendableChooser<Side>().apply {
        setDefaultOption("Left", Side.LEFT)
        addOption("Right", Side.RIGHT)
    }

    private val testAutoChooser = SendableChooser<String?>().apply {
        setDefaultOption("None", null)
        addOption("20 Foot Test", "20 Foot Test")
        addOption("8 Foot Straight", "8 Foot Straight")
        addOption("2 Foot Circle", "2 Foot Circle")
        addOption("4 Foot Circle", "4 Foot Circle")
        addOption("8 Foot Circle", "8 Foot Circle")
        addOption("Hook Path", "Hook Path")
    }

    private val autonomousChooser = SendableChooser<suspend () -> Unit>().apply {
        setDefaultOption("None", null)
        addOption("Rocket Auto", ::rocketAuto)
        addOption("Tests", ::testAuto)
    }

    init {
        SmartDashboard.putData("Side", sideChooser)
        SmartDashboard.putData("Tests", testAutoChooser)
        SmartDashboard.putData("Autos", autonomousChooser)

        try {
            autonomi = Autonomi.fromJsonString(cacheFile.readText())
            println("Autonomi cache loaded.")
        } catch (_: Throwable) {
            DriverStation.reportError("Autonomi cache could not be found", false)
            autonomi = Autonomi()
        }

        NetworkTableInstance.getDefault()
            .getTable("PathVisualizer")
            .getEntry("Autonomi").addListener({ event ->
                val json = event.value.string
                if (!json.isEmpty()) {
                    val t = measureTimeFPGA {
                        autonomi = Autonomi.fromJsonString(json)
                    }
                    println("Loaded autonomi in $t seconds")

                    cacheFile.writeText(json)
                    println("New autonomi written to cache")
                } else {
                    autonomi = Autonomi()
                    DriverStation.reportWarning("Empty autonomi received from network tables", false)
                }
            }, EntryListenerFlags.kImmediate or EntryListenerFlags.kNew or EntryListenerFlags.kUpdate)
    }

    suspend fun autonomous() = use(Drive, Armavator, name = "Autonomous") {
        val nearSide = sideChooser.selected
        startingSide = nearSide

        val autoEntry = autonomousChooser.selected
        autoEntry.invoke()
    }

    suspend fun testAuto() {
        val testPath = testAutoChooser.selected
        if (testPath != null) {
            val testAutonomous = autonomi["Tests"]
            val path = testAutonomous[testPath]
            Drive.driveAlongPath(path, true, 0.0)
        }
    }

}

private suspend fun rocketAuto() = coroutineScope {
    val auto = autonomi["Rocket Auto"]
    auto.isMirrored = false

    parallel({
        Drive.driveAlongPath(auto["Platform to Rocket"], true, 0.0)
    }, {
        if (Limelight.hasValidTarget) {
            autonomousVisionDrive(0.3)
        }
    })
    //Armavator.isPinching = true
    delay(0.5)
}
