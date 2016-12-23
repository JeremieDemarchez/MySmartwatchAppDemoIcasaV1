package service.sensors;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.jrmie.myicasaapp2.MainActivity;

import java.util.List;

import dataManagement.FileOperations;
import service.SmartwatchServices;
import service.network.MqttManager;
import utils.DateTool;

/**
 * Created by Jérémie on 30/11/2016.
 */

public class DataSensorMemorizerService extends Service implements SensorEventListener {

    private final String TAG = "DataSensorMemorizerService";
    private final String TAG_SHORT = "DataSensorMemoServ";
    private boolean isServiceRunning = false;

    private FileOperations fileOp;

    private final int SENSOR_GYRO = Sensor.TYPE_GYROSCOPE; //identifier of heart rate sensor
    private final int SENSOR_HEART = Sensor.TYPE_HEART_BEAT; //identifier of heart rate sensor
    //private final int SENSOR_HEART = Sensor.TYPE_HEART_RATE; //identifier of heart rate sensor
    //private final int SENSOR_HEART = 65538;// identifier of the motion health sensor
    private final int SENSOR_STEP = 19; //identifier of step counter
    private final int SENSOR_DELAY = 5000000;
    private final String stringHeart = "Heart rate : ";
    private final String stringStep = "Number of step : ";
    private final String stringGyro = "(x, y, z) = ";
    private int lastHeartRateValue = -1;
    private int lastStepValue = 0;
    private Float[] lastGyroValue = new Float[3];
    private int pastDayStepValue = 0;
    private String lastHeartBeatDate;
    private String lastStepCounterDate;
    private String lastGyroDate;

    private IBinder myBinder;
    private MainActivity activity;

    private MqttManager mqttManager;

    /**
     * Création du service
     */
    @Override
    public void onCreate(){
        super.onCreate();
        myBinder = new DataSensorBinder();
        //initialisation of the attributes
        fileOp = new FileOperations(getApplicationContext());
        lastStepCounterDate  = DateTool.getDateAsString();
        lastHeartBeatDate  = DateTool.getDateAsString();
        lastGyroDate  = DateTool.getDateAsString();

        for(int i = 0; i < 3; i++) lastGyroValue[i] = new Float(0);


        mqttManager = new MqttManager(getApplicationContext());
        mqttManager.setSmartwatchServices(new SmartwatchServices(fileOp));

        Log.d(TAG_SHORT, "Sensor memorizer service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this, "Sensor memorizer service starting", Toast.LENGTH_SHORT).show();
        //printInfoHealthSensor();
        if(!isServiceRunning) {
            SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            Sensor bloodSensor = mSensorManager.getDefaultSensor(this.SENSOR_HEART);
            Sensor stepSensor = mSensorManager.getDefaultSensor(this.SENSOR_STEP);
            Sensor gyroSensor = mSensorManager.getDefaultSensor(this.SENSOR_GYRO);



            if (bloodSensor == null || stepSensor == null) {
                List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
                Log.e("DataSensorMemorizer", "Desired sensors unavailable. The available sensors are listed bellow :");
                for (Sensor availableSensor : sensors) {
                    Log.i("DataSensorMemorizer", availableSensor.getName() + ": " + availableSensor.getType());
                }
            }
            if(bloodSensor!=null) {
                mSensorManager.registerListener(this, bloodSensor, this.SENSOR_DELAY);

            }
            if(stepSensor!=null) {
                mSensorManager.registerListener(this, stepSensor, this.SENSOR_DELAY);
                mqttManager.publishService("StepCounterService");
            }
            if(gyroSensor!=null) {
                mSensorManager.registerListener(this, gyroSensor, this.SENSOR_DELAY);
                mqttManager.publishService("GyroscopeService");
            }
        }
       // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    /**
     * This receiver allows to monitor internet connectivity changes
     */
/*    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isNetworkAvailable()) {//if an internet connectivity is available
                if (!isConnected) {//if the TCPClient is not running then we start it
                    new TCPClientService.connectTask().execute("");

                    if(activity!=null){
                        activity.setList(3, "Connected");
                    }
                }
            } else {//if there isn't internet connectivity
                if (isConnected) {//if the TCPClient is running then we stop it.
                    mqttManager.

                    if(activity!=null){
                        activity.setList(3, "Disonnected ...");
                    }
                }
            }
        }
    };
*/
    /**
     * Check for internet connectivity
     * @return true if there is the device is connecting or connected to internet.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private void printInfoHealthSensor(){
        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor bloodSensor = mSensorManager.getDefaultSensor(this.SENSOR_HEART);
        Sensor stepSensor = mSensorManager.getDefaultSensor(this.SENSOR_STEP);

        Log.e(TAG_SHORT, "Sensor health info : max range = "+bloodSensor.getMaximumRange()+", string type = "+bloodSensor.getStringType()+", min delay = "+bloodSensor.getMinDelay()+", resolution = "+bloodSensor.getResolution()+", reporting mode = "+bloodSensor.getReportingMode());
    }

    /**
     * Destruction du service
     */
    @Override
    public void onDestroy(){
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public void setActivity(MainActivity mainActivity){
        Log.d(TAG_SHORT, "setActivity called.");
        activity = mainActivity;
        activity.setList(0, stringHeart+lastHeartRateValue);
        activity.setList(1, stringStep+lastStepValue);
        activity.setList(2, stringGyro+getGyroString(lastGyroValue));

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("DataSensorMemorizer", "onAccuracyChanged - accuracy: " + accuracy);
    }

    public void onSensorChanged(SensorEvent event) {
        String msg = "";
        //Log.d("DataSensorMemoServ", "onSensorChanged() : type = "+ event.sensor.getType() +", value = "+(int)event.values[0]);

        if (event.sensor.getType() == this.SENSOR_HEART) {
            //get the value
            this.lastHeartRateValue = (int)event.values[0];
            //MAJ the view
            if(activity!=null){
                activity.setList(0, stringHeart+lastHeartRateValue);
            }
            //memorize the value in a file to save the history
            fileOp.write(lastHeartRateValue, FileOperations.SensorType.HEART_RATE);

            //get actual date
            String date = DateTool.getDateAsString();
            //if actual date is recognize as a new day comparing to the previous measured value
            if(DateTool.isNewDay(lastHeartBeatDate, date)){
                fileOp.cleanFiles();
            }
            //we memorise the date of the actual measure to compare it for the next measure
            lastHeartBeatDate = date;
            testHeartValue(event);
        }
        else if (event.sensor.getType() == this.SENSOR_STEP){
            //initialisation of the start value of the step counter sensor (to deduce to the next measured values.
            if(pastDayStepValue==0){
                pastDayStepValue = (int)event.values[0];
            }
            //We get the value of the number of step since the beginning of the actual day
            this.lastStepValue = (int)event.values[0]-pastDayStepValue;
            // MAJ the user interface if util
            if(activity!=null){
                activity.setList(1, stringStep+lastStepValue);
            }
            //write the value in data file
            fileOp.write(lastStepValue, FileOperations.SensorType.STEP_COUNTER);

            //get actual date
            String date = DateTool.getDateAsString();
            //if actual date is recognize as a new day comparing to the previous measured value
            if(DateTool.isNewDay(lastStepCounterDate, date)){
                //we memorize the value of the step counter (to deduce to new measured values)
                pastDayStepValue = (int)event.values[0];
                fileOp.cleanFiles();
            }
            //we memorise the date of the actual measure to compare it for the next measure
            lastStepCounterDate = date;
        }
        else if (event.sensor.getType() == this.SENSOR_GYRO){
            this.lastGyroValue[0] = event.values[0];
            this.lastGyroValue[1] = event.values[1];
            this.lastGyroValue[2] = event.values[2];

            //MAJ the view TODO : modifier l'interface graphique
            if(activity!=null){
                activity.setList(2, stringGyro+getGyroString(lastGyroValue));
            }

            fileOp.write(getGyroString(lastGyroValue), FileOperations.SensorType.GYROSCOPE);

            //get actual date
            String date = DateTool.getDateAsString();
            //if actual date is recognize as a new day comparing to the previous measured value
            if(DateTool.isNewDay(lastGyroDate, date)){
                fileOp.cleanFiles();
            }
            //we memorise the date of the actual measure to compare it for the next measure
            lastGyroDate = date;
        }
        else {
            Log.e("DataSensorMemorizer", "Unknown sensor type");
        }
    }



    public class DataSensorBinder extends Binder {
        public DataSensorMemorizerService getService(){
            return DataSensorMemorizerService.this;
        }
    }

    private int testHeartValue(SensorEvent event){
        int cpt = 0;
        for(cpt=0; cpt < event.values.length; cpt++){
            Log.e(TAG_SHORT, "Value "+cpt+" = "+event.values[cpt]);
        }
        Log.e(TAG_SHORT, "Tere is "+cpt+" values for health sensor.");
        return cpt;
    }

    private String getGyroString(Float[] lastGyroValue){
        return "("+(int)(lastGyroValue[0]*100)+", "+(int)(lastGyroValue[1]*100)+", "+(int)(lastGyroValue[2]*100)+")";
    }
}
