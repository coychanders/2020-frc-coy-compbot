package org.team1619.behavior;

import org.uacr.models.behavior.Behavior;
import org.uacr.shared.abstractions.InputValues;
import org.uacr.shared.abstractions.OutputValues;
import org.uacr.shared.abstractions.RobotConfiguration;
import org.uacr.utilities.Config;
import org.uacr.utilities.Timer;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import java.util.Set;

/**
 * Controls the robot drivetrain motors in velocity mode
 * Forward is left Y joystick - rotate is the right X joystick
 * The left triggers shifts into low gear
 */

public class Drivetrain_Velocity implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Drivetrain_Velocity.class);
	private static final Set<String> sSubsystems = Set.of("ss_drivetrain");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;

	private final String fYAxisID;
	private final String fXAxisID;
	private final String fGearShiftID;

	public Drivetrain_Velocity(InputValues inputValues, OutputValues outputValues, Config config, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;

		fYAxisID = robotConfiguration.getString("global_drivetrain","y_control");
		fXAxisID = robotConfiguration.getString("global_drivetrain","x_control");
		fGearShiftID = robotConfiguration.getString("global_drivetrain","gearshift_control");
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.debug("Entering state {}", stateName);

	}

	@Override
	public void update() {
		double yAxis = fSharedInputValues.getNumeric(fYAxisID);
		double xAxis = fSharedInputValues.getNumeric(fXAxisID);
		boolean gearShift = fSharedInputValues.getBoolean(fGearShiftID);

		double leftPower = yAxis + xAxis;
		double rightPower = yAxis - xAxis;

		// Scale so the power can never exceed 1.0
		if(Math.abs(leftPower) > 1.0){
			rightPower = rightPower/Math.abs(leftPower);
			leftPower = leftPower/Math.abs(leftPower);
		} else if (Math.abs(rightPower) > 1.0){
			leftPower = leftPower/Math.abs(rightPower);
			rightPower = rightPower/Math.abs(rightPower);
		}

		//todo - currently the only difference between percent and velocity is setting the motor values. Should this just be handled inside percent vs having so much duplicate code?

		//todo - should 12 be hard coded here? should it be inside the velocity code using the scaler in the motor parameters?
		// Set motor velocity - Max is 12 feet per second
		fSharedOutputValues.setNumeric("opn_drivetrain_left", "velocity", leftPower * 12, "pr_drive");
		fSharedOutputValues.setNumeric("opn_drivetrain_right", "velocity", rightPower * 12, "pr_drive");

		// Set gear shifter
		fSharedOutputValues.setBoolean("opb_gearshift", gearShift);
	}

	@Override
	public void dispose() {
		fSharedOutputValues.setNumeric("opn_drivetrain_left", "percent", 0);
		fSharedOutputValues.setNumeric("opn_drivetrain_right", "percent", 0);

	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public Set<String> getSubsystems() {
		return sSubsystems;
	}
}