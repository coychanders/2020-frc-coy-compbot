package org.team1619.behavior;

import org.uacr.models.behavior.Behavior;
import org.uacr.shared.abstractions.InputValues;
import org.uacr.shared.abstractions.OutputValues;
import org.uacr.shared.abstractions.RobotConfiguration;
import org.uacr.utilities.Config;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import java.util.Set;

/**
 * Controls the collector - extend/retract and rollers on/off
 */

public class Hopper_States implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Hopper_States.class);
	private static final Set<String> sSubsystems = Set.of("ss_hopper");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;

	private boolean fHomeHopper;


	public Hopper_States(InputValues inputValues, OutputValues outputValues, Config config, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.debug("Entering state {}", stateName);

		boolean kicker = config.getBoolean("kicker_extended");
		double hopperSpeed = config.getDouble("hopper_speed");
		fHomeHopper = config.getBoolean("home_hopper", false);
		
		fSharedOutputValues.setBoolean("opb_hopper_kicker", kicker);
		fSharedOutputValues.setNumeric("opn_hopper", "percent", hopperSpeed);
	}

	@Override
	public void update() {

		// Look for homing switch
		if(fHomeHopper && fSharedInputValues.getBoolean("ipb_hopper_home_switch")){
			fSharedOutputValues.setNumeric("opn_hopper", "percent", 0.0);
			fHomeHopper = false;
			sLogger.debug("Hopper -> Homed");
		}
	}

	@Override
	public void dispose() {
		fSharedInputValues.setBoolean("opb_hopper_kicker", false);
		fSharedOutputValues.setNumeric("opn_hopper", "percent", 0.0);
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