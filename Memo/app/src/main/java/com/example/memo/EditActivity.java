package com.example.memo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import android.net.Uri;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EditActivity extends AppCompatActivity {
    private static final int Pick_img = 1;
    private static final int Pick_cam = 2;
    public static boolean isFix = false;

    private RecyclerView img_list;
    private List<Bitmap> img_array;
    private EditText edit_title;
    private EditText edit_contents;
    private Bitmap bitmap;


    private File tempImg;
    private Bitmap tempbit;
    private String uristr;

    @Override
    protected void onStart() {
        super.onStart();
        bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.non);
        if(isFix){
            edit_title.setText(showActivity.show_title.getText());
            edit_contents.setText(showActivity.show_contents.getText());
            img_array = showActivity.img_arr;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isFix = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editmenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.savememo)
        {
            if(edit_title.getText().length()<1){
                Toast.makeText(EditActivity.this, "제목이 비어 있습니다.", Toast.LENGTH_SHORT).show();
                return true;
            }
            else if(edit_contents.getText().length()<1){
                Toast.makeText(EditActivity.this, "본문이 비어 있습니다.", Toast.LENGTH_SHORT).show();
                return true;
            }

            if(img_array.size() == 0)
            {
                img_array.add(bitmap);
            }

            JSONArray arr = new JSONArray();
            JSONObject obj = new JSONObject();

            try {
                obj.put("title",edit_title.getText());
                obj.put("contents",edit_contents.getText());
                arr.put(obj);
                List<String> byteimgs = new ArrayList<>();
                for(Bitmap b : img_array){
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    b.compress(Bitmap.CompressFormat.JPEG,100,bao);
                    byte[] temp = bao.toByteArray();
                    String str = Base64.encodeToString(temp,Base64.DEFAULT);
                    byteimgs.add(str);
                    obj = new JSONObject();
                    obj.put("img",str);
                    arr.put(obj);
                }
                int index = MainActivity.memo_index;
                String FileName = "Memo_"+((Integer)index).toString()+".txt";
                String dirPath = Environment.getExternalStorageDirectory()+"/Memo";
                File storageDir = new File(dirPath);

                if (!storageDir.exists()) storageDir.mkdirs();
                File isnew = new File(dirPath+"/"+FileName);
                if(isnew.exists())isnew.delete();
                FileOutputStream fos = new FileOutputStream(dirPath+"/"+FileName, false);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
                //writer.append("[");
                for(int i= 0; i<arr.length(); i++)
                {
                    writer.append(arr.getJSONObject(i).toString());
                    if(i < arr.length()-1)
                        writer.append("\n");
                }
                //writer.append("]");
                writer.flush();
                writer.close();
                fos.close();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        Intent intent = new Intent(EditActivity.this,MainActivity.class);
        startActivity(intent);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit);
        edit_title = findViewById(R.id.edit_title);
        edit_contents = findViewById(R.id.edit_contents);
        img_list = findViewById(R.id.img_list);
        img_array = new ArrayList<Bitmap>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        img_list.setLayoutManager(layoutManager);
        img_list.setAdapter(new RecyclerView.Adapter() {

            private ImageView img;

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                final RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.img_item, parent, false);

                RecyclerView.ViewHolder vh = new RecyclerView.ViewHolder(v){};
                return vh;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                img = holder.itemView.findViewById(R.id.img_view);
                img.setImageBitmap(img_array.get(position));
                holder.itemView.setTag(position);
                holder.itemView.findViewById(R.id.delbtn).setTag(position);
            }

            @Override
            public int getItemCount() {
                return img_array.size();
            }
        });

    }

    public void addimgbtn(View view)
    {
        CharSequence info[] = new CharSequence[] {"갤러리에서 가져오기", "사진 찍기","URI 가져오기" };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("이미지 저장");
        builder.setItems(info, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which)
                {
                    case 0:
                        addImg();
                        break;
                    case 1:
                        addcam();
                        break;
                    case 2:
                        adduri();
                        break;
                }
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void delimg(View view){
        int index = 10;
        if(view.findViewById(R.id.delbtn).getTag() != null)
        {
            index = (int)view.findViewById(R.id.delbtn).getTag();
        }

        img_array.remove(index);
        img_list.getAdapter().notifyDataSetChanged();
    }

    private void addImg(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, Pick_img);
    }

    private void addcam(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            tempImg = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
        if (tempImg != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                Uri photoUri = FileProvider.getUriForFile(this,
                        "com.example.memo.provider", tempImg);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, Pick_cam);
            } else {
                Uri photoUri = Uri.fromFile(tempImg);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, Pick_cam);
            }
        }
    }

    private void adduri()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Input Uri");

        final EditText uri = new EditText(this);
        alert.setView(uri);

        alert.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                uristr = uri.getText().toString()==""? null : uri.getText().toString();
                if(uristr != null)
                {
                    Thread tr = new Thread(){
                        @Override
                        public void run(){
                            try{
                                URL url = new URL(uristr);
                                URLConnection conn = url.openConnection();
                                conn.connect();
                                InputStream is = conn.getInputStream();
                                tempbit = BitmapFactory.decodeStream(is);
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                    };
                    tr.start();
                    try {
                        tr.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(tempbit != null)
                    {
                        img_array.add(tempbit);
                        img_list.getAdapter().notifyDataSetChanged();
                    }
                }
            }
        });

        alert.setNegativeButton("no",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                uristr = null;
            }
        });

        alert.show();

    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = timeStamp + "_";
        String dirPath = Environment.getExternalStorageDirectory()+"/Memopoto";
        File storageDir = new File(dirPath);
        if (!storageDir.exists()) storageDir.mkdirs();

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            if (tempImg != null) {
                if (tempImg.exists()) {
                    if (tempImg.delete()) {
                        tempImg = null;
                    }
                }
            }
            return;
        }

        if (requestCode == Pick_img){
            Uri photoUri = data.getData();
            Cursor cursor = null;

            try {
                String[] proj = { MediaStore.Images.Media.DATA };

                assert photoUri != null;
                cursor = getContentResolver().query(photoUri, proj, null, null, null);

                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                cursor.moveToFirst();

                tempImg = new File(cursor.getString(column_index));
                if(tempImg != null)
                {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    Bitmap originalBm = BitmapFactory.decodeFile(tempImg.getAbsolutePath(), options);
                    Matrix m = new Matrix();
                    m.postRotate(90f);
                    Bitmap getimg = Bitmap.createBitmap(originalBm, 0, 0,
                            originalBm.getWidth(), originalBm.getHeight(), m, false);

                    img_array.add(getimg);
                }

                img_list.getAdapter().notifyDataSetChanged();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        if(requestCode == Pick_cam){
            if(tempImg != null)
            {
                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap originalBm = BitmapFactory.decodeFile(tempImg.getAbsolutePath(), options);
                Matrix m = new Matrix();
                m.postRotate(90f);
                Bitmap getimg = Bitmap.createBitmap(originalBm, 0, 0,
                        originalBm.getWidth(), originalBm.getHeight(), m, false);

                img_array.add(getimg);
            }

            img_list.getAdapter().notifyDataSetChanged();
        }


    }


}
