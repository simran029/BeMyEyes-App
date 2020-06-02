package gad.heartbeat.androidflask.easyupload;

import android.Manifest;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

//import dmax.dialog.SpotsDialog;
//import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.speech.tts.TextToSpeech;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import java.util.Locale;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.camerakit.CameraKitView;

public class MainActivity extends AppCompatActivity {
    public TextToSpeech mTTS;
    public EditText mEditText;
    public Button mButtonSpeak;
    ImageView cameraView;
    Button btn_detect;
	int i=1;
	boolean imagesSelected = false;
	ArrayList<String> selectedImagesPaths;
    AlertDialog waitingDialog;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent(){
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePic.resolveActivity(getPackageManager())!=null)
        {
            startActivityForResult(takePic,REQUEST_IMAGE_CAPTURE);
        }
    }


	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = (ImageView)findViewById(R.id.camera_view);
        btn_detect = (Button)findViewById(R.id.btn_detect);
        btn_detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                selectImage();
            }
        });
		mButtonSpeak = findViewById(R.id.button_speak);
        mEditText = findViewById(R.id.status);
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        mButtonSpeak.setEnabled(true);
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }});

        mButtonSpeak.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        speak();
                    }
                });

    }
 private void selectImage() {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo"))
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(Environment.getExternalStorageDirectory().toString(), "temp.jpg");
                    Uri imageUri = FileProvider.getUriForFile(
                            MainActivity.this,
                            getApplicationContext().getPackageName()+".provider", //(use your app signature + ".provider" )
                            f);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);
                }
                else if (options[item].equals("Choose from Gallery"))
                {
                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(getApplicationContext(), "Access to Storage Permission Granted. Thanks.", Toast.LENGTH_SHORT).show();
                } else {
//                    Toast.makeText(getApplicationContext(), "Access to Storage Permission Denied.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case 2: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(getApplicationContext(), "Access to Internet Permission Granted. Thanks.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Access to Internet Permission Denied.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public void connectServer() {
        //mEditText.setText(selectedImagesPaths);
        String ipv4Address = "192.168.0.106";
        String portNumber = "5000";
        String postUrl = "http://" + ipv4Address + ":" + portNumber + "/";

		
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
		
            for (int i = 0; i < selectedImagesPaths.size(); i++) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
           // options.inSampleSize= 8;
            //options.inJustDecodeBounds= false;
           // File f= new File(selectedImagesPaths.get(i));
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try {
                // Read BitMap by file path.
               // mEditText.setText("line1");
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImagesPaths.get(i), options);
                if(bitmap==null)
                {
                   // mEditText.setText("null");
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            }catch(Exception e){
              //  mEditText.setText("hi"+ selectedImagesPaths.get(i)+e.getMessage());
                return;
            }
            byte[] byteArray = stream.toByteArray();

            multipartBodyBuilder.addFormDataPart("image" + i, "Android_Flask_" + i + ".jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray));
        }

        RequestBody postBodyImage = multipartBodyBuilder.build();


        postRequest(postUrl, postBodyImage);
    }

    void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient.Builder().readTimeout(100, TimeUnit.SECONDS).build();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        Log.w("reqyest","ho ja ");
        //mEditText.setText(postBody.toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                Log.d("FAIL", e.getMessage());

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView responseText = findViewById(R.id.status);
                        responseText.setText("Failed to Connect to Server. Please Try Again.");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView responseText = findViewById(R.id.status);
                        try {
                            responseText.setText(response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                File f = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
                if(data != null){
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }}
                try {
                    Bitmap bitmap;
                    Log.w("chl","1");
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
                            bitmapOptions);
                    cameraView.setImageBitmap(bitmap);
                    Log.w("chl","2");
//                    String path = android.os.Environment.getExternalStorageDirectory()+ File.separator+ "know_it_all";
//                    String path = File.separator + "storage" + File.separator + "emulated" + File.separator + "0" +File.separator+ "DCIM"+File.separator+"Camera"+File.separator;
                    String path = File.separator + "storage/emulated/0/DCIM/Camera/";
//                    f.delete();
                    Log.w("chl","3");
                    OutputStream outFile = null;
                    String system_time = String.valueOf(System.currentTimeMillis());
                    File file = new File(path, system_time + ".jpg");
                    Log.w("chl","4");
                    try {
                        outFile = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        Log.w("kuch ho ja", "something is happening");
                        mEditText.setText("jai ho");
                       selectedImagesPaths = new ArrayList<>();
                        String currPath = path + system_time + ".jpg";
                        mEditText.setText(currPath);

                        //selectedImagesPaths.add(currPath);
                       // connectServer();
//                        cameraView.setImageBitmap(thumbnail);
                        outFile.flush();
                        outFile.close();
                        selectedImagesPaths.add(currPath);
                        connectServer();
				/*Uri selectedImage = data.getData();
				String currImagesPaths = getPath(getApplicationContext(), selectedImage);
				Log.d("ImageDetails", "Single Image URI : " + selectedImage);
                Log.d("ImageDetails", "Single Image Path : " + currImagesPaths);*/
               // mEditText.setText(currPath);
				//selectedImagesPaths.add(currPath);
                       // connectServer();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 2) {
				if(data.getData()!=null)
				{
				selectedImagesPaths = new ArrayList<>();
				Uri selectedImage = data.getData();
				String currImagesPaths = getPath(getApplicationContext(), selectedImage);
				Log.d("ImageDetails", "Single Image URI : " + selectedImage);
                Log.d("ImageDetails", "Single Image Path : " + currImagesPaths);
				mEditText.setText(currImagesPaths);
                selectedImagesPaths.add(currImagesPaths);
				
                
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                Log.w("path of image", picturePath+"");
               // mEditText.setText(selectedImagesPaths);
                cameraView.setImageBitmap(thumbnail);
                connectServer();
				}
            }
        }
    }
	
	public void speak() {
            String text = mEditText.getText().toString();
            mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
        @Override
        public void onDestroy() {
            if (mTTS != null) {
                mTTS.stop();
                mTTS.shutdown();
            }
            super.onDestroy();
        }

    // Implementation of the getPath() method and all its requirements is taken from the StackOverflow Paul Burke's answer: https://stackoverflow.com/a/20559175/5426539
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}



