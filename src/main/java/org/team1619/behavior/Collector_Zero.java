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

public class Collector_Zero implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Collector_Zero.class);
	private static final Set<String> sSubsystems = Set.of("ss_collector");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Timer mTimer;
	private final int mZeroTimeOut;

	public Collector_Zero(InputValues inputValues, OutputValues outputValues, Config config, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		mZeroTimeOut = robotConfiguration.getInt("global_all", "zero_timeout");
		mTimer = new Timer();
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.debug("Entering state {}", stateName);

		boolean solenoidExtended = config.getBoolean("solenoid_extended");
		double rollerSpeed = config.getDouble("roller_speed");

		fSharedOutputValues.setNumeric("opn_collector_rollers", "percent", rollerSpeed);
		fSharedOutputValues.setBoolean("opb_collector_extended", solenoidExtended);

		mTimer.start(mZeroTimeOut);
	}

	@Override
	public void update() {

		// Do not proceed if the drivetrain has already been zeroed
		if(fSharedInputValues.getBoolean("ipb_collector_has_been_zeroed")){
			return;
		}

		// Give the solenoid time to move into position
		if(mTimer.isDone()){
			fSharedInputValues.setBoolean("ipb_collector_has_been_zeroed", true);
		}
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isDone() {
		return fSharedInputValues.getBoolean("ipb_collector_has_been_zeroed");
	}

	@Override
	public Set<String> getSubsystems() {
		return sSubsystems;
	}
}