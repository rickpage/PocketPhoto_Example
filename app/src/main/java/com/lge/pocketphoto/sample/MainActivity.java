package com.lge.pocketphoto.sample;

import com.lge.pocketphoto.bluetooth.*;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private BluetoothFileTransfer mBluetoothFileTransfer = null;
	private PrintProgressAsync mProgress;
	
	public static final int BLUETOOTH_RESPONSE_TARGET_BUSY = 1;
	
	// Device Error - Paper Jam
	public static final int BLUETOOTH_RESPONSE_TARGET_JAM = 2;
	
	// Device Error -Paper Empty 
	public static final int BLUETOOTH_RESPONSE_TARGET_EMPTY = 3;
	
	// Device Error - Wrong Paper
	public static final int BLUETOOTH_RESPONSE_TARGET_WRONG_PAPER = 4;
	
	// Device Error - Data Error  
	public static final int BLUETOOTH_RESPONSE_TARGET_DATA_ERROR = 5;
	
	// Device Error - Cover Opened
	public static final int BLUETOOTH_RESPONSE_TARGET_COVER_OPEN = 6;
	
	// Device Error - System Error
	public static final int BLUETOOTH_RESPONSE_TARGET_SYSTEM_ERROR = 7;
	
	// Device Error - Low Battery
	public static final int BLUETOOTH_RESPONSE_TARGET_BATTERY_LOW = 8;
	
	// Device Error - High Temperature
	public static final int BLUETOOTH_RESPONSE_TARGET_HIGH_TEMPERATURE = 10;
	
	// Device Error - Low Temperature
	public static final int BLUETOOTH_RESPONSE_TARGET_LOW_TEMPERATURE = 11;
	
	// Device Error - Cooling Mode
	public static final int BLUETOOTH_RESPONSE_TARGET_COOLING_MODE = 22;

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//	Button for selection image to print
		Button mButton=(Button)findViewById(R.id.button1);
		mButton.setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("image/*");
						startActivityForResult(intent, 0);
					}
				});
	}

	//	After select image to print, this method is called
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		switch(0)
		{
			case 0:
				try {
					Uri imgUri = data.getData();
					mBluetoothFileTransfer = new BluetoothFileTransfer(this, null, imgUri,
                            mHandler);
					ProgressBar progress = (ProgressBar)this.findViewById(R.id.idProgressBar);
					mProgress = new PrintProgressAsync(progress, 
							null);
					mProgress.execute();
					mBluetoothFileTransfer.checkBluetooth();
					} 
				catch (Exception e) {
					e.printStackTrace();
        			}
				break;
		}
	}
	
	void sendFailState(int error)
	{
		String errStr = null;

		switch(error)
		{

		case MainActivity.BLUETOOTH_RESPONSE_TARGET_BUSY:
			errStr = "BUSY";
			break;

		case MainActivity.BLUETOOTH_RESPONSE_TARGET_JAM:
			errStr = "DATA ERROR";
			break;

		case MainActivity.BLUETOOTH_RESPONSE_TARGET_EMPTY:
			errStr = "PAPER EMPTY";
			break;

		case MainActivity.BLUETOOTH_RESPONSE_TARGET_WRONG_PAPER:
			errStr = "PAPER MISMATCH";
			break;		

		case MainActivity.BLUETOOTH_RESPONSE_TARGET_DATA_ERROR:
			errStr = "DATA ERROR";
			break;

		case MainActivity.BLUETOOTH_RESPONSE_TARGET_COVER_OPEN:
			errStr = "COVER OPEN";
			break;			

		case MainActivity.BLUETOOTH_RESPONSE_TARGET_SYSTEM_ERROR:
			errStr = "SYSTEM ERROR";
			break;					

		case MainActivity.BLUETOOTH_RESPONSE_TARGET_BATTERY_LOW:
			errStr = "BATTERY LOW";
			break;

		case MainActivity.BLUETOOTH_RESPONSE_TARGET_HIGH_TEMPERATURE:
			errStr = "HIGH TEMPERATURE";
			break;

		case MainActivity.BLUETOOTH_RESPONSE_TARGET_LOW_TEMPERATURE:
			errStr = "LOW TEMPERATURE";
			break;
 
		case MainActivity.BLUETOOTH_RESPONSE_TARGET_COOLING_MODE:
			errStr = "HIGH TEMPERATURE";
			break;
		
		}
		
		Toast.makeText(MainActivity.this, "PocketPhoto Error state : "+errStr, Toast.LENGTH_LONG).show(); 
	}
	
	private Handler mHandler = new Handler() 
	{
		@Override
		public void handleMessage(Message msg) 
		{
			switch (msg.what)
			{
			case Opptransfer.BLUETOOTH_SOCKET_CONNECTED:
				break;

			case Opptransfer.BLUETOOTH_SOCKET_CONNECT_FAIL:
				// Connect Fail -> Try to find new device
				if (mProgress!=null) mProgress.setProgress(0);
				Toast.makeText(MainActivity.this, "Cannot connect to paired device.", Toast.LENGTH_LONG).show(); 
				mBluetoothFileTransfer.startDiscovery();
				break;				

			// Cancel while transfer image data via Bluetooth or Receive device error
			case Opptransfer.BLUETOOTH_SEND_FAIL:			
				sendFailState((int)msg.arg1);
				mBluetoothFileTransfer = null;
				if (mProgress!=null) mProgress.setProgress(0);
				break;
			
			// Sending image data via Bluetooth
			case Opptransfer.BLUETOOTH_SEND_PACKET:			
				int per = (int) ((msg.arg1 / (float) msg.arg2) * 100);
				if (mProgress!=null) mProgress.setProgress(per);				
				break;

			// Complete to send image data
			case Opptransfer.BLUETOOTH_SEND_COMPLETE:
				mBluetoothFileTransfer = null;
				Toast.makeText(MainActivity.this, "Send Complete", Toast.LENGTH_LONG).show(); 
				break;
			}
		}
			
	};

}
