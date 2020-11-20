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
 * Zeros the collector
 */

public class Hopper_Zero implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Hopper_Zero.class);
	private static final Set<String> sSubsystems = Set.of("ss_hopper");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Timer fTimer;
	private final int fZeroTimeOut;
	private double mZeroingThreshold;

	public Hopper_Zero(InputValues inputValues, OutputValues outputValues, Config config, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;

		fZeroTimeOut = robotConfiguration.getInt("global_all", "zero_timeout");
		fTimer = new Timer();
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.debug("Entering state {}", stateName);

		fSharedOutputValues.setBoolean("opb_hopper_kicker", false);
		fSharedOutputValues.setNumeric("opn_hopper", "percent", 0);
		mZeroingThreshold = config.getDouble("zeroing_threshold", 0);

		fTimer.start(fZeroTimeOut);
	}

	@Override
	public void update() {

		// Do not proceed if the hopper has already been zeroed
		if(fSharedInputValues.getBoolean("ipb_hopper_has_been_zeroed")){
			return;
		}

		// Zero encoder
		//todo - fix bug so we only need to call setOutputFlag once in initialize()
		fSharedOutputValues.setOutputFlag("opn_hopper", "zero");
		if(Math.abs(fSharedInputValues.getNumeric("ipb_hopper_position")) < mZeroingThreshold){
			fSharedInputValues.setBoolean("ipb_hopper_has_been_zeroed", true);
			sLogger.debug("Hopper -> Zeroed");
		}

		// Time out
		if(fTimer.isDone()){
			fSharedInputValues.setBoolean("ipb_hopper_has_been_zeroed", true);
			sLogger.error("Hopper -> Zero timed out");
		}
	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean isDone() {
		return fSharedInputValues.getBoolean("ipb_hopper_has_been_zeroed");
	}

	@Override
	public Set<String> getSubsystems() {
		return sSubsystems;
	}
}