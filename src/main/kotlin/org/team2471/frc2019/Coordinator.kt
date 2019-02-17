@file:Suppress("UNREACHABLE_CODE")

package org.team2471.frc2019

import javafx.beans.binding.ObjectBinding
import org.team2471.frc.lib.coroutines.parallel
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.intersects
import org.team2471.frc.lib.units.asDegrees
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.inches
import kotlin.math.max
import kotlin.math.min

private val ob1Zone = 6.0..24.0 //inches
private const val SAFETY_POSE_MAX = 50.0 // degrees

//private val currentPose: Pose
//    get() = Pose(Armavator.angle, Armavator.height, OB1.angle)
//
//suspend fun animateToPose(targetPose: Pose) = use(Armavator, OB1) {
//    val startingPose = currentPose
//    val clawHeightRange = min(startingPose.clawHeight.asInches, targetPose.clawHeight.asInches)..
//            max(startingPose.clawHeight.asInches, targetPose.clawHeight.asInches)
//
//    println("Start")
//    if (ob1Zone.intersects(clawHeightRange)) {
//        val safetyPose = Pose((-74).degrees, 12.inches, 0.degrees)
//        println("Safety pose: $safetyPose")
//
//        parallel({
//            OB1.animateToAngle(safetyPose.ob1Angle)
//        }, {
//            suspendUntil { OB1.angle <= SAFETY_POSE_MAX.degrees }
//            println(OB1.angle)
//            Armavator.animate(safetyPose.elevatorHeight, safetyPose.armAngle)
//        })
//
//        parallel({
//            Armavator.animate(targetPose.elevatorHeight, targetPose.armAngle)
//        }, {
//            suspendUntil { currentPose.clawHeight.asInches !in ob1Zone }
//            OB1.animateToAngle(targetPose.ob1Angle)
//        })
//    } else {
//        parallel({
//            Armavator.animate(targetPose.elevatorHeight, targetPose.armAngle)
//        }, {
//            suspendUntil { Pose(Armavator.angle, Armavator.height, OB1.angle).clawHeight.asInches !in ob1Zone }
//            OB1.animateToAngle(targetPose.ob1Angle)
//        })
//    }
//}
//