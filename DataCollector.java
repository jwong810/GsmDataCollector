/*
 * Author: Wong, Kiu-fung Jacky
 * Summer 2014 
 * Capstone: Cellular Project
 */

package com.example.gsmdatacollector;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.*;
import android.telephony.gsm.GsmCellLocation;

public class DataCollector extends Activity implements Runnable{

	private int entryCount = 0;
	private List<String[]> data = new ArrayList<String[]>();
	
	private TelephonyManager tm;
	private CellInfoGsm cellInfoGsm;
	private Calendar cal;
	private String DATE_FORMAT_NOW = "yyyyMMdd HH:mm:ss";
	private GsmCellLocation loc;
	private CellSignalStrengthGsm cellSignalStrengthGsm;
	private CellIdentityGsm cellIdentity;
	
	private SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	
	private Thread mythread = new Thread(this);
	
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.datacollector);
		
		Button finish = (Button)findViewById(R.id.finish);
		
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		
		//create header for csv
		createHeader();

		//start data collection in a new thread
		mythread.start();
		
		finish.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0){
				
				//interrupt thread if finish is pressed
				mythread.interrupt();
				
				try {
					//generate file
					createFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				//finish activity, returns to welcome screen
				finish();
			}
		});
	}

    Handler mHandler = new Handler();
    @Override                    
    public void run(){   
    	
         final TextView tv1 = (TextView) findViewById(R.id.tv);
         
         while(!mythread.isInterrupted()){   
        	 
            try {
                mHandler.post(new Runnable(){
                   @Override
                   public void run(){
                	   //this updates the TextView and poll data every second
                      collectData(tv1);
                   }
                }); 
                
                //sleep for one second
                Thread.sleep(1000);              
            }catch (Exception e) {}           
         } 
    }   
	
    
    //collectData function, changes and displays # of entry counts
    private void collectData(TextView tv1){
    	
    	long startTime = System.currentTimeMillis();
        cal = Calendar.getInstance();
        
        cellInfoGsm = (CellInfoGsm)tm.getAllCellInfo().get(0);
        
        //get signal Strength
        cellSignalStrengthGsm = cellInfoGsm.getCellSignalStrength();
        cellIdentity = cellInfoGsm.getCellIdentity();
        
        //getCellLocation will not work on LTE connection
        loc = (GsmCellLocation) tm.getCellLocation();
        
        //the numbers to collect
        int cid = cellIdentity.getCid();
        int lac = cellIdentity.getLac();
        int mcc = cellIdentity.getMcc();
        int mnc = cellIdentity.getMnc();
        int psc = loc.getPsc();
        int dbm = cellSignalStrengthGsm.getDbm();
        int signalLv = cellSignalStrengthGsm.getLevel();
        int asu = cellSignalStrengthGsm.getAsuLevel();
        String groupId = tm.getGroupIdLevel1();
        String softVer = tm.getDeviceSoftwareVersion();
        String mmsUrl = tm.getMmsUAProfUrl();
        String mmsAgent = tm.getMmsUserAgent();
        long timeStamp = cellInfoGsm.getTimeStamp();
        boolean roaming = tm.isNetworkRoaming();
        
        //check if connection is on 2G, 3G, or 4G
        int network = tm.getNetworkType();
        String networkType = null;
        
        switch (network){ 
        	case (TelephonyManager.NETWORK_TYPE_GPRS):
        		networkType = "GPRS"; //2.5G
        		break;
        
        	case (TelephonyManager.NETWORK_TYPE_EDGE):
        		networkType = "EDGE"; //2.75G
        		break;
        }
        
        String callState = null;
        int callStateVal = tm.getCallState();
        
        switch (callStateVal){
        	case (TelephonyManager.CALL_STATE_IDLE):
        		callState = "Idle";
        		break;
   	
        	case (TelephonyManager.CALL_STATE_RINGING):
        		callState = "Ringing";
        		break;
        	
        	case (TelephonyManager.CALL_STATE_OFFHOOK):
        		callState = "Off-hook";
        		break;
        }
        
        String dataActivity = null;
        int dataActVal = tm.getDataActivity();
        
        switch (dataActVal){
        	case (TelephonyManager.DATA_ACTIVITY_NONE):
        		dataActivity = "None";
        		break;
        	
        	case (TelephonyManager.DATA_ACTIVITY_IN):
        		dataActivity = "Receiving";
        		break;
        	
        	case (TelephonyManager.DATA_ACTIVITY_OUT):
        		dataActivity = "Sending";
        		break;
        		
        	case(TelephonyManager.DATA_ACTIVITY_INOUT):
        		dataActivity = "Receiving & Sending";
        		break;
        	
        	case (TelephonyManager.DATA_ACTIVITY_DORMANT):
        		dataActivity = "Dormant";
        		break;
        }
        
        String dataState = null;
        int dataStateVal = tm.getDataState();
        
        switch (dataStateVal){
        	case (TelephonyManager.DATA_DISCONNECTED):
        		dataState = "Disconnected";
        		break;
        	
        	case (TelephonyManager.DATA_CONNECTING):
        		dataState = "Connecting";
        		break;
        	
        	case(TelephonyManager.DATA_CONNECTED):
        		dataState = "Connected";
        		break;
        	
        	case (TelephonyManager.DATA_SUSPENDED):
        		dataState = "Suspended";
        		break;
        }
        
        StringBuffer temp1 = null, temp2 = null;
             
        //add data to String list
        data.add(new String[]{sdf.format(cal.getTime()),
        		Integer.toString(cid),
    			Integer.toString(lac),
    			Integer.toString(mcc),
    			Integer.toString(mnc),
    			Integer.toString(psc),
    			Integer.toString(dbm),
    			Integer.toString(signalLv),
    			Integer.toString(asu),
    			callState,
    			dataActivity,
    			dataState,
    			networkType,
    			groupId, 
    			softVer, 
    			mmsUrl, 
    			mmsAgent, 
    			
    			//approximate time of this cell info in nanos since boot
    			Long.toString(timeStamp),
    			String.valueOf(roaming)});
        
        //increment entry count, then displays it on screen
        entryCount++;  
    	String string = "Number of times collected: " + entryCount;
    	
    	long endTime = System.currentTimeMillis();
    	long totalTime = endTime - startTime;
    	
    	tv1.setText(string + "\n" + totalTime + "\n" + Integer.toString(cellIdentity.getCid()) +
    			" " + Integer.toString(cellIdentity.getLac()) +
    			" " + Integer.toString(cellIdentity.getMcc()) +
    			" " + Integer.toString(cellIdentity.getMnc()) +
    			" " + Integer.toString(loc.getPsc()) + 
    			" " + cellSignalStrengthGsm.getDbm() +
    			" " + cellSignalStrengthGsm.getLevel() + 
    			" " + cellSignalStrengthGsm.getAsuLevel() +
    			" " + networkType);
    }
    
    //createHeader creates column names for CSV files
    public void createHeader(){
    	data.add(new String[]{"Date/Time", "Cid", "Lac", "Mcc", "Mnc", "Psc", 
    			"Dbm", "Signal Lv.", "Asu", "Call State", "Data Activity",
    			"Data State", "Network Type", "Group ID Lv.1", 
    			"Device Sofware Ver.", 
    			"MMS User Pro. URL", "MMS User Agent", 
    			"Time Stamp", "Roaming?"});
    }
    
    
    //method to generate CSV file
    public void createFile() throws IOException{
    	
    	//file name will be in the following date format
    	String forFile = "yyyyMMddHHmmss";
    	SimpleDateFormat fileDate = new SimpleDateFormat(forFile);
    	Calendar curTime = cal.getInstance();
    	
    	//find and set root path, output file name
    	String rootPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    	String fileName = (fileDate.format(curTime.getTime())) + ".csv";
    	
    	//output data to csv
    	CSVWriter writer = new CSVWriter(new FileWriter(rootPath + "/" + fileName));
    	writer.writeAll(data);
    	writer.close();
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
