package service;

import dataManagement.FileOperations;
import service.sensors.GyroscopeService;
import service.sensors.StepCounterService;

/**
 * Created by Jérémie on 21/12/2016.
 */

public class SmartwatchServices implements GyroscopeService, StepCounterService {

    FileOperations fileOp;

    public SmartwatchServices(FileOperations fileOpe){
        fileOp = fileOpe;
    }

    @Override
    public String getCurrentValues() {
        return fileOp.getLastValue(FileOperations.SensorType.GYROSCOPE);
    }

    @Override
    public String getGyroHistory() {
        return fileOp.readFile(FileOperations.SensorType.GYROSCOPE);
    }

    @Override
    public String getNumberOfStep() {
        return fileOp.getLastValue(FileOperations.SensorType.STEP_COUNTER);
    }

    @Override
    public String getStepHistory() {
        return fileOp.readFile(FileOperations.SensorType.STEP_COUNTER);
    }
}
