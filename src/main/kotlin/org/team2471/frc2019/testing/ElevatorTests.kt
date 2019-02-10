package org.team2471.frc2019.testing

import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.units.Length
import org.team2471.frc.lib.units.inches
import org.team2471.frc2019.Elevator
import kotlin.math.absoluteValue

suspend fun Elevator.goToHeight(height: Length, tolerance: Length = 0.5.inches) = use (this) {
    elevate(height)
    suspendUntil { (this.height - height).asInches.absoluteValue < tolerance.asInches}
}
