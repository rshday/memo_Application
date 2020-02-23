package com.example.memo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class showActivity extends AppCompatActivity {

    public static TextView show_title;
    public static TextView show_contents;
    public static List<Bitmap> img_arr;
    private RecyclerView img_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        show_title = findViewById(R.id.show_title);
        show_contents = findViewById(R.id.show_contents);
        img_list = findViewById(R.id.img_list);

        show_title.setText(MainActivity.titleList.get(MainActivity.memo_index));
        show_contents.setText(MainActivity.contentsList.get(MainActivity.memo_index));

        get_img_arr();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        img_list.setLayoutManager(layoutManager);
        img_list.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            private ImageView img;

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                final RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.show_item, parent, false);

                RecyclerView.ViewHolder vh = new RecyclerView.ViewHolder(v){};
                return vh;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                img = holder.itemView.findViewById(R.id.show_img_view);
                img.setImageBitmap(img_arr.get(position));
            }

            @Override
            public int getItemCount() {
                return img_arr.size();
            }
        });

    }

    private void get_img_arr(){
        img_arr = new ArrayList<Bitmap>();
        File dir = new File(MainActivity.saveDir);
        File[] files = dir.listFiles();
        for(File f : files){
            String s = f.getName().substring(5);
            String[] ret = s.split(".txt");
            if(ret[0].equals(MainActivity.indexList.get(MainActivity.memo_index))){
                List<String> jsonList = MainActivity.loadJSONFromAsset(f.getPath());
                try {
                    for(int i = 1; i<jsonList.size(); i++)
                    {
                        JSONObject obj = new JSONObject(jsonList.get(i));
                        String imgStr = (obj.getString("img"));
                        byte[] temp = Base64.decode(imgStr,Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(temp,0,temp.length);
                        img_arr.add(bitmap);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.showmenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.delmenu)
        {
            File dir = new File(MainActivity.saveDir);
            File[] memolist = dir.listFiles();
            memolist[MainActivity.memo_index].delete();
            finish();
        }
        else{
            EditActivity.isFix = true;
            Intent intent = new Intent(showActivity.this,EditActivity.class);
            startActivity(intent);
        }
        return true;
    }
}
