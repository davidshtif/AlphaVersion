package com.example.david.alphaversion;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Picasso;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.example.david.alphaversion.Fridge.CHANNEL_1_ID;

public class Main extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, AdapterView.OnItemSelectedListener {

    EditText et1,et2;
    ListView list;
    String name,weight,enterD,enterT,expired,barcode;
    DatabaseReference ref;
    Button button,scan;
    ImageView image;
    List<Product> productList;
    TextView pick,diff;
    String hours="07",minutes="00",not;
    int days=1;
    boolean isScan,checked=false;
    private AlertDialog.Builder dialogBuilder;
    private View dialogView;
    private TextView expDate;
    private Product currentProduct;
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isScan=false;

        et1=(EditText)findViewById(R.id.editText);
        et2=(EditText)findViewById(R.id.editText2);
        list=(ListView)findViewById(R.id.list);
        image=(ImageView) findViewById(R.id.imageView);
        pick=(TextView)findViewById(R.id.picked);
        diff=(TextView)findViewById(R.id.diff);

        notificationHelper = new NotificationHelper(this);


        productList=new ArrayList<>();


        Thread t1 = new Thread(){
            @Override
            public void run(){
                try{
                    while(!isInterrupted()){
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView time =(TextView) findViewById(R.id.textView7);
                                long date = System.currentTimeMillis();
                                SimpleDateFormat sdfD = new SimpleDateFormat("dd/MM/yyyy");
                                SimpleDateFormat sdfT = new SimpleDateFormat("HH:mm:ss");
                                String dateString = sdfD.format(date);
                                String timeString = sdfT.format(date);
                                time.setText("Current date: "+dateString+"    Current time: "+timeString);
                            }
                        });
                    }
                }catch(InterruptedException e){

                }
            }
        };
        t1.start();


        Thread t2 = new Thread(){
            @Override
            public void run(){
                try{
                    while(!isInterrupted()){
                        long date = System.currentTimeMillis();
                        SimpleDateFormat sdfT = new SimpleDateFormat("HH:mm");
                        String timeString = sdfT.format(date);
                        String hour = timeString.substring(0,2);
                        String minute = timeString.substring(3,5);
                        if(hour.equals(hours)&&minute.equals(minutes)){
                            Thread.sleep(10000);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ArrayAdapter adapter=new ProductList(Main.this,productList);
                                    if(adapter.getCount()!=0){
                                        ref.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (productList != null) {
                                                    productList.clear();

                                                    for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                                                        Product product = productSnapshot.getValue(Product.class);

                                                        if (product.getExpired() != null) {
                                                            String expired = product.getExpired();
                                                            String message;
                                                            String title = "Expired product";

                                                            String dayS = expired.substring(0, 2);
                                                            String monthS = expired.substring(3, 5);
                                                            String yearS = expired.substring(6, 10);
                                                            int day = Integer.parseInt(dayS);
                                                            int month = Integer.parseInt(monthS);
                                                            int year = Integer.parseInt(yearS);

                                                            Calendar thatDay = Calendar.getInstance();
                                                            thatDay.set(Calendar.DAY_OF_MONTH, day);
                                                            thatDay.set(Calendar.MONTH, (month - 1));
                                                            thatDay.set(Calendar.YEAR, year);

                                                            Calendar today = Calendar.getInstance();

                                                            long curDiff = thatDay.getTimeInMillis() - today.getTimeInMillis();
                                                            diff.setText(""+curDiff);
                                                            if (curDiff/8640000 < 0) {
                                                                //send notification and remove from list
                                                                if(!checked){
                                                                    message = "" + product.getName() + " expired";
                                                                    productList.add(product);
                                                                }
                                                                else{
                                                                    message = "" + product.getName() + " expired and was removed from the list";
                                                                    deleteProduct(product.getId());
                                                                }
                                                                NotificationCompat.Builder nb = notificationHelper.getChannel1Notification(title, message);
                                                                notificationHelper.getManager().notify(1, nb.build());
                                                            } else {
                                                                productList.add(product);
                                                                if (curDiff <= (days * 86400000)) {
                                                                    //send notification
                                                                    message = "" + product.getName() + " will expire in " + ((curDiff/86400000)+1) + " day/s";
                                                                    NotificationCompat.Builder nb = notificationHelper.getChannel2Notification(title, message);
                                                                    notificationHelper.getManager().notify(2, nb.build());
                                                                }
                                                            }
                                                        } else {
                                                            productList.add(product);
                                                        }
                                                    }
                                                    onStart();
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }catch(InterruptedException e){

                }
            }
        };
        t2.start();


        et1.setText("");
        et2.setText("");
        pick.setText("");

        ref=FirebaseDatabase.getInstance().getReference("products");

        button=(Button)findViewById(R.id.expired);
        scan=(Button)findViewById(R.id.scan);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker= new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(),"date picker");
            }
        });
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barcode="";
                name="";
                weight="";
                et1.setText("");
                et2.setText("");
                image.setImageResource(R.drawable.no_image);
                if(v.getId()==R.id.scan){
                    IntentIntegrator scanIntegrator = new IntentIntegrator(Main.this);
                    scanIntegrator.initiateScan();
                }
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                currentProduct = productList.get(position);
                showUpdateDialog(currentProduct);
                return false;
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String sDay,sMonth;
        if(dayOfMonth<10){
            sDay = "0" + dayOfMonth;
        }
        else{
            sDay = "" + dayOfMonth;
        }
        if(month<10){
            sMonth = "0" + (month+1);
        }
        else{
            sMonth = "" + (month+1);
        }
        expired = "" + sDay +"/" + sMonth + "/" + year;
        pick.setText("Date picked: "+expired);
        try {
            if(!productList.isEmpty()){
                currentProduct.setExpired(expired);
                expDate.setText("Expiration date: "+currentProduct.getExpired());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();

                for(DataSnapshot productSnapshot: dataSnapshot.getChildren()){
                    Product product=productSnapshot.getValue(Product.class);
                    productList.add(product);
                }
                ArrayAdapter adapter=new ProductList(Main.this,productList);
                list.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showUpdateDialog(final Product product){

        dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater=getLayoutInflater();
        dialogView = inflater.inflate(R.layout.update_dialog,null);
        dialogBuilder.setView(dialogView);
        final EditText editTextName=(EditText) dialogView.findViewById(R.id.up_name);
        final EditText editTextWeight=(EditText) dialogView.findViewById(R.id.up_weight);
        final Button buttonUpdate=(Button) dialogView.findViewById(R.id.up);
        final Button buttonCancel=(Button) dialogView.findViewById(R.id.cancel);
        final Button buttonDelete=(Button) dialogView.findViewById(R.id.delete);
        final Button buttonExp=(Button) dialogView.findViewById(R.id.exp);
        final Button buttonNewP=(Button) dialogView.findViewById(R.id.newPic);
        final ImageView newP=(ImageView) dialogView.findViewById(R.id.pic);

        barcode=product.getBarcode();

        if(product.getBarcode()!=null){
            product.setBarcode(loadImageFromUrl(product.getBarcode(),newP));
        }

        expDate = (TextView) dialogView.findViewById(R.id.expDate);



        if(product.getExpired()!=null){
            expDate.setText("Expiration date: "+product.getExpired());
        }
        else{
            expDate.setText("Expiration date: none");
        }

        dialogBuilder.setTitle("Updating product "+product.getName());
        final AlertDialog alertDialog= dialogBuilder.create();
        alertDialog.show();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                String name=editTextName.getText().toString();
                String sWeight=editTextWeight.getText().toString();
                if(name.isEmpty()&&sWeight.isEmpty()){
                    updateProduct(product.getId(),product.getName(),product.getEnterD(),product.getEnterT(),product.getExpired(),barcode,product.getWeight(),isScan);
                }
                else{
                    if(!name.isEmpty()){
                        updateProduct(product.getId(),name,product.getEnterD(),product.getEnterT(),product.getExpired(),barcode,product.getWeight(),isScan);
                    }
                    else{
                        if(!sWeight.isEmpty()){
                            updateProduct(product.getId(),product.getName(),product.getEnterD(),product.getEnterT(),product.getExpired(),barcode,sWeight,isScan);
                        }
                        else{
                            updateProduct(product.getId(),name,product.getEnterD(),product.getEnterT(),product.getExpired(),barcode,sWeight,isScan);
                        }
                    }
                }
                image.setImageResource(R.drawable.no_image);
                isScan=false;
                barcode=null;
                et1.setText("");
                et2.setText("");
                alertDialog.dismiss();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                barcode=null;
                isScan=false;
                alertDialog.dismiss();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                deleteProduct(product.getId());
                barcode=null;
                isScan=false;
                alertDialog.dismiss();
            }
        });

        buttonExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker= new DatePickerFragment();
                FragmentManager supportFragmentManager = getSupportFragmentManager();
                datePicker.show(supportFragmentManager,"date picker");
                if(datePicker.isHidden()){
                    product.setExpired(expired);
                }
                isScan=true;
                            }
        });

        buttonNewP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId()==R.id.newPic){
                    IntentIntegrator scanIntegrator = new IntentIntegrator(Main.this);
                    scanIntegrator.initiateScan();

                }
            }
        });
    }

    private void deleteProduct(String productId){
        DatabaseReference drProduct=FirebaseDatabase.getInstance().getReference("products").child(productId);
        drProduct.removeValue();
        Toast.makeText(this,"Product deleted",Toast.LENGTH_SHORT).show();
    }

    private boolean updateProduct(String id,String name,String enterD,String enterT,String expired,String barcode,String weight,boolean isScan){
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("products").child(id);
        Product product=new Product(id,name,enterD,enterT,expired,barcode,weight,isScan);
        databaseReference.setValue(product);
        Toast.makeText(this,"Updated Successfully",Toast.LENGTH_SHORT).show();
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu){
        menu.add("Send SMS");
        menu.add("Send Email");
        menu.add("Settings");
        menu.add("Credits");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        String st=item.getTitle().toString();
        Intent email=new Intent(this,Send.class);
        Intent sms=new Intent(this,Send2.class);
        Intent credits=new Intent(this,Credits.class);
        if(st.equals("Send Email")) {
            startActivity(email);
        }
        if(st.equals("Send SMS")){
            startActivity(sms);
        }
        if(st.equals("Credits")){
            startActivity(credits);
        }
        if(st.equals("Settings")){

            dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater=getLayoutInflater();
            dialogView = inflater.inflate(R.layout.settings_dialog,null);
            dialogBuilder.setView(dialogView);

            final Spinner spinner = dialogView.findViewById(R.id.spinner);
            spinner.setSelection(days);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.numbers,android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(this);
            final Button change = dialogView.findViewById(R.id.button4);
            final Button ok = dialogView.findViewById(R.id.button5);
            final CheckBox checkBox = dialogView.findViewById(R.id.checkBox);
            TextView notify = dialogView.findViewById(R.id.textView10);
            notify.setText("I want to get notified on:   "+hours+":"+minutes);

            if(checked){
                checkBox.setChecked(true);
            }

            dialogBuilder.setTitle("Settings");
            final AlertDialog alertDialog= dialogBuilder.create();
            alertDialog.show();

            change.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    DialogFragment timePicker = new TimePickerFragment();
                    timePicker.show(getSupportFragmentManager(), "Time Picker");
                }
            });
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    if(checkBox.isChecked()){
                        checked = true;
                    }
                    else{
                        checked = false;
                    }
                    alertDialog.dismiss();
                }
            });
        }
        return true;
    }

    public void upload(View view) {
        name=et1.getText().toString();
        weight=et2.getText().toString();
        if(isScan) {
            name = setName();
            weight = setWeight();
        }

        Calendar date=Calendar.getInstance();
        Calendar time=Calendar.getInstance();

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        enterD=format.format(date.getTime());
        SimpleDateFormat mdformat=new SimpleDateFormat("HH:mm:ss");
        enterT=mdformat.format(time.getTime());
        if(name.equals(""))
            Toast.makeText(this,"You didn't enter a name or scanned anything",Toast.LENGTH_SHORT).show();
        else if(weight.equals(""))
                Toast.makeText(this,"You didn't enter a weight or scanned anything",Toast.LENGTH_SHORT).show();
            else{
                String id=ref.push().getKey();
                Product product=new Product(id,name,enterD,enterT,expired,barcode,weight,isScan);
                ref.child(id).setValue(product);
                et1.setText("");
                name="";
                et2.setText("");
                weight="";
                barcode=null;
                pick.setText("");
                isScan=false;
                image.setImageResource(R.drawable.no_image);
            }
    }

    private String setWeight() {
        Ion.with(getApplicationContext())
                .load("https://chp.co.il/%D7%91%D7%90%D7%A8%20%D7%A9%D7%91%D7%A2/0/0/"+barcode+"/0")
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        try {
                            result=result.substring(72,result.indexOf("</title>",49));
                            result.substring(0,result.indexOf(","));
                            weight=result.substring(result.indexOf(", ")+2);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });
        return weight;
    }

    private String setName() {
        Ion.with(getApplicationContext())
                .load("https://chp.co.il/%D7%91%D7%90%D7%A8%20%D7%A9%D7%91%D7%A2/0/0/"+barcode+"/0")
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        try {
                            result=result.substring(72,result.indexOf("</title>",49));
                            name=result.substring(0,result.indexOf(","));
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });
        return name;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult.getContents() != null) {
            barcode= scanningResult.getContents();
            barcode= loadImageFromUrl(barcode,image);
            isScan=true;
            Ion.with(getApplicationContext())
                    .load("https://chp.co.il/%D7%91%D7%90%D7%A8%20%D7%A9%D7%91%D7%A2/0/0/"+barcode+"/0")
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            result=result.substring(72,result.indexOf("</title>",49));
                            try {
                                name=result.substring(0,result.indexOf(","));
                                weight=result.substring(result.indexOf(", ")+2);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }

                            try {
                                if(name!=null&&weight!=null){
                                    et1.setText(""+name);
                                    et2.setText(""+weight);
                                }
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    });

            try {
                if(!productList.isEmpty()){
                    Picasso.get().load("https://m.pricez.co.il/ProductPictures/"+barcode+".jpg").into(image);

                    if(dialogView!=null){
                        Picasso.get().load("https://m.pricez.co.il/ProductPictures/"+barcode+".jpg").into((ImageView)dialogView.findViewById(R.id.pic));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private String loadImageFromUrl(String barcode,ImageView image){
        String url="https://m.pricez.co.il/ProductPictures/"+barcode+".jpg";
        try {
            Picasso.get().load(url).into(image);
        } catch (Exception e) {
            e.printStackTrace();
            image.setImageResource(R.drawable.no_image);
        }
        return barcode;
    }

    public void delete(View view) {
        et1.setText("");
        name="";
        et2.setText("");
        weight="";
        barcode=null;
        pick.setText("");
        isScan=false;
        image.setImageResource(R.drawable.no_image);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        days = Integer.parseInt(text);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        TextView notify = dialogView.findViewById(R.id.textView10);
        if(hourOfDay<10){
            hours = "0" + hourOfDay;
        }
        else{
            hours = "" + hourOfDay;
        }
        if(minute<10){
            minutes = "0" + minute;
        }
        else{
            minutes = "" + minute;
        }
        notify.setText("I want to get notified on:   "+hours+":"+minutes);

    }
}