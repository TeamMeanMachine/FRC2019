package org.team2471.frc2019

private const val CLAW_FREE_HEIGHT = 0.0 // TODO: lowest height the claw can be without concern of hitting the OBI

suspend fun goToPose(pose: Pose) {
    val obiPath = 0.0..0.0 // TODO: range of motion of obi
    val clawHeight = Pose.current.clawHeight

//    // find movement order
//    if (obiPath.contains(clawHeight.asInches)) {
//        // obi last
//    } else {
//        // obi first
//    }
//
//    if (clawHeight.asInches < CLAW_FREE_HEIGHT) {
//        //
//    }
}
