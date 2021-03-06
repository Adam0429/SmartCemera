package com.smartcemera;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.IllegalFormatCodePointException;

import android.util.Base64;
import android.util.Log;
import android.R.string;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import api.analyze;
import api.detect;

public class MainActivity extends Activity {


	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public void upload(View view){
	      Intent intent = new Intent(Intent.ACTION_GET_CONTENT);  
	      intent.setType("image/*");//设置类型
          intent.addCategory(Intent.CATEGORY_OPENABLE);  
          startActivityForResult(intent,1);  
	}
	
	public void camera(View view){
//		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {  //这个方法起保护作用，首先检查一下是否有相关应用能处理这个intent.
//            startActivityForResult(takePictureIntent, 2);//requestcode用来区分数据是从哪个acivity获得  
//        }  
//		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
////		CameraView cameraView = new CameraView(this);
////		cameraView.surfaceCreated(surfaceView.getHolder());
		Intent intent = new Intent(this,Camera.class);
		startActivity(intent);
	}
	
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  //在Activity中得到新打开Activity关闭后返回的数据

        super.onActivityResult(requestCode, resultCode, data);  
        TextView textView = (TextView)findViewById(R.id.textView1);
        TextView textView2 = (TextView)findViewById(R.id.textView2);
		ImageView mIV = (ImageView) findViewById(R.id.imageView1);
		
		  // TODO Auto-generated method stub  
		  
	    if(requestCode == 1) { //requestcode用来区分数据是从哪个acivity获得,这里1是上传,2是拍照
	    	Bitmap bm = null;  
	    	ContentResolver resolver = getContentResolver();  
	    
	    	if(data != null){
	   
	    		try {  
	        	
	    			Uri originalUri = data.getData(); // 获得图片的uri,选择图片时必须从图库中选择不然会报错  
	    			bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);        //得到bitmap图片
	    			mIV.setImageBitmap(ThumbnailUtils.extractThumbnail(bm, 500, 500)); 
//	    			textView2.setText(new detect(bitmapToBase64(bm)).run());
	    			String detectresult = new detect(bitmapToBase64(bm)).run();
	    			if(detectresult.contains("face_token")){
	    				String token = detectresult.split("face_token\": \"")[1].split("\"")[0];
	    				textView.setText(new analyze(token).run());
	    			}
	    			else if(detectresult.contains("INVALID_IMAGE_SIZE")||detectresult.contains("IMAGE_FILE_TOO_LARGE"))
	    				textView.setText("图片大小不合适,请控制在2M以内");
	    			else{
	    				textView.setText("");
	    				textView2.setText("错误报告:");
	    				textView2.append(detectresult);
	    			}
	    			mIV.setImageBitmap(bm); 
	      			String[] proj = { MediaStore.Images.Media.DATA }; 
	    			Cursor cursor = managedQuery(originalUri, proj, null, null, null);  
	    			// 按我个人理解 这个是获得用户选择的图片的索引值  
	    			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);  
	    			// 将光标移至开头 ，这个很重要，不小心很容易引起越界  
	    			cursor.moveToFirst();  
	    			// 最后根据索引值获取图片路径  
	    			String path = cursor.getString(column_index);  
	    			//Toast.makeText(this, new detect(base64).run(), Toast.LENGTH_SHORT).show(); 
	    			//Toast.makeText(this, , Toast.LENGTH_SHORT).show();
        
	    		} catch (IOException e) {  
	    			Toast.makeText(this, "读取图片error,请从图库中选择不然会报错", Toast.LENGTH_SHORT).show();   	
	    		}  
	    	}       
	    }

	    if(requestCode == 2){
	    	 if(data != null){  
	                Bundle extras = data.getExtras();  
	                if(extras != null){  
	                    Bitmap imageBitmap = (Bitmap) extras.get("data");  
	                    mIV.setImageBitmap(imageBitmap);  
	                }else{  
//	                    Log.d(tag,"no Bitmap return");  
	                }  
	            }else{  
//	                Log.d(tag,"data is null");  
	            }  
	          
	    }
	        
	}  
	
	public String bitmapToBase64(Bitmap bitmap) {  
        
		TextView textView2 = (TextView) findViewById(R.id.textView2);
	    String result = null;  
	    ByteArrayOutputStream baos = null;  
	    try {  
	        if (bitmap != null) {  
	            baos = new ByteArrayOutputStream();  
                bitmap.compress(CompressFormat.JPEG, 50, baos);//Bitmap.compress方法确实可以压缩图片，但压缩的是存储大小，即你放到disk上的大小.bitmap大小不变

	            baos.flush();  
	            baos.close();  
	  
	            byte[] bitmapBytes = baos.toByteArray();  
	            result = Base64.encodeToString(bitmapBytes,Base64.NO_WRAP);  //
	           
	        }  
	        else 
	        	textView2.append("空的bitmap");
	    } catch (IOException e) {  
        	textView2.append("错误");
	    }
//    	Toast.makeText(this, result, Toast.LENGTH_SHORT).show();  
//	    textView2.setText(result);
	    return result;  
	}  
}
