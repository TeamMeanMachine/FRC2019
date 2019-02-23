package org.team2471.frc2019

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Timer
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.motion_profiling.MotionCurve
import org.team2471.frc.lib.units.*


data class KeyFrame(val time: Time, val pose: Pose)

class Animation(private vararg val keyFrames: KeyFrame) {
    companion object {
        val START_TO_HANDOFF = Animation(
            KeyFrame(0.seconds, Pose.STARTING_POSITION),
            KeyFrame(1.seconds, Pose(3.inches, (-74).degrees, 45.degrees, true)),
            KeyFrame(2.seconds, Pose(15.inches, (-74).degrees, 45.degrees, false, true)),
            KeyFrame(3.seconds, Pose.HATCH_HANDOFF)
        )

        val HANDOFF_TO_HATCH_CARRY = Animation(
            KeyFrame(0.seconds, Pose.HATCH_HANDOFF),
//        KeyFrame(0.5.seconds, Pose(5.inches, 13.degrees, 90.degrees, false)),
            KeyFrame(0.5.seconds, Pose(9.inches, (-20).degrees, 90.degrees, false)),
            KeyFrame(1.5.seconds, Pose.HATCH_CARRY)
        )

        val SCORE_1
            get() = Animation(
                KeyFrame(0.seconds, Pose(Armavator.height, Armavator.angle, OB1.angle, false)),
                KeyFrame(1.seconds, Pose.HATCH_LOW)
            )

        val SCORE_2
            get() = Animation(
                KeyFrame(0.seconds, Pose(Armavator.height, Armavator.angle, OB1.angle, false)),
                KeyFrame(1.seconds, Pose.HATCH_MED)
            )

        val SCORE_3
            get() = Animation(
                KeyFrame(0.seconds, Pose(Armavator.height, Armavator.angle, OB1.angle, false)),
                KeyFrame(1.seconds, Pose.HATCH_HIGH)
            )

        val CURRENT_TO_HATCH_CARRY
            get() = Animation(
                KeyFrame(0.seconds, Pose(Armavator.height, Armavator.angle, OB1.angle, false)),
                KeyFrame(2.seconds, Pose.SAFETY_POSE),
                KeyFrame(2.5.seconds, Pose.HATCH_CARRY)
            )

        val HOME_TO_START_CLIMB = Animation(
            KeyFrame(0.seconds, Pose.HOME),
            KeyFrame(0.5.seconds, Pose.SAFETY_POSE),
            KeyFrame(1.0.seconds, Pose.CLIMB_START)
        )

        val START_CLIMB_TO_LIFTED = Animation(
            KeyFrame(0.seconds, Pose.CLIMB_START),
            KeyFrame(5.seconds, Pose.LIFTED)
        )

        val HOME_TO_HATCH_GROUND_PICKUP = Animation(
            KeyFrame(0.seconds, Pose.HOME),
            KeyFrame(0.5.seconds, Pose.HATCH_GROUND_PICKUP)
        )

        val GROUND_PICKUP_TO_HATCH_HANDOFF = Animation(
            KeyFrame(0.seconds, Pose.HATCH_GROUND_PICKUP),
            KeyFrame(1.25.seconds, Pose.HATCH_HANDOFF)
        )

        val HOME_TO_CARGO_GROUND_PICKUP = Animation(
            KeyFrame(0.seconds, Pose.HOME),
            KeyFrame(0.75.seconds, Pose.CARGO_GROUND_PICKUP)
        )

        val CARGO_SCORE_1
            get() = Animation(
                KeyFrame(0.seconds, Pose(Armavator.height, Armavator.angle, OB1.angle, true)),
                KeyFrame(0.5.seconds, Pose.CARGO_SAFETY_POSE),
                KeyFrame(1.seconds, Pose.CARGO_LOW)
            )

        val CARGO_SCORE_2
            get() = Animation(
                KeyFrame(0.seconds, Pose(Armavator.height, Armavator.angle, OB1.angle, true)),
                KeyFrame(0.5.seconds, Pose.CARGO_SAFETY_POSE),
                KeyFrame(2.0.seconds, Pose.CARGO_MED)
            )

        val CARGO_SCORE_3
            get() = Animation(
                KeyFrame(0.seconds, Pose(Armavator.height, Armavator.angle, OB1.angle, true)),
                KeyFrame(0.5.seconds, Pose.CARGO_SAFETY_POSE),
                KeyFrame(2.0.seconds, Pose.CARGO_HIGH)
            )

        val CARGO_SHIP_SCORE
            get() = Animation(
                KeyFrame(0.seconds, Pose(Armavator.height, Armavator.angle, OB1.angle, true)),
                KeyFrame(0.5.seconds, Pose.CARGO_SAFETY_POSE),
                KeyFrame(1.5.seconds, Pose.CARGO_SHIP_SCORE)
            )

        val HOME_TO_FEEDER_STATION
            get() = Animation(
                KeyFrame(0.seconds, Pose.HOME),
                KeyFrame(0.75.seconds, Pose.SAFETY_POSE),
                KeyFrame(1.75.seconds, Pose.HATCH_INTERMEDIATE),
                KeyFrame(2.5.seconds, Pose.HATCH_FEEDER_PICKUP)
            )

        val LIFTED_TO_LIFT_ELEVATOR
            get() = Animation(
                KeyFrame(0.seconds, Pose.LIFTED),
                KeyFrame(1.seconds, Pose.CLIMB_LIFT_ELEVATOR)
            )

        val CURRENT_TO_HOME
            get() = Animation(
                KeyFrame(0.seconds, Pose(Armavator.height, Armavator.angle, OB1.angle, true)),
                KeyFrame(1.5.seconds, Pose.CARGO_SAFETY_POSE),
                KeyFrame(2.25.seconds, Pose.HOME)
            )
    }

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
            keyFrame.pose.isPinching,
            keyFrame.pose.isClimbing
        )
    }

    fun reverse() = Animation(*keyFrames.map { KeyFrame(duration - it.time, it.pose) }.reversed().toTypedArray())
}

suspend fun Animation.play() {
    println("Playing animation $duration")
    val firstPose = get(0.seconds)
    check(firstPose.closeTo(Pose.current)) { "Current pose is too far away from starting point of animation" }

    use(Armavator, OB1) {
        val timer = Timer()
        timer.start()
        var i = 0
        periodic {
            val time = timer.get().seconds

            val pose = get(time)
            Armavator.heightSetpoint = pose.elevatorHeight
            Armavator.angleSetpoint = pose.armAngle
            Armavator.isClamping = pose.isClamping
            Armavator.isPinching = pose.isPinching
            Armavator.isClimbing = pose.isClimbing
            OB1.pivotSetpoint = pose.ob1Angle

            if (i++ % 10 == 0) {
//                println(pose)
            }

            if (time >= duration) return@periodic stop()
        }
    }
}
