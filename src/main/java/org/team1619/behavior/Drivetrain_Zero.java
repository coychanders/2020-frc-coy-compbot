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
	private Timer mTimer;
	private int mZeroTimeOut;
	private Double fZeroingThreshold;

	public Drivetrain_Zero(InputValues inputValues, OutputValues outputValues, Config config, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		mZeroTimeOut = robotConfiguration.getInt("global_all", "zero_timeout");
		mTimer = new Timer();
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.debug("Entering state {}", stateName);

		fZeroingThreshold = config.getDouble("zeroing_threshold");

		// Stop wheel motors
		fSharedOutputValues.setNumeric("opn_drivetrain_left", "percent", 0);
		fSharedOutputValues.setNumeric("opn_drivetrain_right", "percent", 0);

		mTimer.start(mZeroTimeOut);
	}

	@Override
	public void update() {

		// Do not proceed if the drivetrain has already been zeroed
		if(fSharedInputValues.getBoolean("ipb_drivetrain_has_been_zeroed")){
			return;
		}

		//todo - should we be zeroing the enoders over and over like this? If we assume the robot is stationary, should we call them just once in initialize()?
		// Zero encoders
		fSharedOutputValues.setOutputFlag("opn_drivetrain_left", "zero");
		fSharedOutputValues.setOutputFlag("opn_drivetrain_right", "zero");

		// Check encoders read zero
		if((Math.abs(fSharedInputValues.getNumeric("ipn_drivetrain_left_primary_position")) < fZeroingThreshold)
			&& (Math.abs(fSharedInputValues.getNumeric("ipn_drivetrain_right_primary_position")) < fZeroingThreshold)){
			fSharedInputValues.setBoolean("ipb_drivetrain_has_been_zeroed", true);
		}

		// If the encoders do not read zero by the end of the timer, move on so the robot is not stuck waiting to zero
		if(mTimer.isDone()){
			fSharedInputValues.setBoolean("ipb_drivetrain_has_been_zeroed", true);
			sLogger.error("Drivetrain failed to zero");
		}
	}

	@Override
	public void dispose() {
	//todo - do we need to set motors to zero when they have already been set that way in initialize()?
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