package com.example.david.alphaversion;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ProductList extends ArrayAdapter <Product>{
    private Activity context;
    private List<Product> productList;

    public ProductList(Activity context, List<Product>productList){
        super(context, R.layout.list_layout, productList);
        this.context=context;
        this.productList=productList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();

        View listViewItem=inflater.inflate(R.layout.list_layout,null,true);
        TextView name=(TextView)listViewItem.findViewById(R.id.Name);
        TextView weight=(TextView)listViewItem.findViewById(R.id.Weight);
        TextView date=(TextView)listViewItem.findViewById(R.id.Date);
        TextView time=(TextView)listViewItem.findViewById(R.id.Time);
        TextView expire=(TextView)listViewItem.findViewById(R.id.Expire);
        ImageView image=(ImageView) listViewItem.findViewById(R.id.imageView);

        Product product=productList.get(position);

        if(product.getBarcode()!=null){
            String url="https://m.pricez.co.il/ProductPictures/"+product.getBarcode()+".jpg";
            Picasso.get().load(url).into(image);
            product.setIsScan(true);
        }
        else{
            image.setImageResource(R.drawable.no_image);
        }

        if(product.getIsScan()){
           product=setNW(product);
        }

        name.setText(""+product.getName());
        weight.setText(""+product.getWeight());
        date.setText(""+product.getEnterD());
        time.setText(""+product.getEnterT());
        if(product.getExpired()!=null) {
            expire.setText(""+product.getExpired());
        }
        else{
            expire.setText("None");
        }
        return  listViewItem;
    }

    public Product setNW(final Product product){

        Ion.with(getContext())
                .load("https://chp.co.il/%D7%91%D7%90%D7%A8%20%D7%A9%D7%91%D7%A2/0/0/"+product.getBarcode()+"/0")
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        result=result.substring(72,result.indexOf("</title>",49));
                        product.setName(result.substring(0,result.indexOf(",")));
                        product.setWeight(result.substring(result.indexOf(", ")+2));
                    }
                });
        return product;
    }
}
