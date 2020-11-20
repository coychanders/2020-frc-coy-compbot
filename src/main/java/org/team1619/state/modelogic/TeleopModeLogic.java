package org.team1619.state.modelogic;

import org.uacr.models.state.State;
import org.uacr.robot.AbstractModeLogic;
import org.uacr.shared.abstractions.InputValues;
import org.uacr.shared.abstractions.RobotConfiguration;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

/**
 * Handles the isReady and isDone logic for teleop mode on competition bot
 */

public class TeleopModeLogic extends AbstractModeLogic {

	private static final Logger sLogger = LogManager.getLogger(TeleopModeLogic.class);

	private Boolean mFloorCollect = false;
	private Boolean mWallCollect = false;
	private Boolean mPrime = false;
	private Boolean mShoot = false;
	private Boolean mDejam = false;
	private Boolean mProtect = false;

	public TeleopModeLogic(InputValues inputValues, RobotConfiguration robotConfiguration) {
		super(inputValues, robotConfiguration);
	}

	@Override
	public void initialize() {
		sLogger.info("***** TELEOP *****");

		mFloorCollect = false;
		mWallCollect = false;
		mPrime = false;
		mShoot = false;
		mDejam = false;
		mProtect = false;
	}

	@Override
	public void update() {

		// Protect
		if(fSharedInputValues.getBooleanRisingEdge("ipb_operator_dpad_right")){
			mFloorCollect = false;
			mWallCollect = false;
			mPrime = false;
			mShoot = false;
			mProtect = true;
		}

		// Floor Collect
		if(fSharedInputValues.getBooleanRisingEdge("ipb_operator_left_trigger")){
			mFloorCollect = !mFloorCollect;
			mWallCollect = false;
			if(mFloorCollect) {
				mPrime = false;
				mShoot = false;
			}
			mProtect = false;
		}

		// Wall Collect
		if(fSharedInputValues.getBooleanRisingEdge("ipb_operator_left_bumper")){
			mWallCollect = !mWallCollect;
			mFloorCollect = false;
			if(mWallCollect) {
				mPrime = false;
				mShoot = false;
			}
			mProtect = false;
		}

		// Prime
		if(fSharedInputValues.getBooleanRisingEdge("ipb_operator_right_bumper")){
			mPrime = !mPrime;
			if(mPrime){
				mFloorCollect = false;
				mWallCollect = false;
			}
			mShoot = false;
			mProtect = false;
		}

		// Shoot - If either the driver or the operator pull their right trigger AND the system is priming AND either the system is ready to shoot or the operator overrides with the left dPad
		if((fSharedInputValues.getBoolean("ipb_operator_right_trigger") || fSharedInputValues.getBoolean("ipb_driver_right_trigger"))
				&& (fSharedInputValues.getBoolean("ipb_primed_to_shoot") || fSharedInputValues.getBoolean("ipb_operator_dpad_left"))){
			mShoot = true;
			mPrime = false;
		}

		if(fSharedInputValues.getBooleanFallingEdge("ipb_operator_right_trigger") || fSharedInputValues.getBooleanFallingEdge("ipb_driver_right_trigger")){
			mShoot = false;
			mPrime = true;
		}


		// Dejam
		mDejam = fSharedInputValues.getBoolean("ipb_operator_dpad_up");
	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean isReady(String name) {
		switch (name) {
			// Zeros
			case "st_drivetrain_zero":
				return !fSharedInputValues.getBoolean("ipb_drivetrain_has_been_zeroed");
			case "st_collector_zero":
				return !fSharedInputValues.getBoolean("ipb_collector_has_been_zeroed");
			case "st_hopper_zero":
				return !fSharedInputValues.getBoolean("ipb_hopper_has_been_zeroed");
			case "st_elevator_zero":
				return !fSharedInputValues.getBoolean("ipb_elevator_has_been_zeroed");
			case "st_flywheel_zero":
				return !fSharedInputValues.getBoolean("ipb_flywheel_has_been_zeroed");
			case "st_turret_zero":
				return !fSharedInputValues.getBoolean("ipb_turret_has_been_zeroed");

			// Drivetrain
			case "st_drivetrain_velocity":
				return fSharedInputValues.getBoolean(fRobotConfiguration.getString("global_drivetrain", "velocity_mode_control"));

			// Parallels
			case "pl_intake_floor":
				return mFloorCollect;
			case "pl_intake_wall":
				return mWallCollect;
			case "pl_prime":
				return mPrime;
			case "pl_shoot":
				return mShoot;
			case "pl_dejam":
				return mDejam;
			case "pl_protect":
				return mProtect;
			default:
				return false;
		}
	}

	@Override
	public boolean isDone(String name, State state) {
		switch (name) {
			case "st_drivetrain_velocity":
				return !fSharedInputValues.getBoolean(fRobotConfiguration.getString("global_drivetrain", "velocity_mode_control"));
			case "pl_intake_floor":
				return !mFloorCollect;
			case "pl_intake_wall":
				return !mWallCollect;
			case "pl_prime":
				return !mPrime;
			case "pl_shoot":
				return !mShoot;
			case "pl_dejam":
				return !mDejam;
			case "pl_protect":
				return !mProtect;
			default:
				return state.isDone();
		}
	}
}
