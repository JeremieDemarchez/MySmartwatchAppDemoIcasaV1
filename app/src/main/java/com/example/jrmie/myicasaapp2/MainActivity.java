package com.example.jrmie.myicasaapp2;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ListView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;

import dataManagement.FileOperations;
import layout.MyCustomAdapter;
import service.network.MqttManager;
import service.network.TCPClientService;
import service.sensors.DataSensorMemorizerService;
import utils.DateTool;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private ListView mList;
    private ArrayList<String> arrayList;
    private MyCustomAdapter mAdapter;

    private FileOperations fileOp = null;

    private MainActivity me;

    private DataSensorMemorizerService dataSensorService;
    private TCPClientService tcpClientService;

    private final String sensorServiceName = "service.sensors.DataSensorMemorizerService";
    private final String tcpServiceName = "service.network.TCPClientService";

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected : componentName == "+componentName.getShortClassName());
            if(componentName.getShortClassName().equals(sensorServiceName)){
                dataSensorService = ((DataSensorMemorizerService.DataSensorBinder)iBinder).getService();
                dataSensorService.setActivity(me);
            }
            else if(componentName.getShortClassName().equals(tcpServiceName)){
                tcpClientService = ((TCPClientService.TcpBinder)iBinder).getService();
                tcpClientService.setActivity(me);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected : componentName == "+componentName.getShortClassName());
            if(componentName.getShortClassName().equals(sensorServiceName)){
                dataSensorService = null;
            }
            else if(componentName.getShortClassName().equals(tcpServiceName)){
                tcpClientService = null;
            }
        }
    };

    public void setList(int position, String value){
        arrayList.set(position, value);
        mAdapter.notifyDataSetChanged();
        //Toast.makeText(this, "List updated", Toast.LENGTH_LONG).show();
        Log.d(TAG, "setList("+position+", "+value+")");
    }

    /**
     * initialise the application view
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        me=this;

        initList();

        //relate the listView from java to the one created in xml
        mList = (ListView) findViewById(R.id.list);
        mAdapter = new MyCustomAdapter(this, arrayList);
        mList.setAdapter(mAdapter);

        fileOp = new FileOperations(getApplicationContext());

        //fileOp.cleanFiles();

        //Service initialisation
        Intent sensorIntent = new Intent(me, DataSensorMemorizerService.class);
        //Intent tcpIntent = new Intent(me, TCPClientService.class);
        startService(sensorIntent);
        //startService(tcpIntent);
        bindService(sensorIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        //bindService(tcpIntent, serviceConnection, Context.BIND_AUTO_CREATE);


        Log.d(TAG, "Date == "+ DateTool.getDateAsString());
        //printRunningServices();
        //doTest1();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent sensorIntent = new Intent(me, DataSensorMemorizerService.class);
        //Intent tcpIntent = new Intent(me, TCPClientService.class);
        bindService(sensorIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        //bindService(tcpIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(serviceConnection);
    }

    private void initList(){
        arrayList = new ArrayList<String>();
        arrayList.add("Heart beat charging ...");
        arrayList.add("Number of step charging ...");
        arrayList.add("Gyroscope charging ...");
        arrayList.add("Connexion state ?");
        arrayList.add("Client state ?");
    }

    /*
        private BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals("DataSensorMemorizerService")){
                    String value = intent.getStringExtra(ExtraKey.getValueKey());
                    String sensorType = intent.getStringExtra(ExtraKey.getSensorTypeKey());

                    if(sensorType.equals(SensorKey.getHeartBeatKey())){
                        arrayList.set(0, value);
                    }
                    else if(sensorType.equals(SensorKey.getStepCounterKey())){
                        arrayList.set(1, value);
                    }
                    mAdapter.notifyDataSetChanged();
                }
                else if(intent.getAction().equals("TCPClientService")){
                    String value = intent.getStringExtra(ExtraKey.getValueKey());
                    arrayList.set(2, value);
                    mAdapter.notifyDataSetChanged();
                }
            }
        };
    */
    /*
    public static class ExtraKey {
        private final static String value = "VALUE";
        private final static String sensorType = "SENSOR_TYPE";
        public  static String getValueKey(){ return value;}
        public static String getSensorTypeKey(){ return sensorType;}
    }

    public static class SensorKey {
        private final static String heartBeat = "HEART_BEAT";
        private final static String stepCounter = "STEP_COUNTER";
        public  static String getHeartBeatKey(){ return heartBeat;}
        public static String getStepCounterKey(){ return stepCounter;}
    }

    private boolean isMyServiceRunning(String className) {
        ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (className.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    private void printRunningServices() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            Log.i(TAG, service.service.getClassName());
        }
    }
    */

    private void doTest1(){
        String clientId = MqttClient.generateClientId();
        MqttAndroidClient mqttClient = new MqttAndroidClient(getApplicationContext(), MqttManager.MqttBroker.getUri(), clientId);

        try {
            Log.i(TAG, "Try to connect client to broker \""+ MqttManager.MqttBroker.getUri()+"\".");
            IMqttToken token = mqttClient.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.e(TAG, "onSuccess test 1 : mqtt client connected.");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.e(TAG, "onFailure test1 : failed to connect mqtt client ...");
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, "Error during mqtt client connection ...");
            e.printStackTrace();
        }
    }
}
