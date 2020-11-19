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

public class Flywheels_States implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Flywheels_States.class);
	private static final Set<String> sSubsystems = Set.of("ss_flywheel");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;

	private final String fMacroAdjustID;
	private final String fMicroAdjustID;
	private final int fMacroScalar;
	private final int fMicroScalar;

	private int mVelocity;
	private String mProfile;
	private int mVelocityThreshold;
	private boolean mAllowAdjustment;
	private double mTurboPercent;
	private int mTurboCutoffVelocity;
	private boolean mCoast;

	private double mVelocityAdjustment;
	private Boolean mUpToSpeed;


	public Flywheels_States(InputValues inputValues, OutputValues outputValues, Config config, RobotConfiguration robotConfiguration) {
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

		mVelocity = config.getInt("flywheel_velocity", 0);
		mProfile = config.getString("profile", "");
		mVelocityThreshold = config.getInt("velocity_threshold", 100);
		mAllowAdjustment = config.getBoolean("allow_adjust", false);
		mCoast = config.getBoolean("coast", false);

		mTurboPercent = config.getDouble("turbo_percent", 0);
		mTurboCutoffVelocity = config.getInt("turbo_cutoff_velocity", 0);

		mUpToSpeed = false;

		fSharedInputValues.setBoolean("ipb_flywheel_primed", false);
	}

	@Override
	public void update() {

		// Get current velocity
		double velocity = fSharedInputValues.getNumeric("ipn_flywheel_primary_velocity");

		// Calculate a running velocity adjustment value based on the joysticks
		if(mAllowAdjustment) {
			mVelocityAdjustment += (fSharedInputValues.getNumeric(fMacroAdjustID) * fMacroScalar) + (fSharedInputValues.getNumeric(fMicroAdjustID) * fMicroScalar);
		} else {
			mVelocityAdjustment = 0;
		}

		// Limit the amount of adjustment
		if ((mVelocity + mVelocityAdjustment) > 8000) {
			mVelocityAdjustment = 8000 - mVelocity;
		}
		if ((mVelocity + mVelocityAdjustment) < -1000) {
			mVelocityAdjustment = mVelocity - 1000;
		}

		// Turbo prevents the velocity mode from throwing 100% power at the motors and throwing the breakers by using a lower percent power mode until close to the final velocity
		if((mTurboPercent > 0) && (velocity < mTurboCutoffVelocity)){
			fSharedOutputValues.setNumeric("opn_flywheel", "percent", mTurboPercent);
		} else if(mCoast) {
			// Allows the flywheels to coast instead of being driven to stop
			fSharedOutputValues.setNumeric("opn_flywheel", "percent", mVelocity);
		} else {
			// Uses velocity mode to maintain flywheel speed
			fSharedOutputValues.setNumeric("opn_flywheel", "velocity", mVelocity + mVelocityAdjustment, mProfile);
			if(mTurboPercent > 0) {
				fSharedInputValues.setBoolean("ipb_flywheel_primed", true);
			}
		}

		mUpToSpeed = Math.abs(velocity - mVelocity) < mVelocityThreshold;
	}

	@Override
	public void dispose() {
		fSharedOutputValues.setNumeric("opn_flywheel", "percent", 0.0);
	}

	@Override
	public boolean isDone() {
		return mUpToSpeed;
	}

	@Override
	public Set<String> getSubsystems() {
		return sSubsystems;
	}
}