package com.example.android.beam;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MenuActivity extends Activity {
	private SharedPreferences mPreferences;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        TextView name = (TextView) findViewById(R.id.name);
        TextView balance = (TextView) findViewById(R.id.balance);
        name.setText("Hello " + mPreferences.getString("name", "Anonymous")+"!");
        balance.setText("Balance: $"+ (mPreferences.getInt("balance", 0) / 100));
        
        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
        } else {
            mNfcAdapter.setNdefPushMessageCallback(null, this);
            mNfcAdapter.setOnNdefPushCompleteCallback(null, this);
        }
    }
    
    public void startTransaction(View view)
    {
    	Intent intent = new Intent(getApplicationContext(), TransactionActivity.class);
    	startActivity(intent);
    }
    
    public void startAddCard(View view)
    {
    	
    }
    
    public void quit(View view)
    {
    	finish();
    }
}
