package utils;

import service.SmartwatchServices;

/**
 *
 *   Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under a specific end user license agreement;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://adeleresearchgroup.github.com/iCasa/snapshot/license.html
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

public class SmartwatchOperations {
	
	//code of services
	private final static int STEP_COUNTER_SERVICE = 0;
	private final static int GYROSCOPE_SERVICE = 1;
	
	//code of methods for each service 
	//STEP_COUNTER_SERVICE
	private final static int GET_NUMBER_OF_STEP = 0;
	private final static int GET_NUMBER_OF_STEP_HISTORY = 1;
	//GYROSCOPE_SERVICE
	private final static int GET_XYZ_AXIS_VALUES = 2;
	private final static int GET_GYROSCOPE_HISTORY = 3;
	private final static int GET_DEVICE_TYPE = 4;
	
	//TODO
	public static String getServiceName(int code){
		switch (code) {
		case STEP_COUNTER_SERVICE:
			return "StepCounterService";
		case GYROSCOPE_SERVICE:
			return "GyroscopeService";

		default:
			break;
		}
		return null;
	}
	
	//TODO
	public static int getServiceCode(String serviceName){
		
		if(serviceName.equals("StepCounterService")){
			return STEP_COUNTER_SERVICE;
		}
		else if(serviceName.equals("GyroscopeService")){
			return GYROSCOPE_SERVICE;
		}
		return -1;
	}
	
	//TODO
	public static String getMethodName(int serviceCode, int methodCode){
		switch (serviceCode) {
		case STEP_COUNTER_SERVICE:
			switch (methodCode) {
			case GET_NUMBER_OF_STEP:
				return "getNumberOfStep";
			case GET_NUMBER_OF_STEP_HISTORY:
				return "getStepHistory";
			default:
				break;
			}
			break;
		case GYROSCOPE_SERVICE:
			switch (methodCode) {
			case GET_XYZ_AXIS_VALUES:
				return "getCurrentValues";
			case GET_GYROSCOPE_HISTORY:
				return "getGyroHistory";
			default:
				break;
			}
			break;

		default:
			break;
		}
		return null;
	}
	
	//TODO
	public static int getMethodCode(String serviceName, String methodName){
		
		if(serviceName.equals("GyroscopeService")){
			if(methodName.equals("getCurrentValues")){
				return GET_XYZ_AXIS_VALUES;
			}
			else if(methodName.equals("getGyroHistory")){
				return GET_GYROSCOPE_HISTORY;
			}
		}
		else if(serviceName.equals("StepCounterService")){
			if(methodName.equals("getNumberOfStep")){
				return GET_NUMBER_OF_STEP;
			}
			else if(methodName.equals("getStepHistory")){
				return GET_NUMBER_OF_STEP_HISTORY;
			}
		}
		return -1;
	}

	public static String callMethodFromCode(int methodCode, SmartwatchServices services){

		switch (methodCode) {
			case GET_NUMBER_OF_STEP:
				return services.getNumberOfStep();
			case GET_NUMBER_OF_STEP_HISTORY:
				return services.getStepHistory();
			case GET_XYZ_AXIS_VALUES:
				return services.getCurrentValues();
			case GET_GYROSCOPE_HISTORY:
				return services.getGyroHistory();
			default:
				break;
		}
		return null;
	}
	
	
	public static String getIcasaServiceName(int code){
		switch (code) {
		case GYROSCOPE_SERVICE:
			return "MqttGyroscopeService";
		default:
			break;
		}
		return null;
	}
	
	
	public static int getIcasaServiceCode(String serviceName){
		
		if(serviceName.equals("MqttGyroscopeService")){
			return GYROSCOPE_SERVICE;
		}
		return -1;
	}
	
	
	public static String getIcasaMethodName(int serviceCode, int methodCode){
		switch (serviceCode) {
		case GYROSCOPE_SERVICE:
			switch (methodCode) {
			case GET_XYZ_AXIS_VALUES:
				return "askXYZAxisValues";
			case GET_GYROSCOPE_HISTORY:
				return "askHistory";
			case GET_DEVICE_TYPE:
				return "askDeviceType";
			default:
				break;
			}
			break;
		case STEP_COUNTER_SERVICE:
					
			break;
		default:
			break;
		}
		return null;
	}
	
	
	public static int getIcasaMethodCode(String serviceName, String methodName){
		
		if(serviceName.equals("MqttGyroscopeService")){
			if(methodName.equals("askXYZAxisValues")){
				return GET_XYZ_AXIS_VALUES;
			}
			else if(methodName.equals("askHistory")){
				return GET_GYROSCOPE_HISTORY;
			}
			else if(methodName.equals("askDeviceType")){
				return GET_DEVICE_TYPE;
			}
		}
		return -1;
	}
	
	
	
	
}