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

public class Collector_States implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Collector_States.class);
	private static final Set<String> sSubsystems = Set.of("ss_collector");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;


	public Collector_States(InputValues inputValues, OutputValues outputValues, Config config, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.debug("Entering state {}", stateName);

		boolean solenoidExtended = config.getBoolean("solenoid_extended");
		double rollerSpeed = config.getDouble("roller_speed");

		fSharedOutputValues.setNumeric("opn_collector_rollers", "percent", rollerSpeed);
		fSharedOutputValues.setBoolean("opb_collector_extended", solenoidExtended);
	}

	@Override
	public void update() {
	}

	@Override
	public void dispose() {
		fSharedOutputValues.setNumeric("opn_collector_rollers", "percent", 0);
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