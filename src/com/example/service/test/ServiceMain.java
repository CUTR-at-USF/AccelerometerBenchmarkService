package com.example.service.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ServiceMain extends Activity implements OnClickListener {
  private static final String TAG = "ServicesDemo";
  Button buttonStart, buttonStop;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_service_main);

    buttonStart = (Button) findViewById(R.id.btnStart);
    buttonStop = (Button) findViewById(R.id.btnStop);

    buttonStart.setOnClickListener(this);
    buttonStop.setOnClickListener(this);
  }

  public void onClick(View src) {
    switch (src.getId()) {
    case R.id.btnStart:
      Log.d(TAG, "onClick: starting srvice");
      startService(new Intent(this, MyService.class));
      break;
    case R.id.btnStop:
      Log.d(TAG, "onClick: stopping srvice");
      stopService(new Intent(this, MyService.class));
      break;
    }
  }
  
}
