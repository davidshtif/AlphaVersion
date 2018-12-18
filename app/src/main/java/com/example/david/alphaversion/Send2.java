package com.example.david.alphaversion;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Send2 extends AppCompatActivity {

    EditText pNum,pMsg;
    String sNum,sMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send2);

        pNum=(EditText)findViewById(R.id.phone);
        pMsg=(EditText)findViewById(R.id.text);
    }

    public void send(View view) {
        sNum=pNum.getText().toString();
        sMsg=pMsg.getText().toString();
        if(sNum.equals(""))
            Toast.makeText(this,"You didn't enter a phone number",Toast.LENGTH_SHORT).show();
        else if(sMsg.equals(""))
                Toast.makeText(this,"You didn't enter a message",Toast.LENGTH_SHORT).show();
            else{
                SmsManager smsManager=SmsManager.getDefault();
                smsManager.sendTextMessage(sNum,null,sMsg,null,null);
        }
    }

    public void back(View view) {
        finish();
    }
}
