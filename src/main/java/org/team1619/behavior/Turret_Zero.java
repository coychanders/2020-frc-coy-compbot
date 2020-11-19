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

public class Turret_Zero implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Turret_Zero.class);
	private static final Set<String> sSubsystems = Set.of("ss_turret");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Timer fTimer;
	private final int fZeroTimeOut;
	private double mZeroingThreshold;

	public Turret_Zero(InputValues inputValues, OutputValues outputValues, Config config, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;

		fZeroTimeOut = robotConfiguration.getInt("global_all", "zero_timeout");
		fTimer = new Timer();
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.debug("Entering state {}", stateName);

		double zeroingSpeed = config.getDouble("zeroing_speed", 0);
		mZeroingThreshold = config.getDouble("zeroing_threshold", 0);

		fSharedOutputValues.setNumeric("opn_turret", "percent", zeroingSpeed);

		fTimer.start(fZeroTimeOut);
	}

	@Override
	public void update() {

		// Do not proceed if the hopper has already been zeroed
		if(fSharedInputValues.getBoolean("ipb_turret_has_been_zeroed")){
			return;
		}

		if(Math.abs(fSharedInputValues.getNumeric("ipn_turret_velocity")) < mZeroingThreshold){
			fSharedOutputValues.setNumeric("opn_turret", "percent", 0);
			fSharedOutputValues.setOutputFlag("opn_turret", "zero");
			fSharedInputValues.setBoolean("ipb_turret_has_been_zeroed", true);
			sLogger.debug("Turret -> Zeroed");
		}

		// Time out
		if(fTimer.isDone()){
			fSharedInputValues.setBoolean("ipb_turret_has_been_zeroed", true);
			fSharedOutputValues.setNumeric("opn_turret", "percent", 0);
			sLogger.error("Turret -> Zero timed out");
		}
	}

	@Override
	public void dispose() {
		fSharedOutputValues.setNumeric("opn_turret", "percent", 0.0);
	}

	@Override
	public boolean isDone() {
		return fSharedInputValues.getBoolean("ipb_turret_has_been_zeroed");
	}

	@Override
	public Set<String> getSubsystems() {
		return sSubsystems;
	}
}