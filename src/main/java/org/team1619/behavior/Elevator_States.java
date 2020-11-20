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
 * Controls the collector - extend/retract and rollers on/off
 */

public class Elevator_States implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Elevator_States.class);
	private static final Set<String> sSubsystems = Set.of("ss_elevator");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;

	private final Timer fTimer;

	private Boolean mSenseBall;
	private Boolean mBallFound;

	public Elevator_States(InputValues inputValues, OutputValues outputValues, Config config, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;

		fTimer = new Timer();
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.debug("Entering state {}", stateName);

		mSenseBall = config.getBoolean("sense_ball", false);
		double elevatorSpeed = config.getDouble("elevator_speed");

		mBallFound = false;

		fSharedOutputValues.setNumeric("opn_elevator", "percent", elevatorSpeed);

		int time = config.getInt("time", 0);
		if(time > 0){
			fTimer.start(time);
		} else {
			fTimer.reset();
		}
	}

	@Override
	public void update() {
		if(mSenseBall) {
			mBallFound = fSharedInputValues.getBoolean("ipb_elevator_beam_sensor");
			if(mBallFound){
				fSharedOutputValues.setNumeric("opn_elevator", "percent", 0);
			}
		}

		if(fTimer.isDone()){
			fSharedOutputValues.setNumeric("opn_elevator", "percent", 0);
		}

	}

	@Override
	public void dispose() {
		fSharedOutputValues.setNumeric("opn_elevator", "percent", 0.0);
	}

	@Override
	public boolean isDone() {
		if(mSenseBall){
			return mBallFound;
		}
		return true;
	}

	@Override
	public Set<String> getSubsystems() {
		return sSubsystems;
	}
}