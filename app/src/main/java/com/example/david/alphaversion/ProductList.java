package com.example.david.alphaversion;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
        name.setText(product.getName());
        weight.setText(""+product.getWeight()+" kg");
        date.setText(""+product.getEnterD());
        time.setText(""+product.getEnterT());
        if(product.getExpired()!=null) {
            expire.setText(""+product.getExpired());
        }
        else{
            expire.setText("None");
        }


        if(product.getBarcode()!=null){
            String url="https://m.pricez.co.il/ProductPictures/"+product.getBarcode()+".jpg";
            Picasso.get().load(url).into(image);
        }
        else{
            image.setImageResource(R.drawable.no_image);
        }

        return  listViewItem;
    }
}
