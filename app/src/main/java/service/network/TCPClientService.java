package service.network;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.jrmie.myicasaapp2.MainActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import dataManagement.FileOperations;

/**
 * Created by Jérémie on 30/11/2016.
 */

public class TCPClientService extends Service {

    private final String TAG = "TCPClientService";

    private String serverRequest;
    public static final String SERVERIP = "129.88.51.44"; //your computer IP address
    //public static final String SERVERIP = "192.168.1.47"; //your computer IP address
    public static final int SERVERPORT = 4444;
    private OnMessageReceived mMessageListener = null;
    private boolean isServiceRunning = false;
    private boolean isConnected = false;
    private boolean isClientConnected = false;

    private PrintWriter out;
    private BufferedReader in;

    private FileOperations fileOp;

    private TCPClientService me;
    private Socket socket = null;
    private IBinder myBinder;
    private MainActivity activity;

    private String connectionState = "Disconnected";

    /**
     * This receiver allows to monitor internet connectivity changes
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        if (isNetworkAvailable()) {//if an internet connectivity is available
            if (!isConnected) {//if the TCPClient is not running then we start it
                new connectTask().execute("");

                if(activity!=null){
                    activity.setList(3, "Connected");
                }
            }
        } else {//if there isn't internet connectivity
            if (isConnected) {//if the TCPClient is running then we stop it.
                stopClient();

                if(activity!=null){
                    activity.setList(3, "Disonnected ...");
                }
            }
        }
        }
    };

    /**
     * Création du service
     */
    @Override
    public void onCreate(){
        super.onCreate();
        me=this;
        myBinder = new TcpBinder();
        //to monitor connectivity changes
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(receiver, filter);

        //initialisation of the attributes
        fileOp = new FileOperations(getApplicationContext());
        Log.d(TAG, "TCP service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this, "TCP service starting", Toast.LENGTH_SHORT).show();

        if(!isServiceRunning) {
            isServiceRunning = true;
            //to connect this client to the distant server through internet
            new connectTask().execute("");                                      //TODO start and stop according to the internet changes -> to test
        }
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }


    /**
     * Connexion d'une activité au service
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    /**
     * Destruction du service
     */
    @Override
    public void onDestroy(){
        super.onCreate();
        unregisterReceiver(receiver);
        this.stopClient();
    }


    public void setActivity(MainActivity mainActivity){
        Log.d(TAG, "setActivity called.");
        activity = mainActivity;
        majView();
    }

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

    private String getConnectionStateString(){
        if(isNetworkAvailable()) return "Connected";
        return "Disconnected";
    }

    /**
     * Enum to check and parse the server requests
     */
    public enum PossibleRequests{
        GET_HEART_RATE, GET_STEP_COUNT, GET_HEART_RATE_HISTORY, GET_STEP_COUNT_HISTORY, HELP;

        public static PossibleRequests getValueOf(String request){
            if(request.equalsIgnoreCase("GET_HEART_RATE")) return PossibleRequests.GET_HEART_RATE;
            else if(request.equalsIgnoreCase("GET_STEP_COUNT")) return PossibleRequests.GET_STEP_COUNT;
            else if(request.equalsIgnoreCase("GET_HEART_RATE_HISTORY")) return PossibleRequests.GET_HEART_RATE_HISTORY;
            else if(request.equalsIgnoreCase("GET_STEP_COUNT_HISTORY")) return PossibleRequests.GET_STEP_COUNT_HISTORY;
            return PossibleRequests.HELP;
        }
    };

    /**
     * Pour stopper le client tcp (provoque la fin de l'execution de run()).
     */
    public void stopClient(){
        isConnected = false;
    }

    /**
     * Initialisation de la connexion avec le server
     */
    public void run() {
        isConnected = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);

            Log.e("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, SERVERPORT);

            try {
                if(socket.isConnected()) {
                    Log.e(TAG, "CLIENT CONNECTED TO SERVER");
//                    majView();

                    //send the message to the server
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                    //receive the message which the server sends back
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    //in this while the client listens for the messages sent by the server
                    while (isConnected) {
                        Log.e(TAG, "Wait for server request ...");
                        //runServerRequest(PossibleRequests.getValueOf("help"));

                        serverRequest = in.readLine();

                        if (serverRequest != null && mMessageListener != null) {
                            //call the method messageReceived from MyActivity class
                            //mMessageListener.requestReceived(serverRequest);
                            Log.d(TAG, "serverRequest == "+serverRequest);
                            runServerRequest(PossibleRequests.getValueOf(serverRequest));
                        }
                        else{
                            //Log.e("TCPClient","serverRequest != null && mMessageListener != null =? " + (serverRequest != null && mMessageListener != null) + "'");
                            Log.e("TCPClient","mMessageListener != null =? " + (mMessageListener != null) + "'");
                            Log.e("TCPClient","serverRequest != null =? " + (serverRequest != null)  + "'");
                            stopClient();
                        }
                        serverRequest = null;

                    }

                    Log.d("REQUEST FROM SERVER", "S: Received request: '" + serverRequest + "'");
                }
                else{
                    Log.e("NOT CONNECTED TO SERVER", "socket.isConnected() = '" + socket.isConnected() + "'");
                }

            } catch (Exception e) {

                Log.e("TCP", "S: Error, client is stopping ...", e);
                socket.close();
                this.stopClient();
//                majView();

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
                socket = null;
            }

        } catch (Exception e) {
            isConnected = false;
            Log.e("TCP", "C: Error during socket initialisation ...", e);

        }
//        majView();
    }

    /**
     * This method is used to actualize the view when needed
     */
    private void majView(){
        if(activity!=null){
            boolean isClientConnected = ((socket != null) && (socket.isConnected())) ? true : false;
            activity.setList(3, getConnectionStateString());
            activity.setList(4, ""+isClientConnected);
        }
    }

    /**
     * execute la requète du server
     * @param request la requète du server
     */
    private void runServerRequest(PossibleRequests request){
        String response = null;

        if(request.equals(PossibleRequests.GET_HEART_RATE)){
            response = new String(""+this.fileOp.getLastValue(FileOperations.SensorType.HEART_RATE));
        }
        else if(request.equals(PossibleRequests.GET_HEART_RATE_HISTORY)){
            response = new String(this.fileOp.readFile(FileOperations.SensorType.HEART_RATE));
        }
        else if(request.equals(PossibleRequests.GET_STEP_COUNT)){
            response = new String(""+this.fileOp.getLastValue(FileOperations.SensorType.STEP_COUNTER));
        }
        else if(request.equals(PossibleRequests.GET_STEP_COUNT_HISTORY)){
            response = new String(this.fileOp.readFile(FileOperations.SensorType.STEP_COUNTER));
        }
        else{//either "HELP" or invalid request
            response = new String("Possible requests are :  GET_HEART_RATE, GET_STEP_COUNT, GET_HEART_RATE_HISTORY, GET_STEP_COUNT_HISTORY, HELP.");
        }

       // Log.d(TAG, "serverRequest == "+request);
       // Log.d(TAG, "this.fileOp.getLastValue(FileOperations.SensorType.STEP_COUNTER) == "+this.fileOp.getLastValue(FileOperations.SensorType.STEP_COUNTER));
        Log.d(TAG, "serverResponse == "+response);

        sendResponseToServer(response.toString());
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public Void sendResponseToServer(String... message) {

        if (out != null && !out.checkError()) {
            //Log.e("DoInBackground", message[0]);
            //out.println(message);
            //out.flush();
            new Sender(out).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,message);
        }
        else{
            Log.e("TCPClient.java", "sendMessage(String message) failed: out =" + out);
        }
        return null;
    }

    /**
     * Envoie de la réponse au server
     */
    private class Sender extends AsyncTask<String, Void, Void> {
        PrintWriter out = null;
        public Sender(PrintWriter printWriter){
            out = printWriter;
        }
        public Void doInBackground(String... toSend){
            Log.d("TCPClient", "Response of server request : "+toSend[0]);
            out.println(toSend[0]);
            out.flush();
            //Log.e("TCP", "After sending message");
            return null;
        }
    }

    /**
     * this class allows the client to connect to the server
     */
    public class connectTask extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... message) {

            //we create a TCPClient object and
            if(mMessageListener == null) {
                mMessageListener = new OnMessageReceived() {
                    @Override
                    //here the messageReceived method is implemented
                    public void messageReceived(String message) {
                        //this method calls the onProgressUpdate
                        publishProgress(message);
                    }
                };
            }
            run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

    public class TcpBinder extends Binder {
        public TCPClientService getService(){
            return TCPClientService.this;
        }
    }
}
