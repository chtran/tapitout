package com.example.android.beam;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;

public class MenuActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        
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
