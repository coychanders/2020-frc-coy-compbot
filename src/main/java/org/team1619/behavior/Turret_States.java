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

public class Turret_States implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Turret_States.class);
	private static final Set<String> sSubsystems = Set.of("ss_turret");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;

	private final String fMacroAdjustID;
	private final String fMicroAdjustID;
	private final int fMacroScalar;
	private final int fMicroScalar;

	private boolean mAllowAdjustment;
	private double mTurretAngle;
	private String mProfile;
	private int mPositionThreshold;

	private double mAngleAdjustment;
	private Boolean mPositionReached;


	public Turret_States(InputValues inputValues, OutputValues outputValues, Config config, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;

		fMacroAdjustID = robotConfiguration.getString("global_flywheel", "macro_adjust");
		fMicroAdjustID = robotConfiguration.getString("global_flywheel", "micro_adjust");
		fMacroScalar = robotConfiguration.getInt("global_flywheel", "macro_scalar");
		fMicroScalar = robotConfiguration.getInt("global_flywheel", "micro_scalar");

	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.debug("Entering state {}", stateName);

		mTurretAngle = config.getDouble("turret_angle");
		mAllowAdjustment = config.getBoolean("allow_adjust", false);
		mProfile = config.getString("profile", "");
		mPositionThreshold = config.getInt("position_threshold", 10);

	}

	@Override
	public void update() {

		double currentPosition = fSharedInputValues.getNumeric("ipn_turret_position");

		// Calculate a running velocity adjustment value based on the joysticks
		if(mAllowAdjustment) {
			mAngleAdjustment += (fSharedInputValues.getNumeric(fMacroAdjustID) * fMacroScalar) + (fSharedInputValues.getNumeric(fMicroAdjustID) * fMicroScalar);
		} else {
			mAngleAdjustment = 0;
		}

		double desiredPosition = mTurretAngle + mAngleAdjustment;

		fSharedOutputValues.setNumeric("opn_turret", "motion_magic", desiredPosition, mProfile);

		mPositionReached = Math.abs(currentPosition - desiredPosition) < mPositionThreshold;
	}

	@Override
	public void dispose() {
		fSharedOutputValues.setNumeric("opn_turret", "percent", 0.0);
	}

	@Override
	public boolean isDone() {
		return mPositionReached;
	}

	@Override
	public Set<String> getSubsystems() {
		return sSubsystems;
	}
}