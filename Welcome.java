/*
 * Author: Wong, Kiu-fung Jacky
 * Summer 2014 
 * Capstone: Cellular Project
 */

package com.example.gsmdatacollector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class Welcome extends Activity{
	
	//have an object of TelephonyManager
	private TelephonyManager tm;
	
	//phone type
	private int phoneType;
	
	private TextView mTextView;
	
	@Override
	protected void onCreate(Bundle bundle){
		
		super.onCreate(bundle);
		setContentView(R.layout.welcome);
		
		//TelephonyManger object
		tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		
		//phone type
		phoneType = tm.getPhoneType();
		
		//check phone type
		switch (phoneType){
		
			case (TelephonyManager.PHONE_TYPE_CDMA):
				Toast.makeText(this, "Data Collector Does Not Support CDMA.", 
						Toast.LENGTH_LONG).show();
				finish();
				return;
				
			case (TelephonyManager.PHONE_TYPE_SIP):
				Toast.makeText(this, "Data Collector Does Not Support SIP.", 
						Toast.LENGTH_LONG).show();
				finish();
				return;
				
			case (TelephonyManager.PHONE_TYPE_NONE):
				Toast.makeText(this, "Please Launch On A GSM Device.", 
						Toast.LENGTH_LONG).show();
				finish();
				return;
				
			case (TelephonyManager.PHONE_TYPE_GSM):
				
				int network = tm.getNetworkType();
				if (network == TelephonyManager.NETWORK_TYPE_GPRS ||
						network == TelephonyManager.NETWORK_TYPE_EDGE){
					
					String Provider = tm.getNetworkOperatorName();
					String IMSI = tm.getSubscriberId();
					//String IMEI = tm.getDeviceId();
				    mTextView = (TextView) findViewById(R.id.tv);
				    mTextView.setText("Current Network: " + Provider + 
				    		//"\nIMEI: " + IMEI 
				    		"\n" + "IMSI: "+ IMSI + "\n");
				    
				    Button start = (Button)findViewById(R.id.start);
				    start.setOnClickListener(new OnClickListener(){
				    	
				    	public void onClick(View arg0){
				    		startActivity(new Intent(Welcome.this, DataCollector.class));
				    	}
				    });
				    
				    Button exit = (Button)findViewById(R.id.exit);
				    exit.setOnClickListener(new OnClickListener(){
				    	
				    	public void onClick(View arg0){
				    		finish();
				    		return;
				    	}
				    });
				
				}else{
					startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
					Toast.makeText(this, "Please make sure you're on 2G mode.", 
							Toast.LENGTH_LONG).show();
					finish();
					return;
				}
		}	
	}
    
    @Override
    public void onResume() {
        super.onResume();
    }
    
    @Override
    public void onPause() {
        super.onPause();
    }
}
