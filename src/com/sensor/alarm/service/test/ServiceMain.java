/**
--
Application Title: ServiceMain
Date: August 1, 2012
By: Francisco J. Perez Laras
University of Puerto Rico Bayamon Campus
Contact: fplaras@gmail.com
Description: This application turns on and off the accelerometer sensor.
The sensor is duty cycled at a user defined time. When the benchmark is running
the battery change will be recorded. All notifications are commented.
--
 **/

package com.sensor.alarm.service.test;

import java.io.IOException;

import com.example.service.test.R;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This demonstrates how you can schedule an alarm that causes a service to
 * be started.  This is useful when you want to schedule alarms that initiate
 * long-running operations, such as retrieving recent e-mails.
 */
public class ServiceMain extends Activity {

	private PendingIntent mAlarmSender;

	//used to edit the text in the interface of the application
	EditText txtInterval;
	EditText txtDuration;

	//--------------ONCREATE METHOD ----------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Create an IntentSender that will launch our service, to be scheduled
		// with the alarm manager.
		//Parameters: Context, requestCode, Intent(Context, Class, flags)
		mAlarmSender = PendingIntent.getService(ServiceMain.this, 0, 
				new Intent(ServiceMain.this, MyService.class), 0);

		//defines the type of layout that will be used
		setContentView(R.layout.activity_service_main);

		// Watch for button clicks.
		Button button = (Button) findViewById(R.id.btnStart);
		button.setOnClickListener(mStartAlarmListener);
		button = (Button)findViewById(R.id.btnStop);
		button.setOnClickListener(mStopAlarmListener);

		//object that handles the text
		txtInterval = (EditText) findViewById(R.id.txtInterval);
		txtDuration = (EditText) findViewById(R.id.txtDuration);

		//Set default values
		txtInterval.setText("15");
		txtDuration.setText("5");

	}

	//--------------ONLICK LISTENER METHOD FOR THE START BUTTON ----------------
	private OnClickListener mStartAlarmListener = new OnClickListener() {
		public void onClick(View v) {
			// We want the alarm to go off 30 seconds from now.
			long firstTime = SystemClock.elapsedRealtime();

			//assigns value of variables in MyServie class.
			MyService.interval = Integer.parseInt(txtInterval.getText().toString());
			MyService.duration = Integer.parseInt(txtDuration.getText().toString());

			
			// Schedule the alarm
			AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
			am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					firstTime, MyService.interval*1000, mAlarmSender);

			/*// Tell the user about what we did.
            Toast.makeText(ServiceMain.this, R.string.repeating_scheduled,
                    Toast.LENGTH_LONG).show();*/
			
		}
	};

	//--------------ONLICK LISTENER METHOD FOR THE STOP BUTTON ----------------
	private OnClickListener mStopAlarmListener = new OnClickListener() {
		public void onClick(View v) {

			//MyService.counter = 0;

			//when clicked stop variable returns to original value
			MyService.fileCreated = false;

			//unregisterReceiver(MyService.batteryReceiver);
			//Closing the file
			try {

				MyService.out.flush();
				MyService.out.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// And cancel the alarm.
			AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
			am.cancel(mAlarmSender);

			// Tell the user about what we did.
			//  Toast.makeText(ServiceMain.this, R.string.repeating_unscheduled,
			//        Toast.LENGTH_LONG).show();

		}
	};
}
