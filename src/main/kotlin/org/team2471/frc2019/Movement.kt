@file:Suppress("ConstantConditionIf", "UNREACHABLE_CODE")

package org.team2471.frc2019

import edu.wpi.first.wpilibj.Timer
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use
import kotlin.math.absoluteValue

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


suspend fun goToPose(targetPose: Pose) = use(Armavator) {
    val timer = Timer().apply { start() }

    periodic {
        Armavator.heightSetpoint = targetPose.elevatorHeight
        Armavator.angleSetpoint = targetPose.armAngle
        OB.angleSetpoint = targetPose.obAngle

        val armError = Armavator.angleSetpoint - Armavator.angle
        val elevatorError = Armavator.heightSetpoint - Armavator.height
        val obError = OB.angleSetpoint - (OB.leftAngle + OB.rightAngle)/2.0
        if (armError.asDegrees.absoluteValue < 10.0 &&
            elevatorError.asInches.absoluteValue < 3.0 &&
            timer.get() > 0.3
        ) {
            stop()
        }
    }
}
