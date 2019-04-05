package org.team2471.frc2019

import edu.wpi.first.networktables.EntryListenerFlags
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import kotlinx.coroutines.coroutineScope
import org.team2471.frc.lib.control.PDController
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.halt
import org.team2471.frc.lib.coroutines.parallel
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.motion.following.driveAlongPath
import org.team2471.frc.lib.motion.following.driveAlongPathWithStrafe
import org.team2471.frc.lib.motion_profiling.Autonomi
import org.team2471.frc.lib.util.measureTimeFPGA
import org.team2471.frc2019.actions.*
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
    auto.isMirrored = startingSide == Side.LEFT
    val translationPDController = PDController(0.015, 0.0)
    val timer = Timer()
    timer.start()
    parallel({
        Drive.driveAlongPathWithStrafe(auto["Platform to Rocket"], true, 0.0,
            { if (Limelight.area > 3.0) 1.0 else 0.0 },
            { translationPDController.update(Limelight.xTranslation) },
            { Limelight.hasValidTarget && Limelight.isAtTarget(ScoringPosition.ROCKET_HIGH) })
        println("Drive done")
    }, {
        delay(1.0)
        goToPose(Pose.HATCH_HIGH)
    })
    placeHatch()


    timer.reset()
    parallel({
        Drive.driveAlongPathWithStrafe(auto["Rocket to Feeder Station"], false, 0.0,
            { time ->  if (auto["Rocket to Feeder Station"].easeCurve.getValue(time) > 0.5
                && Limelight.hasValidTarget
                && (Limelight.area > 3.0)) 1.0 else 0.0 },
            { translationPDController.update(Limelight.xTranslation) },
            { Limelight.hasValidTarget && Limelight.isAtTarget() && timer.get() > 3.0})
    }, {
        delay(1.5)
        autoIntakeHatch()
    })

    timer.reset()
    parallel({
        Drive.driveAlongPathWithStrafe(auto["Feeder Station to Back Rocket"], false, 0.0,
            { time ->  if (auto["Feeder Station to Back Rocket"].easeCurve.getValue(time) > 0.8
                && Limelight.hasValidTarget
                && (Limelight.area > 3.0)) 1.0 else 0.0 },
            { translationPDController.update(Limelight.xTranslation) },
            { Limelight.hasValidTarget && Limelight.isAtTarget(ScoringPosition.ROCKET_HIGH) && timer.get() > 3.25})
    }, {
        delay(2.5)
        goToPose(Pose.HATCH_HIGH)
    })

    placeHatch()

    timer.reset()
    parallel({
        Drive.driveAlongPathWithStrafe(auto["Back Rocket to Cargoship"], false, 0.0,
            { time ->  if (auto["Back Rocket to Cargoship"].easeCurve.getValue(time) > 0.8
                && Limelight.hasValidTarget
                && (Limelight.area > 3.0)) 1.0 else 0.0 },
            { translationPDController.update(Limelight.xTranslation) },
            { Limelight.hasValidTarget && Limelight.isAtTarget() && timer.get() > 3.0})
    }, {
        delay(1.0)
        Armavator.intake(0.6)
        goToPose(Pose.HATCH_LOW)
        delay(-0.0)
    })
    Armavator.isPinching = true
    delay(Double.POSITIVE_INFINITY)
}