@file:Suppress("ConstantConditionIf", "UNREACHABLE_CODE")

package org.team2471.frc2019

import edu.wpi.first.wpilibj.Timer
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.inches
import kotlin.math.absoluteValue
import kotlin.math.max

//suspend fun goToPose2(targetPose: Pose) = use(Armavator, OB1) {
//    val startingPose = Pose.current
//
//    var elevatorDirection = Math.signum(targetPose.elevatorHeight.asInches - startingPose.elevatorHeight.asInches)
//    var elevatorResolved = false
//    var armResolved = false
//    var obiResolved = false
//
//    val clawHeight = startingPose.clawHeight
//
//    if (targetPose.clawHeight < )
//
//    if (elevatorResolved) Armavator.goToHeight(targetPose.elevatorHeight)
//    if (armResolved) Armavator.goToAngle(targetPose.armAngle)
//    if (obiResolved) OB1.goToAngle(targetPose.obiAngle)
//
//
//}

private const val OBI_SAFETY_LINE_MIN = 6.0
private const val OBI_SAFETY_ANGLE = 35.0
private const val ARM_SAFETY_TOLERANCE = 5
private const val ELEVATOR_SAFETY_TOLERANCE = 3.0

private fun Pose.confined(): Boolean {
    val safetyLine = max(
        OBI_SAFETY_LINE_MIN,
        obiHeight.asInches
    ).inches
    return (clawHeight < safetyLine && armAngle.asDegrees < Pose.SAFETY.armAngle.asDegrees + ARM_SAFETY_TOLERANCE)
}

suspend fun goToPose(targetPose: Pose) {
    val startingPose = Pose.current
    val targetConfined = targetPose.confined()
    println("Target: $targetPose, confined: $targetConfined")
    val timer = Timer().apply { start() }
    check(!(targetConfined && !targetPose.isClamping)) { "Illegal pose: confined and not clamping" }

    use(Armavator, OB1, name = "Go To Pose") {
        // part 1, avoid collisisons
        periodic {
            Armavator.isPinching = targetPose.isPinching
            val currentPose = Pose.current
            if (currentPose.confined() != targetConfined) {
                // 🤔
                Armavator.isClamping = true
                if (targetConfined) {
                    OB1.angleSetpoint = Pose.SAFETY.obiAngle
                    Armavator.angleSetpoint = if (currentPose.elevatorHeight >
                        Pose.SAFETY.elevatorHeight - ELEVATOR_SAFETY_TOLERANCE.inches
                    ) {
                        Pose.SAFETY.armAngle
                    } else {
                        currentPose.armAngle
                    }
                    Armavator.heightSetpoint = if (
                        currentPose.armAngle < Pose.SAFETY.armAngle + ARM_SAFETY_TOLERANCE.degrees &&
                        (startingPose.isClamping || timer.get() > 0.25)
                    ) {
                        targetPose.elevatorHeight
                    } else {
                        Pose.SAFETY.elevatorHeight
                    }
                } else {
                    if (currentPose.obiAngle.asDegrees < OBI_SAFETY_ANGLE) {
                        Armavator.heightSetpoint =
                                max(Pose.SAFETY.elevatorHeight.asInches, targetPose.elevatorHeight.asInches).inches
                    } else {
                        Armavator.heightSetpoint = Armavator.height
                        Armavator.angleSetpoint = Armavator.angle
                        OB1.angleSetpoint = Pose.SAFETY.obiAngle
                    }
                }
            } else {
                stop()
            }
        }

        timer.reset()
        periodic {
            Armavator.heightSetpoint = targetPose.elevatorHeight
            Armavator.angleSetpoint = targetPose.armAngle
            OB1.angleSetpoint = targetPose.obiAngle
            Armavator.isClamping = targetPose.isClamping

            val armError = Armavator.angleSetpoint - Armavator.angle
            val elevatorError = Armavator.heightSetpoint - Armavator.height
            val obiError = OB1.angleSetpoint - OB1.angle
            if (armError.asDegrees.absoluteValue < 10.0 &&
                elevatorError.asInches.absoluteValue < 3.0 &&
                obiError.asDegrees.absoluteValue < 10.0 &&
                timer.get() > 0.3
            ) {
                stop()
            }
        }
    }
}
