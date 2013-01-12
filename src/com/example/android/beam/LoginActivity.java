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

public class LoginActivity extends Activity {
	
	Thread t;
	private SharedPreferences mPreferences;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        
        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
        } else {
            mNfcAdapter.setNdefPushMessageCallback(null, this);
            mNfcAdapter.setOnNdefPushCompleteCallback(null, this);
        }
    }
    
    public void launchSignUp(View view)
    {
    	Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
    	startActivity(intent);
    }
    
    public void processLogIn(View view)
    {
    	t = new Thread() {
			public void run() {
				Looper.prepare();
				if(logIn())
				{
			    	Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
			    	startActivity(intent);
				}
				else
				{
					Toast.makeText(getApplicationContext(), "Sign in failed. Please verify username and password.", Toast.LENGTH_LONG).show();
				}
				Looper.loop();
			}
		};
		t.start();
    }
    
    public boolean logIn()
    {
		try
		{
	    	EditText mEmailField = (EditText) findViewById(R.id.email);
			EditText mPasswordField = (EditText) findViewById(R.id.password);
	 
			String email = mEmailField.getText().toString();
			String password = mPasswordField.getText().toString();
	 
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(getString(R.string.api_base)+"/sessions");
			
			List<NameValuePair> params = new ArrayList<NameValuePair>(2);
			params.add(new BasicNameValuePair("user[email]", mEmailField.getText().toString()));
			params.add(new BasicNameValuePair("user[password]", mPasswordField.getText().toString()));
			
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			post.setHeader("Accept", "application/json");
			
			String response = null;
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			response = client.execute(post, responseHandler);
			
			JSONObject jObject = new JSONObject(response);
			JSONObject sessionObject = jObject.getJSONObject("session");
			
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putString("auth_token", sessionObject.getString("authentication_token"));
			editor.putString("email", sessionObject.getString("email"));
			editor.putString("name", sessionObject.getString("name"));
			editor.putInt("balance", sessionObject.getInt("balance"));
			editor.commit();
			
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
    }

}