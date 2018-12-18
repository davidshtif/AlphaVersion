package com.example.david.alphaversion;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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

        Product product=productList.get(position);

        name.setText(product.getName());
        weight.setText(""+product.getWeight());

        return  listViewItem;
    }
}
