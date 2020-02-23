package com.example.memo;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.BitSet;
import java.util.List;

public class itemAdepter extends RecyclerView.Adapter<itemAdepter.MyViewHolder> {
    List<String> titledat;
    List<String> condat;
    List<Bitmap> imgdat;
    private static View.OnClickListener onClickListener;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public TextView contents;
        public ImageView some;
        public View rootView;
        public MyViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.title);
            contents = v.findViewById(R.id.contents);
            some = v.findViewById(R.id.some);
            rootView = v;
            v.setEnabled(true);
            v.setClickable(true);
            v.setOnClickListener(onClickListener);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public itemAdepter(List<String> title,List<String> contents,List<Bitmap> img ,View.OnClickListener onclick) {
        titledat = title;
        condat = contents;
        imgdat = img;
        onClickListener = onclick;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public itemAdepter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_layout, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.setText(titledat.get(position));
        holder.contents.setText(condat.get(position));
        holder.some.setImageBitmap(imgdat.get(position));

        holder.rootView.setTag(position);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return titledat.size();
    }

}