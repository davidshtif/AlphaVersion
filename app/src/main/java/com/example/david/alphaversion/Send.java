package com.example.david.alphaversion;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Send extends AppCompatActivity {

    EditText email,sub,msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        email=(EditText)findViewById(R.id.email);
        sub=(EditText)findViewById(R.id.sub);
        msg=(EditText)findViewById(R.id.msg);
    }

    public void send(View view) {
        String fromEmail="shtifdavid456@gmail.com";
        String password="0528562537";
        String toEmail=email.getText().toString();
        String subject=sub.getText().toString();
        String message=msg.getText().toString();
        if(toEmail.isEmpty()){
            Toast.makeText(this,"You didn't enter an email",Toast.LENGTH_SHORT).show();
        } else if(subject.isEmpty()){
                Toast.makeText(this,"You didn't enter a subject",Toast.LENGTH_SHORT).show();
            } else if(message.isEmpty()){
                    Toast.makeText(this,"You didn't enter a message",Toast.LENGTH_SHORT).show();
                    } else{
                        new SendMailTask(Send.this).execute(fromEmail, password,toEmail,subject,message);
                        }

    }

    public void back(View view) {
        finish();
    }
}
