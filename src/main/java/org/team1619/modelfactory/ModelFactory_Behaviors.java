package org.team1619.modelfactory;

import org.team1619.behavior.*;
import org.uacr.models.behavior.Behavior;
import org.uacr.models.exceptions.ConfigurationException;
import org.uacr.robot.AbstractModelFactory;
import org.uacr.shared.abstractions.InputValues;
import org.uacr.shared.abstractions.ObjectsDirectory;
import org.uacr.shared.abstractions.OutputValues;
import org.uacr.shared.abstractions.RobotConfiguration;
import org.uacr.utilities.Config;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

public class ModelFactory_Behaviors extends AbstractModelFactory {

	private static final Logger sLogger = LogManager.getLogger(ModelFactory_Behaviors.class);

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final RobotConfiguration fRobotConfiguration;

	public ModelFactory_Behaviors(InputValues inputValues, OutputValues outputValues, RobotConfiguration robotConfiguration, ObjectsDirectory objectsDirectory) {
		super(inputValues, outputValues, robotConfiguration, objectsDirectory);
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fRobotConfiguration = robotConfiguration;
	}

	public Behavior createBehavior(String name, Config config) {
		sLogger.trace("Creating behavior '{}' of type '{}' with config '{}'", name, config.getType(), config.getData());

		switch (name) {

			// Drivetrain
			case "bh_drivetrain_zero":
				return new Drivetrain_Zero(fSharedInputValues, fSharedOutputValues, config, fRobotConfiguration);
			case "bh_drivetrain_states":
				return new Drivetrain_States(fSharedInputValues, fSharedOutputValues, config, fRobotConfiguration);

			// Collector
			case "bh_collector_zero":
				return new Collector_Zero(fSharedInputValues, fSharedOutputValues, config, fRobotConfiguration);
			case "bh_collector_states":
				return new Collector_States(fSharedInputValues, fSharedOutputValues, config, fRobotConfiguration);

			// Hopper
			case "bh_hopper_zero":
				return new Hopper_Zero(fSharedInputValues, fSharedOutputValues, config, fRobotConfiguration);
			case "bh_hopper_states":
				return new Hopper_States(fSharedInputValues, fSharedOutputValues, config, fRobotConfiguration);

			// Elevator
			case "bh_elevator_zero":
				return new Elevator_Zero(fSharedInputValues, fSharedOutputValues, config, fRobotConfiguration);
			case "bh_elevator_states":
				return new Elevator_States(fSharedInputValues, fSharedOutputValues, config, fRobotConfiguration);

			// Flywheel
			case "bh_flywheel_zero":
				return new Flywheels_Zero(fSharedInputValues, fSharedOutputValues, config, fRobotConfiguration);
			case "bh_flywheel_states":
				return new Flywheels_States(fSharedInputValues, fSharedOutputValues, config, fRobotConfiguration);

			// Turret
			case "bh_turret_zero":
				return new Turret_Zero(fSharedInputValues, fSharedOutputValues, config, fRobotConfiguration);
			case "bh_turret_states":
				return new Turret_States(fSharedInputValues, fSharedOutputValues, config, fRobotConfiguration);


			// State not found
			default:
				throw new ConfigurationException("Behavior " + name + " does not exist.");
		}
	}

}