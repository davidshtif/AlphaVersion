package com.example.david.alphaversion;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Main extends AppCompatActivity{

    EditText et1,et2;
    ListView list;
    String name,weight;
    DatabaseReference ref;
    List<Product> productList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et1=(EditText)findViewById(R.id.editText);
        et2=(EditText)findViewById(R.id.editText2);
        list=(ListView)findViewById(R.id.list);
        ref=FirebaseDatabase.getInstance().getReference("products");
        productList=new ArrayList<>();

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Product product=productList.get(position);
                showUpdateDialog(product.getId(),product.getName());
                return false;
            }
        });
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

    private void showUpdateDialog(final String productId, String productName){

        AlertDialog.Builder dialogBuilder=new AlertDialog.Builder(this);
        LayoutInflater inflater=getLayoutInflater();
        final View dialogView=inflater.inflate(R.layout.update_dialog,null);
        dialogBuilder.setView(dialogView);
        final EditText editTextName=(EditText) dialogView.findViewById(R.id.up_name);
        final EditText editTextWeight=(EditText) dialogView.findViewById(R.id.up_weight);
        final Button buttonUpdate=(Button)dialogView.findViewById(R.id.up);
        final Button buttonCancel=(Button)dialogView.findViewById(R.id.cancel);
        final Button buttonDelete=(Button)dialogView.findViewById(R.id.delete);

        dialogBuilder.setTitle("Updating product "+productName);
        final AlertDialog alertDialog=dialogBuilder.create();
        alertDialog.show();
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                String name=editTextName.getText().toString();
                String sWeight=editTextWeight.getText().toString();
                if(name.isEmpty()){
                    Toast.makeText(getApplicationContext(),"You didn't enter a name",Toast.LENGTH_SHORT).show();
                } else if(sWeight.isEmpty()){
                        Toast.makeText(getApplicationContext(),"You didn't enter a weight",Toast.LENGTH_SHORT).show();
                        }else{
                            double weight=Double.parseDouble(sWeight);
                            updateProduct(productId,name,weight);
                            alertDialog.dismiss();
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
                deleteProduct(productId);
                alertDialog.dismiss();
            }
        });
    }

    private void deleteProduct(String productId){
        DatabaseReference drProduct=FirebaseDatabase.getInstance().getReference("products").child(productId);
        drProduct.removeValue();
        Toast.makeText(this,"Product deleted",Toast.LENGTH_SHORT).show();
    }

    private boolean updateProduct(String id,String name,double weight){
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("products").child(id);
        Product product=new Product(id,name,weight);
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
        if(name.equals(""))
            Toast.makeText(this,"You didn't enter a name",Toast.LENGTH_SHORT).show();
        else if(weight.equals(""))
                Toast.makeText(this,"You didn't enter a weight",Toast.LENGTH_SHORT).show();
            else{
                String id=ref.push().getKey();
                Product product=new Product(id,name,Double.parseDouble(weight));
                ref.child(id).setValue(product);
            }
    }
}