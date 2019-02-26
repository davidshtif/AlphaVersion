package com.example.david.alphaversion;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class Main extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    EditText et1,et2;
    ListView list;
    String name,weight,enterD,enterT,expired,barcode,url;
    DatabaseReference ref;
    Button button,scan;
    ImageView image;
    List<Product> productList;
    TextView pick;
    private AlertDialog.Builder dialogBuilder;
    private View dialogView;
    private TextView expDate;
    private Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et1=(EditText)findViewById(R.id.editText);
        et2=(EditText)findViewById(R.id.editText2);
        list=(ListView)findViewById(R.id.list);
        image=(ImageView) findViewById(R.id.imageView);
        pick=(TextView)findViewById(R.id.picked);
        ref=FirebaseDatabase.getInstance().getReference("products");
        productList=new ArrayList<>();

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
        Calendar calendar=Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        expired= DateFormat.getDateInstance(DateFormat.SHORT).format(calendar.getTime());
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
        String url="https://m.pricez.co.il/ProductPictures/"+product.getBarcode()+".jpg";
        String exp=product.getExpired();

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
                    updateProduct(product.getId(),product.getName(),product.getEnterD(),product.getEnterT(),product.getExpired(),barcode,product.getWeight());
                    image.setImageResource(R.drawable.no_image);
                    alertDialog.dismiss();
                }
                else{
                    if(!name.isEmpty()){
                        updateProduct(product.getId(),name,product.getEnterD(),product.getEnterT(),product.getExpired(),barcode,product.getWeight());
                        image.setImageResource(R.drawable.no_image);
                        alertDialog.dismiss();
                    }
                    else{
                        if(!sWeight.isEmpty()){
                            updateProduct(product.getId(),product.getName(),product.getEnterD(),product.getEnterT(),product.getExpired(),barcode,Double.parseDouble(sWeight));
                            image.setImageResource(R.drawable.no_image);
                            alertDialog.dismiss();
                        }
                        else{
                            updateProduct(product.getId(),name,product.getEnterD(),product.getEnterT(),product.getExpired(),barcode,Double.parseDouble(sWeight));
                            image.setImageResource(R.drawable.no_image);
                            alertDialog.dismiss();
                        }
                    }
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                alertDialog.dismiss();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                deleteProduct(product.getId());
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


                            }
        });

        buttonNewP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId()==R.id.newPic){
                    IntentIntegrator scanIntegrator = new IntentIntegrator(Main.this);
                    scanIntegrator.initiateScan();

                    product.setBarcode(loadImageFromUrl(barcode,newP));
                    Picasso.get().load("https://m.pricez.co.il/ProductPictures/"+barcode+".jpg").into(newP);
                }
            }
        });
    }

    private void deleteProduct(String productId){
        DatabaseReference drProduct=FirebaseDatabase.getInstance().getReference("products").child(productId);
        drProduct.removeValue();
        Toast.makeText(this,"Product deleted",Toast.LENGTH_SHORT).show();
    }

    private boolean updateProduct(String id,String name,String enterD,String enterT,String expired,String barcode,double weight){
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("products").child(id);
        Product product=new Product(id,name,enterD,enterT,expired,barcode,weight);
        databaseReference.setValue(product);
        Toast.makeText(this,"Updated Successfully",Toast.LENGTH_SHORT).show();
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu){
        menu.add("Send SMS");
        menu.add("Send Email");
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
        return true;
    }

    public void upload(View view) {
        name=et1.getText().toString();
        weight=et2.getText().toString();
        Calendar date=Calendar.getInstance();
        Calendar time=Calendar.getInstance();
        enterD=DateFormat.getDateInstance(DateFormat.SHORT).format(date.getTime());
        SimpleDateFormat mdformat=new SimpleDateFormat("HH:mm:ss");
        enterT=mdformat.format(time.getTime());
        if(name.equals(""))
            Toast.makeText(this,"You didn't enter a name",Toast.LENGTH_SHORT).show();
        else if(weight.equals(""))
                Toast.makeText(this,"You didn't enter a weight",Toast.LENGTH_SHORT).show();
            else{
                String id=ref.push().getKey();
                Product product=new Product(id,name,enterD,enterT,expired,barcode,Double.parseDouble(weight));
                ref.child(id).setValue(product);
                et1.setText("");
                et2.setText("");
                pick.setText("");
                image.setImageResource(R.drawable.no_image);
            }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            barcode= scanningResult.getContents();
            barcode=loadImageFromUrl(barcode,image);
            try {
                if(!productList.isEmpty()){
                    Picasso.get().load("https://m.pricez.co.il/ProductPictures/"+barcode+".jpg").into((ImageView) dialogView.findViewById(R.id.pic));
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
        Picasso.get().load(url).into(image);
        return barcode;
    }
}