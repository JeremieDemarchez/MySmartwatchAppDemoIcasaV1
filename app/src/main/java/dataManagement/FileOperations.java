package dataManagement;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import utils.DateTool;

/**
 * Created by Jérémie on 15/11/2016.
 */

public class FileOperations {

    //private final String FILEPATH = "SensorData/";
    private final String TAG = "FileOperations";
    private File file_stepCounter;
    private File file_heartRate;
    private File file_gyro;
    private final String stepCounterFileName = "StepCounter.txt";
    private final String heartRateFileName = "HeartRate.txt";
    private final String gyroFileName = "Gyroscope.txt";

   // private TimerTask cleanFileTask;

    public enum SensorType{HEART_RATE, STEP_COUNTER, GYROSCOPE};

/**
 * Create the data files and set the daily file cleaner
 */
    public FileOperations(Context ctx) {
        file_stepCounter = new File(ctx.getFilesDir(), stepCounterFileName);
        file_heartRate = new File(ctx.getFilesDir(), heartRateFileName);
        file_gyro = new File(ctx.getFilesDir(), gyroFileName);

        // If file does not exists, then create it
        if (!file_stepCounter.exists()) {
            try {
                file_stepCounter.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
       /* else{// If file already exist, delete it and create a new one.
            file_stepCounter.delete();
            try {
                file_stepCounter.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

        // If file does not exists, then create it
        if (!file_heartRate.exists()) {
            try {
                file_heartRate.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
       /*else{// If file already exist, delete it and create a new one.
            file_heartRate.delete();
            try {
                file_heartRate.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        // If file does not exists, then create it
        if (!file_gyro.exists()) {
            try {
                file_gyro.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
       /*else{// If file already exist, delete it and create a new one.
            file_gyro.delete();
            try {
                file_gyro.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
/*
        //TODO Plannification for daily clean of old value in files
        cleanFileTask = new TimerTask() {
            @Override
            public void run() {
                cleanFiles();
            }
        };
        Timer timer = new Timer();
        long ONCE_PER_DAY = 1000*60*60*24;
        timer.schedule(cleanFileTask, ONCE_PER_DAY, ONCE_PER_DAY);
*/
    }

    public void write(int value, SensorType typeOfSensor){
        //Log.d("FileOperations", "File content : "+read());
        String date = DateTool.getDateAsString();
        String fcontent = date+" = "+value;
        try {
            FileWriter fw;
            if(typeOfSensor.equals(SensorType.HEART_RATE)) {
                fw = new FileWriter(file_heartRate.getAbsoluteFile(), true);
            }
            else if(typeOfSensor.equals(SensorType.GYROSCOPE)){
                fw = new FileWriter(file_gyro.getAbsoluteFile(), true);
            }
            else{
                fw = new FileWriter(file_stepCounter.getAbsoluteFile(), true);
            }
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(fcontent+"\n");
            //bw.newLine();
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String value, SensorType typeOfSensor){
        //Log.d("FileOperations", "File content : "+read());
        String date = DateTool.getDateAsString();
        String fcontent = date+" = "+value;
        try {
            FileWriter fw;
            if(typeOfSensor.equals(SensorType.HEART_RATE)) {
                fw = new FileWriter(file_heartRate.getAbsoluteFile(), true);
            }
            else if(typeOfSensor.equals(SensorType.GYROSCOPE)){
                fw = new FileWriter(file_gyro.getAbsoluteFile(), true);
            }
            else{
                fw = new FileWriter(file_stepCounter.getAbsoluteFile(), true);
            }
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(fcontent+"\n");
            //bw.newLine();
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String readFile(SensorType typeOfSensorDataInFile){

        BufferedReader br = null;
        String response = "";

        try {

            StringBuffer output = new StringBuffer();
            if(typeOfSensorDataInFile.equals(SensorType.HEART_RATE)) {
                br = new BufferedReader(new FileReader(file_heartRate));
            }
            else if(typeOfSensorDataInFile.equals(SensorType.GYROSCOPE)){
                br = new BufferedReader(new FileReader(file_gyro));
            }
            else {
                br = new BufferedReader(new FileReader(file_stepCounter));
            }
            String line = br.readLine(), nextline = null;
            while (line != null) {
                nextline = br.readLine();
                if(nextline!=null){
                    output.append(line+" | ");
                } else{
                    output.append(line);
                }
                line = nextline;
            }
            response = output.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return null;

        }
        //Log.e(TAG, "response history = "+response);
        return response;

    }

    public String getLastValue(SensorType typeOfSensorDataInFile){  //TODO améliorer cette fonction (on parcours tout le fichier pour réccupérer seulement la dernière valeur...)
        BufferedReader br = null;
        String response;
        try {
            StringBuffer output = new StringBuffer();
            if(typeOfSensorDataInFile.equals(SensorType.HEART_RATE)) {
                br = new BufferedReader(new FileReader(file_heartRate));
            }
            else if(typeOfSensorDataInFile.equals(SensorType.GYROSCOPE)){
                br = new BufferedReader(new FileReader(file_gyro));
            }
            else {
                br = new BufferedReader(new FileReader(file_stepCounter));
            }
            String line = br.readLine(), nextline = null;
            while (line != null) {
                nextline = br.readLine();
                if(nextline==null){
                    output.append(line);
                }
                line = nextline;
            }
            response = output.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
       // Log.e(TAG, "In getLastValue : response == "+response);
       // Log.e(TAG, "In getLastValue : getValuePart(response) == "+getValuePart(response));
        return getValuePart(response);

    }

    public void cleanFiles(){
        //TODO : corriger et tester cette fonction
        new Thread(){
            public void run(){
                cleanFile(SensorType.STEP_COUNTER);
            }
        }.start();

        new Thread(){
            public void run(){
                cleanFile(SensorType.HEART_RATE);
            }
        }.start();

        new Thread(){
            public void run(){
                cleanFile(SensorType.GYROSCOPE);
            }
        }.start();
    }

    private void cleanFile(SensorType typeOfSensorDataFile){
        Log.e("FileOperation", "File cleaning ...");

        File tmpFile = null, fileToClean = null;
        BufferedReader br = null;

        try {
            //initialisations according to the type of data file to clean
            if(typeOfSensorDataFile == SensorType.STEP_COUNTER) {
                //Log.e(TAG, "file_stepCounter.getParent() == "+file_stepCounter.getParent());
                tmpFile = new File(file_stepCounter.getParent(), "tmpStep.txt");
                fileToClean = this.file_stepCounter;
                br = new BufferedReader(new FileReader(file_stepCounter));
            }
            else if (typeOfSensorDataFile == SensorType.HEART_RATE){
                tmpFile = new File(file_heartRate.getParent(), "tmpHeart.txt");
                fileToClean = this.file_heartRate;
                br = new BufferedReader(new FileReader(file_heartRate));
            }
            else if (typeOfSensorDataFile == SensorType.GYROSCOPE){
                tmpFile = new File(file_gyro.getParent(), "tmpGyroscope.txt");
                fileToClean = this.file_gyro;
                br = new BufferedReader(new FileReader(file_gyro));
            }


            //if no errors during initialisations
            if(tmpFile != null && br != null && fileToClean != null) {
                String stringDateTwoDaysBefore = DateTool.getDateTwoDaysBeforeAsString();
                String line = br.readLine();
                boolean isFileToClean = false;

                while (line != null) {
                    if(line.contains(" = ") && !DateTool.isBefore(getDatePart(line), stringDateTwoDaysBefore)) {
                        write(tmpFile, line);
                        isFileToClean = true;
                    }
                    line = br.readLine();
                }
                if(isFileToClean) {
                    copyFile(tmpFile, fileToClean);
                    tmpFile.delete();
                }
            }
            else{
                Log.e(TAG, "Error during initialisation phase of file cleaning task ...");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void copyFile(File toCopy, File toCrush){
        try {
            FileWriter fw;
            if(toCrush.equals(file_heartRate)) {
                fw = new FileWriter(file_heartRate.getAbsoluteFile(), false);
            }
            else if(toCrush.equals(file_gyro)){
                fw = new FileWriter(file_gyro.getAbsoluteFile(), false);
            }
            else{
                fw = new FileWriter(file_stepCounter.getAbsoluteFile(), false);
            }
            BufferedWriter bw = new BufferedWriter(fw);

            BufferedReader br = new BufferedReader(new FileReader(toCopy));

            String line = br.readLine();
            while (line != null) {
                bw.write(line+"\n");
                line = br.readLine();
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public String getValuePart(String sensorDataFileEntry){
        try{
            //Log.e(TAG, "sensorDataFileEntry.substring(sensorDataFileEntry.indexOf(\" = \")+3, sensorDataFileEntry.length()) == "+sensorDataFileEntry.substring(sensorDataFileEntry.indexOf(" = ")+3, sensorDataFileEntry.length()));
            return sensorDataFileEntry.substring(sensorDataFileEntry.indexOf(" = ")+3, sensorDataFileEntry.length());
        }catch(StringIndexOutOfBoundsException e){
            e.printStackTrace();
            return  null;
        }
    }

    public String getDatePart(String sensorDataFileEntry){
        try {
            return sensorDataFileEntry.substring(0, sensorDataFileEntry.indexOf(" = "));
        }catch(StringIndexOutOfBoundsException e){
            e.printStackTrace();
            return null;
        }
    }

    private void write(File file, String value){
        try {
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write(value+"\n");
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    public int getNumberOfLineInFile(){
        int cpt = 0;

        BufferedReader br = null;
        String response = null;

        try {

            StringBuffer output = new StringBuffer();

            br = new BufferedReader(new FileReader(file));
            String line = "", lastline = "";
            while ((line = br.readLine()) != null) {
                cpt++;
                lastline = line;
            }
           // Log.d("TAG", "Last line of file = "+output.append(lastline));

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        return cpt;
    }
     */
}
