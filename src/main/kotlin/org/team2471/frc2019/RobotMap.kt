@file:Suppress("unused")

package org.team2471.frc2019

object Talons {
    const val DRIVE_FRONTLEFT = 0
    const val STEER_FRONTLEFT = 1

    const val DRIVE_BACKLEFT = 2
    const val STEER_BACKLEFT = 3

    const val DRIVE_BACKRIGHT = 13
    const val STEER_BACKRIGHT = 12

    const val DRIVE_FRONTRIGHT = 15
    const val STEER_FRONTRIGHT = 14

    const val ELEVATOR_MASTER = 4

    const val ARM_MASTER = 8

    const val OB1_INTAKE = 7

    const val OB1_PIVOT_MASTER = 11
}

object Victors {
    const val ELEVATOR_SLAVE = 5
    const val ARM_SLAVE = 9

    const val RESERVED = 6

    const val OB1_PIVOT_SLAVE = 10
}

object Solenoids {
    const val BALL_INTAKE = 0
    const val HATCH_INTAKE = 1
    const val SHIFTER = 2
}