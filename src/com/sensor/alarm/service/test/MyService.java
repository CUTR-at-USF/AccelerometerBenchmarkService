package com.sensor.alarm.service.test;

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.service.test.R;



import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This is an example of implementing an application service that will run in
 * response to an alarm, allowing us to move long duration work out of an
 * intent receiver.
 * 
 * @see AlarmService
 * @see AlarmService_Alarm
 */
public class MyService extends Service implements SensorEventListener {
	 String TAG = "MyService", 
			SENSOR = "Accelerometer", 
			BINFO = "Battery", 
			SAMPLES = "Counter",
			LOCATION = "WHere am I?",
			FILE = "File";
	
	
	   
    NotificationManager mNM;
       
    public static int interval = 15; //sec
    public static int duration = 5; //sec
    
    static SensorManager mSensorManager;
    static Sensor mAccelerometer;
    
    static int scale = -1;
	static int level = -1;
	static int voltage = -1;
	static int temp = -1;
	
	static File root;
	static File fileDir;
	static File file;
	static FileWriter filewriter;
	static BufferedWriter out;
	
	//DateTime variables
	static Date timestamp = new Date();
	
	static SimpleDateFormat csvFormatterDate,csvFormatterTime, fileDate, fileTime;
	static String csvFormattedDate, csvFormattedTime, formatFileDate, formatFileTime;
	
	static int counter = 0;
	
	static boolean fileCreated = false;
    BroadcastReceiver batteryReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);//BATTERY CHARGE
			scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);//SCALE OF FULL BATTERY CHARGE
			temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);//BATTERY TEMPERATURE
			voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);//BATTERY VOLTAGE
			Log.d(TAG, "Battery level is "+level+"/"+scale+", temp is "+temp+", voltage is "+voltage);
			Log.d(BINFO, "Battery level is "+level+"/"+scale+", temp is "+temp+", voltage is "+voltage);     

			try {

				timestamp = new Date();
				csvFormattedDate = csvFormatterDate.format(timestamp);
				csvFormattedTime = csvFormatterTime.format(timestamp);

				out.newLine();
				Log.d(FILE,"Wrote battery info");
				out.append(csvFormattedDate +","+ csvFormattedTime +","+ Integer.toString(level) +","+ Integer.toString(voltage)+"," + counter);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	};
	
    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // show the icon in the status bar
        showNotification();
        Log.d(TAG, "Hello from MyService");
        Log.d(LOCATION,"Hello from MyService");
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        
        if(!fileCreated){
    	csvFormatterDate = new SimpleDateFormat("MM-dd-yyyy");  //formatter for CSV timestamp field
  		csvFormatterTime = new SimpleDateFormat("HH:mm:ss");
  		
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
  		
  		 try {  

			  timestamp = new Date();

				fileDate = new SimpleDateFormat("MM-dd-yyyy");
		  		fileTime = new SimpleDateFormat("HH-mm-ss");
		  		
		  		
		  		formatFileDate = fileDate.format(timestamp);
		  		formatFileTime = fileTime.format(timestamp);
				// check for SDcard   
				root = Environment.getExternalStorageDirectory();  


				Log.i("Writter","path.." +root.getAbsolutePath());  


				//check sdcard permission  
				if (root.canWrite()){ 

					fileDir = new File(root.getAbsolutePath()+"/battery_data/");  
					fileDir.mkdirs();  
	
					file= new File(fileDir, formatFileDate +"_interval"+ "-"+ interval+"_"+ formatFileTime + ".csv");  
					filewriter = new FileWriter(file);  
					out = new BufferedWriter(filewriter);

					out.write("Date" +","+ "Time" +","+ "BatteryLevel(0-100)" +","+"Voltage"+","+ "Sample#");  
					Log.d(FILE,"File Created and Titled");  

				}  
			} catch (IOException e) {  
				Log.e("ERROR:---", "Could not write file to SDCard" + e.getMessage());  
			}  
  		 
  		 fileCreated= true;
        }
  		 
        Log.d(TAG, "Thread start in service");
   
        Thread thr = new Thread(null, mTask, "AlarmService_Service");
        thr.start();
        
        
        
       
    }

    @Override
    public void onDestroy() {
        // Cancel the notification -- we use the same ID that we had used to start it
    	Log.d(TAG, "BATTERY SENSOR OFF");
    	Log.d(SENSOR, "Sensor OFF");
    	 unregisterReceiver(batteryReceiver);
		    
//			
			
        mNM.cancel(R.string.alarm_service_started);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.alarm_service_finished , Toast.LENGTH_SHORT).show();
    }

    /**
     * The function that runs in our worker thread
     */
    Runnable mTask = new Runnable() {
        public void run() {
            // Normally we would do some work here...  for our sample, we will
            // just sleep for 30 seconds.
            long endTime = System.currentTimeMillis() + duration*1000;
            Log.d(TAG, "Sensor ON");
            Log.d(SENSOR, "Sensor ON");
            //register sensor listener here
            mSensorManager.registerListener(MyService.this, mAccelerometer,
        			SensorManager.SENSOR_DELAY_FASTEST);
            Log.d(TAG, "BATTERY SENSOR ON");
            Log.d(BINFO, "BATTERY SENSOR ON");
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			registerReceiver(batteryReceiver, filter);
            while (System.currentTimeMillis() < endTime) {
            	
                synchronized (mBinder) {
                    try {
                        mBinder.wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    	Log.e(TAG, "Exception calling wait on binder: " + e);
                    }
                }
            }
            
            //Deregister listener here
            Log.d(TAG, "Sensor OFF");
            Log.d(SENSOR,"Sensor OFF");
            mSensorManager.unregisterListener(MyService.this, mAccelerometer );           
            ++counter;
            Log.d("TAG", "counter="+counter);
            Log.d(SAMPLES,"Samples="+counter);
            // Done with our work...  stop the service!
            MyService.this.stopSelf();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.alarm_service_started);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.stat_sample, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, ServiceMain.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.alarm_service_label),
                       text, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNM.notify(R.string.alarm_service_started, notification);
    }

    /**
     * This is the object that receives interactions from clients.  See RemoteService
     * for a more complete example.
     */
    private final IBinder mBinder = new Binder() {
        @Override
		protected boolean onTransact(int code, Parcel data, Parcel reply,
		        int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };

	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public void onSensorChanged(SensorEvent event) {
		//if(event.sensor.getName() == SensorManager.SENSOR_ACCELEROMETER){
			Log.d(TAG, "x=" + event.values[0] + ", y=" + event.values[1] +", z=" + event.values[2]);
		//}
	}
	
	
}

