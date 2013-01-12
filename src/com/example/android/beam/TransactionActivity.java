package com.example.android.beam;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class TransactionActivity extends Activity {
	
	Thread t;
	private SharedPreferences mPreferences;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction);
        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        
        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
        } else {
            mNfcAdapter.setNdefPushMessageCallback(null, this);
            mNfcAdapter.setOnNdefPushCompleteCallback(null, this);
        }
    }
    
    public void startWaiting(View view)
    {
    	t = new Thread() {
			public void run() {
				Looper.prepare();
				Integer transactionId = createTransaction();
				if(transactionId > 0)
				{
			    	Intent intent = new Intent(getApplicationContext(), WaitingActivity.class);
			    	intent.putExtra("transactionId", transactionId);
			    	startActivity(intent);
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
    
    public int createTransaction()
    {
    	try
		{
	    	EditText mAmountField = (EditText) findViewById(R.id.transactionAmount);
			Integer transactionAmount = (int) (Double.parseDouble(mAmountField.getText().toString()) * 100.0);
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(getString(R.string.api_base)+"/transactions");
			List<NameValuePair> params = new ArrayList<NameValuePair>(3);
			params.add(new BasicNameValuePair("transaction[amount]", transactionAmount.toString()));
			params.add(new BasicNameValuePair("auth_token", mPreferences.getString("auth_token", "")));
			params.add(new BasicNameValuePair("email", mPreferences.getString("email", "")));
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			post.setHeader("Accept", "application/json");
			String response = null;
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			response = client.execute(post, responseHandler);

			JSONObject jObject = new JSONObject(response);
			JSONObject transactionObject = jObject.getJSONObject("transaction");
			
			return transactionObject.getInt("id");
		}
		catch(Exception e)
		{
//			e.printStackTrace();
			return -1;
		}
    }
    
    public void quit(View view)
    {
    	finish();
    }

}