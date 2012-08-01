/**
--
Application Title: MyService
Date: August 1, 2012
By: Francisco J. Perez Laras
University of Puerto Rico Bayamon Campus
Contact: fplaras@gmail.com
Description: Service class from this application turns on and off the accelerometer sensor.
The sensor is duty cycled at a user defined time. When the benchmark is running
the battery change will be recorded. All notifications are commented.
Acknowledgements:
Sean J Barbeau from Center for Urban Transportation Research for the mentorship
University of South Florida - Department of Computer Science and Engineering for the unique opportunity
National Science Foundation for the internship offering
Sprint for the mobile device
--
**/

package com.sensor.alarm.service.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

/**
 * This is an example of implementing an application service that will run in
 * response to an alarm, allowing us to move long duration work out of an
 * intent receiver.
 * 
 * @see AlarmService
 * @see AlarmService_Alarm
 */
public class MyService extends Service implements SensorEventListener {
	
    //NotificationManager mNM;
       
    public static int interval = 15; //seconds
    public static int duration = 5; //seconds
    
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
	
	static SimpleDateFormat csvFormatterDate,csvFormatterTime, fileDate, fileTime, sensorTime;
	static String csvFormattedDate, csvFormattedTime, formatFileDate, formatFileTime,sensorFormatTime;
	
	static int counter = 0;
	
	static boolean fileCreated = false;
	
	static IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
	
	//--Battery data--
    BroadcastReceiver batteryReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			//variables returned from the BatteryManager
			level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);//BATTERY CHARGE
			scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);//SCALE OF FULL BATTERY CHARGE
			temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);//BATTERY TEMPERATURE
			voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);//BATTERY VOLTAGE
			
			try {

				timestamp = new Date();
				csvFormattedDate = csvFormatterDate.format(timestamp);
				csvFormattedTime = csvFormatterTime.format(timestamp);

				out.newLine();
				
				out.append(csvFormattedDate +","+ csvFormattedTime +","+ Integer.toString(level) +","+ Integer.toString(temp)+","+ Integer.toString(voltage));

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	};
	
	//--ONCREATE
    @Override
    public void onCreate() {
        //mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

       
        
        // show the icon in the status bar
        //showNotification();
		
       mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        
      
       if(!fileCreated){
    	   
    	csvFormatterDate = new SimpleDateFormat("MM-dd-yyyy");  //formatter for CSV timestamp field
  		csvFormatterTime = new SimpleDateFormat("HH:mm:ss");
  		sensorTime = new SimpleDateFormat("H:m:s:S");
  		
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
					//file name
					file= new File(fileDir, formatFileDate +"-interval"+ "_"+ MyService.interval+"-"+ formatFileTime + ".csv");  
					filewriter = new FileWriter(file);  
					out = new BufferedWriter(filewriter);
					//cell title
					out.write("Date"+","+"Time"+","+"battery level"+","+"temperature"+","+"Voltage");  
					

				}  
			} catch (IOException e) {  
				Log.e("ERROR:---", "Could not write file to SDCard" + e.getMessage());  
			}  
  		 
  		 fileCreated= true;
        }

       IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryReceiver, filter);
       	//start the thread
        Thread thr = new Thread(null, mTask, "Service Benchmark");
        thr.start();
       
    }

    //--ONDESTROY
    @Override
    public void onDestroy() {
        //Cancel the notification -- we use the same ID that we had used to start it
    
    	
    	 //unregisterReceiver(batteryReceiver);
    	 
			
			
       // mNM.cancel(R.string.alarm_service_started);

        // Tell the user we stopped.
       // Toast.makeText(this, R.string.alarm_service_finished , Toast.LENGTH_SHORT).show();
    	 
    }

    /**
     * The function that runs in our worker thread
     */
    Runnable mTask = new Runnable() {
    	
        public void run() {
        	
            // Normally we would do some work here...  for our sample, we will
            // just sleep for 30 seconds.
        	//sensor will be awake
            long endTime = System.currentTimeMillis() + duration*1000;
  
            //register sensor listener here
           
         mSensorManager.registerListener(MyService.this, mAccelerometer,
        SensorManager.SENSOR_DELAY_FASTEST);    
    			
            while (System.currentTimeMillis() < endTime) {
            	
                synchronized (mBinder) {
                    try {
                        mBinder.wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    	
                    }
                }
            }
            
            //Deregister listener here
           
            mSensorManager.unregisterListener(MyService.this, mAccelerometer ); 
            
           
          //  ++counter;
           
			
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
  /*  private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        //CharSequence text = getText(R.string.alarm_service_started);

        // Set the icon, scrolling text and timestamp
       // Notification notification = new Notification(R.drawable.stat_sample, text,
         //       System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
       // PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
         //       new Intent(this, ServiceMain.class), 0);

        // Set the info for the views that show in the notification panel.
       /// notification.setLatestEventInfo(this, getText(R.string.alarm_service_label),
         //              text, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
       // mNM.notify(R.string.alarm_service_started, notification);
    }
*/
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
		
		/*if(event.sensor.getName()==mAccelerometer.getName()){
		    try {
	        	 
		    	timestamp = new Date();
		    	sensorFormatTime = sensorTime.format(timestamp);
 
 				out.newLine();
 				//Log.d(FILE,"Wrote battery info");
 				out.append(String.valueOf(sensorFormatTime+","+mAccelerometer.getPower()));
 
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
		}*/
	}
	
	
}

