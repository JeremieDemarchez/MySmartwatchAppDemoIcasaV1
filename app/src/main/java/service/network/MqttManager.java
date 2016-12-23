package service.network;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import service.SmartwatchServices;
import utils.DateTool;
import utils.SmartwatchOperations;

/**
 * Created by Jérémie Démarchez on 21/12/2016.
 */

public class MqttManager implements MqttCallback {

    private final String TAG = "MyMqttClient";

    private MqttAndroidClient mqttClient;
    private IMqttToken token;
    private SmartwatchServices swServices;

    private String clientId = ("[myHuaweiSmartwatch "+ DateTool.getDateAsString()+"]").replaceAll(" ", "_");//MqttClient.generateClientId();

    private List<String[]> listToPublish = new ArrayList<String[]>();


    /* public static MqttManager getInstance(){
         if(mqttManager == null) mqttManager = new MqttManager();
         return mqttManager;
     }
 */
    public void setSmartwatchServices(SmartwatchServices sw) {
        swServices = sw;
    }

    public MqttManager(Context ctxt) {
        super();
        // initialisation du client mqtt
        mqttClient = new MqttAndroidClient(ctxt, MqttBroker.getUri(), clientId);

        try {
            Log.i(TAG, "Try to connect client to broker \"" + MqttBroker.getUri() + "\".");
            token = mqttClient.connect();
            mqttClient.setCallback(this);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.e(TAG, "onSuccess : mqtt client connected.");

                    try {
                        token = mqttClient.subscribe(clientId, 0);
                        token.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                // We are connected
                                Log.i(TAG, "Subscription to topic '"+clientId+"' succeed.");

                                for (String[] toPublish : listToPublish) {
                                    publishMessage(toPublish[0], toPublish[1]);
                                    Log.e(TAG, "onSuccess : publish on topic '" + toPublish[0] + "', message content = '"+toPublish[1]+"'.");
                                }
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                // Something went wrong e.g. connection timeout or firewall problems
                                Log.e(TAG, "Subscription to topic '"+clientId+"' failed.");

                            }
                        });
                    } catch (MqttException e) {
                        Log.e(TAG, "Client cannot receive message : subscription failed");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.e(TAG, "onFailure : failed to connect mqtt client ...");
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, "Error during mqtt client connection ...");
            e.printStackTrace();
        }
    }

    public void publishService(String serviceName) {
        // trouver le code du service proposé, puis publier sur le broker

        int serviceCode = SmartwatchOperations.getServiceCode(serviceName);
        publishMessage(MqttBroker.topicIotService, this.clientId + "-" + serviceCode);

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        Log.e(TAG, "Message arrived ! topic = " + topic + ", message = " + message.toString());

        if (topic.equals(this.clientId)) {
            //Log.e(TAG, "step 1");
            String topicResult = message.toString();
            //Log.e(TAG, "step 2");

            String consumerId = getPart(0, message.toString());
            //Log.e(TAG, "step 3");
            String methodCode = getPart(1, message.toString());
            //Log.e(TAG, "step 4");
            //String[] argList = getArgs(2, message.toString());
            //Log.e(TAG, "step 5");

            String result = SmartwatchOperations.callMethodFromCode((int) (Integer.parseInt(methodCode)), swServices);
           // Log.e(TAG, "step 6");

            //Log.e(TAG, " Result = " + result);

            publishMessage(topicResult, result);
            //Log.e(TAG, "step 7");
        }
        else{
            Log.e(TAG, "Not interested by this topic ("+topic+"), message ignored");
        }
    }

    /**
     * Get each part of the message (each part are separated by "-").
     *
     * @param i       : the part number
     * @param message : the original message
     * @return : the part i in the message
     */
    private String getPart(int i, String message){
        String tmp = message;

        if(tmp.indexOf('-') == 0) tmp = tmp.substring(1);

        //String test = "-1";
        //System.out.println("test == "+test+", test.indexOf('-') == "+test.indexOf('-'));

        while(i>0){
            try{
                tmp = tmp.substring(tmp.indexOf("-")+1);
                i--;
            }catch(IndexOutOfBoundsException e){
                Log.e(TAG, "MqttServiceDiscovery : getPart("+i+", "+message+") throws IndexOutOfBoundsException.");
                e.printStackTrace();
                return null;
            }
        }
        //System.out.println("tmp == "+tmp);
        int end = tmp.indexOf("-") != -1 ? tmp.indexOf("-") : tmp.length();
        //System.out.println("end == "+end);
        return tmp.substring(0, end);
    }
/*    private String getPart(int i, String message) {
        String tmp = message;

        while (i > 0) {
            try {
                tmp = tmp.substring(tmp.indexOf("-"));
            } catch (IndexOutOfBoundsException e) {
                Log.e("MqttManager", "MqttServiceDiscovery : getPart(" + i + ", " + message + ") throws IndexOutOfBoundsException.");
                e.printStackTrace();
                return null;
            }
        }

        int end = tmp.indexOf("-") != -1 ? tmp.indexOf("-") : tmp.length();
        return tmp.substring(0, end);
    }
*/
    //TODO
    private String[] getArgs(int i, String message) {
        String tmp = message;

        while (i > 0) {
            try {
                tmp = tmp.substring(tmp.indexOf("-"));
            } catch (IndexOutOfBoundsException e) {
                Log.e("MqttManager", "MqttServiceDiscovery : getPart(" + i + ", " + message + ") throws IndexOutOfBoundsException.");
                e.printStackTrace();
                return null;
            }
        }

        return tmp.split("-");
    }


    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public static class MqttBroker {
        private final static String ADDRESS = "tcp://iot.eclipse.org";
        // private final static String ADDRESS 			= "tcp://broker.hivemq.com";
        private final static String PORT = "1883";
        private final static String topicIotService = "IOT/AVAILABLE_SERVICES";

        public static String getUri() {
            return ADDRESS + ":" + PORT;
        }

        public static String getTopicOfIotServices() {
            return topicIotService;
        }
    }

    private void publishMessage(String topic, String payload) {

        if (mqttClient.isConnected()) {
            byte[] encodedPayload = new byte[0];
            try {
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                //if(isRetainedMessage)
                message.setRetained(true);
                mqttClient.publish(topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Cannot publish message (mqtt client not connected yet)... Messages will be publish when connected.");
            String[] toPublish = new String[2];
            toPublish[0] = MqttBroker.topicIotService;
            toPublish[1] = payload;
            listToPublish.add(toPublish);
        }
    }
}
