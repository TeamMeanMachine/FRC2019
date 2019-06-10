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
import org.team2471.frc.lib.coroutines.parallel
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.motion.following.driveAlongPath
import org.team2471.frc.lib.motion.following.driveAlongPathWithStrafe
import org.team2471.frc.lib.motion_profiling.Autonomi
import org.team2471.frc.lib.units.feet
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
        addOption("Cargo Ship Auto", ::cargoShipAuto)
        addOption("Front Cargo Ship Auto", ::cargoShipAutoFront)
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

private suspend fun cargoShipAuto() = coroutineScope {
    val auto = autonomi["Cargo Auto"]
    auto.isMirrored = startingSide == Side.LEFT
    parallel({
        scoreAtTarget(auto["Platform to Cargo Ship"], 4.3, Limelight.AUTO_CARGO_MED_SHIP.feet, true )
    }, {
        delay(3.8)
        goToPose(Pose.HATCH_LOW)
    })
    placeHatch()

    parallel({
        scoreAtTarget(auto["Cargo Ship to Feeder Station"],2.5, Limelight.AUTO_HATCH_PICKUP.feet )
    }, {
        delay(2.2)
        autoIntakeHatch()
    })

    parallel({
        scoreAtTarget(auto["Feeder Station to Cargo Ship"],3.2, Limelight.AUTO_CARGO_MED_SHIP.feet )
    }, {
        delay(2.5)
        goToPose(Pose.HATCH_LOW)
    })
    placeHatch()
    delay(Double.POSITIVE_INFINITY)
}

private suspend fun cargoShipAutoFront() = coroutineScope {
    val auto = autonomi["Cargo Auto"]
    auto.isMirrored = startingSide == Side.LEFT
    parallel({
        scoreAtTarget(auto["Platform to Front Cargo Ship"], 1.5, Limelight.AUTO_CARGO_MED_SHIP.feet, true )
    }, {
        delay(1.0)
        goToPose(Pose.HATCH_LOW)
    })
    placeHatch()

    parallel({
        scoreAtTarget(auto["Front Cargo Ship to Feeder Station"],3.75, Limelight.AUTO_HATCH_PICKUP.feet )
    }, {
        delay(2.2)
        autoIntakeHatch()
    })

    parallel({
        scoreAtTarget(auto["Feeder Station to Cargo Ship"],3.2, Limelight.AUTO_CARGO_MED_SHIP.feet, true )
    }, {
        delay(2.5)
        goToPose(Pose.HATCH_LOW)
    })
    placeHatch()
    delay(.25)

    scoreAtTarget(auto["Far to Middle Cargo Ship"],1.2, Limelight.AUTO_CARGO_MED_SHIP.feet )
    delay(Double.POSITIVE_INFINITY)
}


private suspend fun rocketAuto() = coroutineScope {
    val auto = autonomi["Rocket Auto"]
    auto.isMirrored = startingSide == Side.LEFT
    val timer = Timer()
    timer.start()
    parallel({
        scoreAtTarget(auto["Platform to Rocket"], 2.7, Limelight.AUTO_HATCH_LOW_HIGH.feet, true )
    }, {
        delay(1.0)
        goToPose(Pose.HATCH_LOW)
    })
    placeHatch()


    timer.reset()
    parallel({
        scoreAtTarget(auto["Rocket to Feeder Station 2"], 2.7, Limelight.AUTO_HATCH_PICKUP.feet)
    }, {
        delay(1.5)
        autoIntakeHatch()
    })

    timer.reset()
    parallel({
        scoreAtTarget(auto["Feeder Station to Back Rocket"], 3.8, Limelight.AUTO_HATCH_LOW_HIGH.feet)
    }, {
        delay(2.5)
        goToPose(Pose.HATCH_LOW)
    })

    placeHatch()

    timer.reset()
    parallel({
        scoreAtTarget(auto["Back Rocket to Cargoship"], 3.6, Limelight.AUTO_HATCH_LOW_HIGH.feet)
    }, {
        delay(1.0)
        Armavator.intake(0.6)
        goToPose(Pose.CARGO_GROUND_PICKUP)
        delay(-0.0)
    })
    Armavator.isPinching = true
    delay(Double.POSITIVE_INFINITY)
}

private suspend fun autoCycleToBackRocket(position: ScoringPosition) = coroutineScope {
    parallel({
        scoreAtTarget(autonomi["Rocket Auto"]["Feeder Station to Back Rocket"], 3.8, Limelight.AUTO_HATCH_LOW_HIGH.feet)
    }, {
        delay(2.5)
        goToPose(when (position) {
            ScoringPosition.ROCKET_LOW -> Pose.HATCH_LOW
            ScoringPosition.ROCKET_MED -> Pose.HATCH_MED
            ScoringPosition.ROCKET_HIGH -> Pose.HATCH_HIGH
            ScoringPosition.CARGO_SHIP -> throw IllegalAccessException("I can only do hatches")
        })
    })
    placeHatch()

//    parallel({
//        scoreAtTarget(autonomi["Rocket Auto"]["Back Rocket to Cargoship"], 3.6, AUTO_HATCH_LOW_HIGH.feet)
//    }, {
//        delay(1.0)
//        Armavator.intake(0.6)
//        goToPose(Pose.CARGO_GROUND_PICKUP)
//        delay(-0.0)
//    })
}
