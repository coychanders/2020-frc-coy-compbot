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
 * Zeros the drivetrain encoders
 */

public class Drivetrain_Zero implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Drivetrain_Zero.class);
	private static final Set<String> sSubsystems = Set.of("ss_drivetrain");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Timer fTimer;
	private final int fZeroTimeOut;
	private Double mZeroingThreshold;

	public Drivetrain_Zero(InputValues inputValues, OutputValues outputValues, Config config, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fZeroTimeOut = robotConfiguration.getInt("global_all", "zero_timeout");
		fTimer = new Timer();
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.debug("Entering state {}", stateName);

		mZeroingThreshold = config.getDouble("zeroing_threshold");

		// Stop wheel motors
		fSharedOutputValues.setNumeric("opn_drivetrain_left", "percent", 0);
		fSharedOutputValues.setNumeric("opn_drivetrain_right", "percent", 0);

		fTimer.start(fZeroTimeOut);
	}

	@Override
	public void update() {

		// Do not proceed if the drivetrain has already been zeroed
		if(fSharedInputValues.getBoolean("ipb_drivetrain_has_been_zeroed")){
			return;
		}

		//todo - We call zero every frame as it is possible for it to be missed. There is an issue logged for this

		// Instruct the encoders to zero at the end of the current frame.
		fSharedOutputValues.setOutputFlag("opn_drivetrain_left", "zero");
		fSharedOutputValues.setOutputFlag("opn_drivetrain_right", "zero");

		// Check if the encoders have finished zeroing.
		// The encoders may move slightly off true-zero due to physical forces, so "zeroed"
		// really means that the encoder absolute value is within a small threshold from zero.
		double left = fSharedInputValues.getNumeric("ipn_drivetrain_left_primary_position");
		double right = fSharedInputValues.getNumeric("ipn_drivetrain_right_primary_position");
		boolean leftIsZeroed = Math.abs(left) < mZeroingThreshold;
		boolean rightIsZeroed = Math.abs(right) < mZeroingThreshold;
		if (leftIsZeroed && rightIsZeroed) {
			fSharedInputValues.setBoolean("ipb_drivetrain_has_been_zeroed", true);
		}

		// If the encoders do not read zero by the end of the timer, move on so the robot is not stuck waiting to zero
		if(fTimer.isDone()){
			fSharedInputValues.setBoolean("ipb_drivetrain_has_been_zeroed", true);
			sLogger.error("Drivetrain failed to zero");
		}
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isDone() {
		return fSharedInputValues.getBoolean("ipb_drivetrain_has_been_zeroed");
	}

	@Override
	public Set<String> getSubsystems() {
		return sSubsystems;
	}
}