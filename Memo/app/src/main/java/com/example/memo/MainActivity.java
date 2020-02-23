package com.example.memo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private LinearLayout text_lay;
    private RecyclerView main_list;
    private Button edit_btn;
    private RecyclerView.LayoutManager layoutManager;
    public static int memo_index = 0;
    public static final String saveDir = Environment.getExternalStorageDirectory()+"/Memo";
    public static List<String> titleList;
    public static List<String> contentsList ;
    List<Bitmap> imgList ;
    public static List<String> indexList;


    @Override
    protected void onResume() {
        super.onResume();
        titleList = new ArrayList<String>();
        contentsList = new ArrayList<String>();
        imgList = new ArrayList<Bitmap>();
        indexList = new ArrayList<String>();
        getFileList();
        setAdepter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tedPermission();
        setContentView(R.layout.activity_main);

        main_list = findViewById(R.id.main_list);
        text_lay = findViewById(R.id.text_lay);
        edit_btn = findViewById(R.id.edit_btn);

    }

    private void setAdepter(){
        main_list.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        main_list.setLayoutManager(layoutManager);
        final itemAdepter adepter = new itemAdepter(titleList,contentsList,imgList, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getTag() != null)
                {
                    int position = (int)v.getTag();
                    memo_index = position;
                    Intent intent = new Intent(MainActivity.this,showActivity.class);
                    startActivity(intent);
                }

            }
        });
        main_list.setAdapter(adepter);
        main_list.getAdapter().notifyDataSetChanged();
    }

    public void edit_btn_click(View view){
        File dir = new File(saveDir);
        if(dir.listFiles() !=null)
        {
            File[] files = dir.listFiles();
            memo_index = files.length;
            for(int i = 0; i<files.length; i++){
                String s = files[i].getName().substring(5);
                String[] ret = s.split(".txt");
                if(ret[0].equals(((Integer)memo_index).toString())){
                    memo_index++;
                    i = 0;
                }
            }
        }
        else
        {
            memo_index = 0;
        }

        Intent intent = new Intent(MainActivity.this, EditActivity.class);
        startActivity(intent);
    }


    private void tedPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

            }
            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();


    }

    private void getFileList(){
        File dir = new File(saveDir);
        if(dir.listFiles() !=null)
        {
            File[] memos = dir.listFiles();
            for(File f : memos){
                String s = f.getName().substring(5);
                String[] ret = s.split(".txt");
                indexList.add(ret[0]);
                List<String> jsonList = loadJSONFromAsset(f.getPath());
                try {
                    for(int i = 0; i<jsonList.size(); i++)
                    {
                        JSONObject obj = new JSONObject(jsonList.get(i));
                        if(i == 0)
                        {
                            titleList.add(obj.getString("title"));
                            contentsList.add(obj.getString("contents"));
                        }
                        else if(i == 1)
                        {
                            String imgStr = (obj.getString("img"));
                            byte[] temp = Base64.decode(imgStr,Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(temp,0,temp.length);
                            imgList.add(bitmap);
                        }
                        else{
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    public static List<String> loadJSONFromAsset(String path) {
        BufferedReader br = null;
        List<String> contentGetter = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(path));

            String temp;

            while((temp = br.readLine())!=null)
            {
                contentGetter.add(temp);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return contentGetter;

    }



}