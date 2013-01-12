package com.example.android.beam;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.text.format.Time;
import android.view.View;
import android.widget.Toast;

public class WaitingActivity extends Activity implements CreateNdefMessageCallback, OnNdefPushCompleteCallback {

	NfcAdapter mNfcAdapter;
	private static final int MESSAGE_SENT = 1;
	private Integer transactionId;
	Thread t;
	private SharedPreferences mPreferences;
	NumberFormat nf;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.waiting);
		
//		ImageView tapitImage = (ImageView) findViewById(R.id.selected);
//		tapitImage.setBackgroundResource(R.drawable.tap_animation);
//		
//		AnimationDrawable tapitAnimation = (AnimationDrawable) tapitImage.getBackground();
//		tapitAnimation.start();
		
		Intent intent = getIntent();
		transactionId = intent.getIntExtra("transactionId", 0);
		mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);

		nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
		} else {
			mNfcAdapter.setNdefPushMessageCallback(this, this);
			mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
		}
	}
	
	/**
	 * Implementation for the CreateNdefMessageCallback interface
	 */
	@SuppressLint("NewApi")
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		Time time = new Time();
		time.setToNow();

		NdefMessage msg = new NdefMessage(NdefRecord.createMime("application/com.example.android.beam", transactionId.toString().getBytes()));
		return msg;
	}

	/**
	 * Implementation for the OnNdefPushCompleteCallback interface
	 */
	@Override
	public void onNdefPushComplete(NfcEvent arg0) {
		// A handler is needed to send messages to the activity when this
		// callback occurs, because it happens from a binder thread
		mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
	}

	/** This handler receives a message from onNdefPushComplete */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_SENT:
				Toast.makeText(getApplicationContext(), "Sending money...", Toast.LENGTH_LONG).show();
				
				t = new Thread() {
					public void run() {

						Looper.prepare();
						while(true)
						{
							Pair<Integer, String> transactionDetails = checkTransaction();
							System.out.println(transactionDetails.first());
							System.out.println(transactionDetails.second());
							if(!transactionDetails.second().equals("null") && !transactionDetails.second().equals(""))
							{
								class RunnableWithPair implements Runnable {
									Pair<Integer, String> transactionDetails;
									
									public RunnableWithPair(Pair<Integer, String> dets)
									{
										transactionDetails = dets;
									}

									public void run() {
									   new AlertDialog.Builder(WaitingActivity.this)
										.setIcon(android.R.drawable.ic_dialog_alert)
										.setTitle("Send money?")
										.setMessage("Are you sure that you want to send $" + nf.format(transactionDetails.first() / 100.0) + " to " + transactionDetails.second() + "?")
										.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											Thread t2 = new Thread() {
												public void run() {

													Looper.prepare();
													Integer transactionStatus = setTransactionStatus(1);

													if(transactionStatus == 3)
													{
														Toast.makeText(getApplicationContext(), "You don't have enough in your account!", Toast.LENGTH_LONG).show();
													}
													else if(transactionStatus == 1)
													{
														Toast.makeText(getApplicationContext(), "Money sent!", Toast.LENGTH_LONG).show();
													}
													else
													{
														Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_LONG).show();
													}
													
													Looper.loop();
												}
											};
											t2.start();
										}

									})
									.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) { 
											Thread t2 = new Thread() {
												public void run() {

													Looper.prepare();
													Integer transactionStatus = setTransactionStatus(1);

													if(transactionStatus == 2)
													{
														Toast.makeText(getApplicationContext(), "Transaction cancelled!", Toast.LENGTH_LONG).show();
													}
													else
													{
														Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_LONG).show();
													}
													
													Looper.loop();
												}
											};
											t2.start();
										}
									}).show();
								}};
									
								runOnUiThread(new RunnableWithPair(transactionDetails));
								
								return;
							}
							else
							{
								System.out.println("still waiting 1");
							}
							System.out.println("testing after waiting");
							Looper.loop();
						}
					}
				};
				t.start();
				
				break;
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		// Check to see that the Activity started due to an Android Beam
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			processIntent(getIntent());
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		// onResume gets called after this to handle the intent
		setIntent(intent);
	}

	/**
	 * Parses the NDEF Message from the intent and prints to the TextView
	 */
	void processIntent(Intent intent) {
		Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		// only one message sent during the beam
		NdefMessage msg = (NdefMessage) rawMsgs[0];
		
		transactionId = Integer.parseInt(new String(msg.getRecords()[0].getPayload()));
		t = new Thread() {
			public void run() {
				Looper.prepare();
				Pair<Integer, String> transactionDetails = confirmTransaction();
				if(transactionDetails.first() > 0)
				{
					Toast.makeText(getApplicationContext(), transactionDetails.second() + " is sending you $" + nf.format(transactionDetails.first() / 100.0) + "...", Toast.LENGTH_LONG).show();

					// Thread t2 = new Thread() {
					// 	public void run() {

					// 		Looper.prepare();
							while(true)
							{
								Integer transactionStatus = checkTransactionStatus();
								if(transactionStatus != 0)
								{
									System.out.println(transactionStatus + " == TRANSACTION_STATUS");
									String toastText;
									if(transactionStatus == 1)
									{
										System.out.println("TESTING");
										toastText = "Money received!";
									}
									else if(transactionStatus == 2)
									{
										toastText = "Transaction cancelled!";
									}
									else if(transactionStatus == 3)
									{
										toastText = "The sender does not have enough money.";
									}
									else
									{
										toastText = "Something went wrong...";
									}

									class RunnableWithString implements Runnable {
										String toastText;
										
										public RunnableWithString(String text)
										{
											toastText = text;
										}

										public void run() {
											Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_LONG).show();
										}
									};
									runOnUiThread(new RunnableWithString(toastText));
									
									finish();
								}
								else
								{
									System.out.println("still waiting");
								}
								//View vg = findViewById(R.id.waiting);
								//vg.invalidate();
								
								Looper.loop();
							}
					// 	}
					// };
					// t2.start();
				}
				else
				{
					Toast.makeText(getApplicationContext(), "Transaction failed to create.", Toast.LENGTH_LONG).show();
				}
				Looper.loop();
			}
		};
		t.start();
	}
	
	public Pair<Integer, String> confirmTransaction()
	{
		try
		{
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(getString(R.string.api_base)+"/transactions/" + transactionId + "/receive");
			List<NameValuePair> params = new ArrayList<NameValuePair>(3);
			params.add(new BasicNameValuePair("auth_token", mPreferences.getString("auth_token", "")));
			params.add(new BasicNameValuePair("email", mPreferences.getString("email", "")));
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			post.setHeader("Accept", "application/json");
			String response = null;
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			response = client.execute(post, responseHandler);

			JSONObject jObject = new JSONObject(response);
			JSONObject transactionObject = jObject.getJSONObject("transaction");
			Integer transactionAmount = transactionObject.getInt("amount");
			String transactionSender = transactionObject.getString("sender_name");
			
			return new Pair<Integer, String>(transactionAmount, transactionSender);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new Pair<Integer, String>(-1, "");
		}
	}
	
	public Pair<Integer, String> checkTransaction()
	{
		try
		{
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet post = new HttpGet(getString(R.string.api_base)+"/transactions/" + transactionId + "?auth_token=" + mPreferences.getString("auth_token", ""));
			post.setHeader("Accept", "application/json");
			String response = null;
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			response = client.execute(post, responseHandler);

			JSONObject jObject = new JSONObject(response);
			JSONObject transactionObject = jObject.getJSONObject("transaction");
			Integer transactionAmount = transactionObject.getInt("amount");
			String transactionReceiver = transactionObject.getString("receiver_name");
			
			return new Pair<Integer, String>(transactionAmount, transactionReceiver);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new Pair<Integer, String>(-1, "");
		}	
	}

	public Integer setTransactionStatus(Integer status)
	{
		try
		{
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(getString(R.string.api_base)+"/transactions/" + transactionId + "/confirm");
			List<NameValuePair> params = new ArrayList<NameValuePair>(3);
			params.add(new BasicNameValuePair("auth_token", mPreferences.getString("auth_token", "")));
			params.add(new BasicNameValuePair("email", mPreferences.getString("email", "")));
			params.add(new BasicNameValuePair("transaction[status]", status.toString()));
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			post.setHeader("Accept", "application/json");
			String response = null;
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			response = client.execute(post, responseHandler);

			JSONObject jObject = new JSONObject(response);
			JSONObject transactionObject = jObject.getJSONObject("transaction");
			Integer transactionStatus = transactionObject.getInt("status");
			
			return transactionStatus;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return -1;
		}
	}

	public Integer checkTransactionStatus()
	{
		try
		{
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet post = new HttpGet(getString(R.string.api_base)+"/transactions/" + transactionId + "?auth_token=" + mPreferences.getString("auth_token", ""));
			post.setHeader("Accept", "application/json");
			String response = null;
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			response = client.execute(post, responseHandler);

			JSONObject jObject = new JSONObject(response);
			JSONObject transactionObject = jObject.getJSONObject("transaction");
			Integer transactionStatus = transactionObject.getInt("status");
			
			return transactionStatus;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return -1;
		}
	}
}
