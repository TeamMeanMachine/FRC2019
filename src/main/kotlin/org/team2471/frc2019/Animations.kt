package org.team2471.frc2019

import edu.wpi.first.wpilibj.Timer
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.motion_profiling.MotionCurve
import org.team2471.frc.lib.units.*


data class KeyFrame(val time: Time, val pose: Pose)

enum class Animation(private vararg val keyFrames: KeyFrame) {
    START_TO_HANDOFF(
        KeyFrame(0.seconds, Pose.STARTING_POSITION),
        KeyFrame(1.seconds, Pose(3.inches, (-74).degrees, 45.degrees)),
        KeyFrame(2.seconds, Pose(15.inches, (-74).degrees, 45.degrees, false, true)),
        KeyFrame(3.seconds, Pose.HATCH_HANDOFF)
    ),
    HANDOFF_TO_HATCH_CARRY(
        KeyFrame(0.seconds, Pose.HATCH_HANDOFF),
        KeyFrame(0.5.seconds, Pose(5.inches, 13.degrees, 90.degrees, false))
    )

    ;

    val duration = keyFrames.last().time

    private val elevatorCurve = MotionCurve()
    private val armCurve = MotionCurve()
    private val ob1Curve = MotionCurve()

    init {
        for (frame in keyFrames) {
            elevatorCurve.storeValue(frame.time.asSeconds, frame.pose.elevatorHeight.asInches)
            armCurve.storeValue(frame.time.asSeconds, frame.pose.armAngle.asDegrees)
            ob1Curve.storeValue(frame.time.asSeconds, frame.pose.ob1Angle.asDegrees)
        }
    }

    operator fun get(time: Time): Pose {
        val keyFrame = keyFrames.last { time >= it.time }

        return Pose(
            elevatorCurve.getValue(time.asSeconds).inches,
            armCurve.getValue(time.asSeconds).degrees,
            ob1Curve.getValue(time.asSeconds).degrees,
            keyFrame.pose.isClamping,
            keyFrame.pose.isPinching
        )
    }
}

suspend fun Animation.play() = use(Armavator, OB1) {
    val timer = Timer()
    timer.start()
    var i = 0
    periodic {
        val time = timer.get().seconds

        if (time >= duration) return@periodic stop()

        val pose = get(time)
        Armavator.heightSetpoint = pose.elevatorHeight
        Armavator.angleSetpoint = pose.armAngle
        Armavator.isClamping = pose.isClamping
        Armavator.isPinching = pose.isPinching
        OB1.pivotSetpoint = pose.ob1Angle

//        if (i++ % 10 == 0) {
//            println(pose)
//        }
    }
}
