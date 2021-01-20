package main.com.notaryagent.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import main.com.notaryagent.R;
import main.com.notaryagent.constant.BaseUrl;
import main.com.notaryagent.constant.MySession;
import main.com.notaryagent.databinding.ActivityDocumentBinding;
import main.com.notaryagent.tabactivity.MainTabActivity;
import www.develpoeramit.mapicall.ApiCallBuilder;
import www.develpoeramit.mapicall.Method;

public class DocumentActivity extends AppCompatActivity {
    private onImageSelected listener;
    private ActivityDocumentBinding binding;
    private HashMap<String, String> param;
    private ApiCallBuilder mApiCall;
    private MySession mySession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_document);
        param=(HashMap<String,String>)getIntent().getExtras().getSerializable("data");
        BindView();
    }

    private void BindView() {
        mySession = new MySession(this);
        mApiCall= ApiCallBuilder.build(this).setMethod(Method.POST).setUrl(BaseUrl.baseurl + "signup").isShowProgressBar(true).setParam(param);
        StringBuilder URL=new StringBuilder();
        URL.append(BaseUrl.baseurl + "signup?");
        for (Map.Entry<String, String> entry : param.entrySet()) {
            URL.append(entry.getKey()+"="+entry.getValue()+"&");
        }
        Log.e("SignUpURL","===>"+URL.toString());
        binding.layDriverLicens.setOnClickListener(v->{
            onPickImage(path -> {
                mApiCall.setFile("driver_lincens",path);
                binding.imgLinces.setImageResource(R.drawable.checkimg);
            });
        });
        binding.layYourPhoto.setOnClickListener(v->{
            onPickImage(path -> {
                mApiCall.setFile("driver_lincens",path);
                binding.yourPhotos.setImageResource(R.drawable.checkimg);
            });
        });
        binding.layW9.setOnClickListener(v->{
            onPickImage(path -> {
                mApiCall.setFile("driver_lincens",path);
                binding.imgW9.setImageResource(R.drawable.checkimg);
            });
        });
        binding.layIndviSign.setOnClickListener(v->{
            onPickImage(path -> {
                mApiCall.setFile("driver_lincens",path);
                binding.imgIndviSign.setImageResource(R.drawable.checkimg);
            });
        });
        binding.layApplication.setOnClickListener(v->{
            onPickImage(path -> {
                mApiCall.setFile("driver_lincens",path);
                binding.imgApplication.setImageResource(R.drawable.checkimg);
            });
        });
        binding.layNotaryAgentAgreement.setOnClickListener(v->{
            onPickImage(path -> {
                mApiCall.setFile("driver_lincens",path);
                binding.imgNotaryAgentAgreement.setImageResource(R.drawable.checkimg);
            });
        });
        binding.layCopyOfBond.setOnClickListener(v->{
            onPickImage(path -> {
                mApiCall.setFile("driver_lincens",path);
                binding.imgCopyOfBond.setImageResource(R.drawable.checkimg);
            });
        });
        binding.btnSent.setOnClickListener(v->{
            mApiCall.execute(new ApiCallBuilder.onResponse() {
                @Override
                public void Success(String response) {
                    Log.e("Response","====>"+response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equalsIgnoreCase("1")) {
                          /*  mySession.setlogindata(response);
                            mySession.signinusers(true);
                            mySession.onlineuser(false);*/
                            accountCreated();
                        } else {
                            Toast.makeText(DocumentActivity.this, "" + jsonObject.getString("result"), Toast.LENGTH_LONG).show();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Intent i = new Intent(DocumentActivity.this, LoginAct.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(i);
                }

                @Override
                public void Failed(String error) {
                    Log.e("error","====>"+error);
                }
            });
        });
    }
    private void accountCreated() {
        Dialog dialogSts = new Dialog(DocumentActivity.this, R.style.DialogSlideAnim);
        dialogSts.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogSts.setCancelable(false);
        dialogSts.setContentView(R.layout.signupsuccess);
        dialogSts.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView yes_tv = dialogSts.findViewById(R.id.yes_tv);
        yes_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSts.dismiss();
                startActivity(new Intent(DocumentActivity.this, LoginAct.class));
            }
        });
        dialogSts.show();
    }
    void onPickImage(onImageSelected listener) {
        this.listener = listener;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), 1);
    }

    interface onImageSelected {
        void ImagePath(String path);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    Uri selectedImage = data.getData();
                    //  getPath(selectedImage);
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String FinalPath = cursor.getString(columnIndex);
                    cursor.close();
                    String ImagePath = getPath(selectedImage);
                    Log.e("PATH Get Gallery", "" + ImagePath);
                    decodeFile(ImagePath);
                    break;
                case 2:
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    ImagePath = saveToInternalStorage(photo);
                    Log.e("PATH Camera", "" + ImagePath);
                    break;
            }
        }

    }
    public void decodeFile(String filePath) {
        Log.e("ImagePath in decode", " >>" + filePath);
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, o);
        // The new size we want to scale to
        final int REQUIRED_SIZE = 1024;
        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }
        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, o2);
      String  path = saveToInternalStorage(bitmap);
      listener.ImagePath(path);

    }
    private String saveToInternalStorage(Bitmap bitmapImage) {
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        String dateToStr = format.format(today);
        ContextWrapper cw = new ContextWrapper(DocumentActivity.this);
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath = new File(directory, "profile_" + dateToStr + ".JPEG");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mypath.getAbsolutePath();
    }
    public String getPath(Uri uri) {
        String path = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();
        cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        if (cursor.moveToFirst()) {
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            //  Log.e("image_path.===..", "" + path);
        }
        cursor.close();
        return path;
    }
}