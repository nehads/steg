package com.example.dell.steg;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private static int RESULT_SHOW_MESSAGE = 1;
    private static int RESULT_HIDE_MESSAGE = 2;
    private String strPicturePath;
    private TextView textView;
    private ImageView imageView;
    private TextView txtView;
    StorageReference riversRef;
    String  abc="hellow";
    private StorageReference mStorageRef;
    private TextView newtextView;
    private ProgressBar progressBar;


    // Used to load the 'native-lib' library on application startup.
//    static {
//        System.loadLibrary("native-lib");
//    }


    @RequiresApi(api = Build.VERSION_CODES.M)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        String[] perms = { "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE" };
        int permsRequestCode = 200;
        requestPermissions(perms, permsRequestCode);
        setContentView(R.layout.activity_main);

      //  newtextView.setText(abc);
       // newtextView.setText(abc);


        // Example of a call to a native method
        // TextView tv = (TextView) findViewById(R.id.sample_text);
        // tv.setText(stringFromJNI());
      newtextView=(TextView)findViewById(R.id.editHideMessage);

         textView = (TextView) findViewById(R.id.txtShowMessage);
        // http://stackoverflow.com/questions/21072034/image-browse-button-in-android-activity
        Button buttonShowMessage = (Button) findViewById(R.id.buttonShowMessage);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        buttonShowMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_SHOW_MESSAGE);
            }
        });

        Button buttonHideMessage = (Button) findViewById(R.id.buttonHideMessage);
        buttonHideMessage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // if the edit text isn't showing, show it

                newtextView.setText(abc);
              //  String h=newtextView.getText().toString();
               // Toast.makeText(getApplicationContext(),h,Toast.LENGTH_SHORT).show();

                if ( newtextView.getVisibility() == View.GONE) {
                    newtextView.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.GONE);
                } else {
                    Intent i = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult(i, RESULT_HIDE_MESSAGE);
                }
            }
        });
    }

    private class DecodeAsyncWrapper extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strBitmapLocation) {
            Bitmap bitmap = BitmapFactory.decodeFile(strBitmapLocation[0]);
            return (new Decode(bitmap).getString());
        };

        @Override
        protected void onPostExecute(String result) {
            imageView.setVisibility(View.VISIBLE);
            textView.setText(result);
            progressBar.setVisibility(View.GONE);

        }
    }

    private class EncodeAsyncWrapper extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strs) {
            Bitmap bitmap = BitmapFactory.decodeFile(strs[0]);
            return (new Encode()).encoded(bitmap, strs[1]);
        };

        @Override
        protected void onPostExecute(Bitmap bmp) {
            SaveToDisk(bmp);
            imageView.setVisibility(View.INVISIBLE);
            imageView.setImageBitmap(bmp);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        ArrayList<String> result = data
//                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

//        final TextView  newtextView=(TextView)findViewById(R.id.editHideMessage);
      // txtView = (TextView) findViewById(R.id.txtShowMessage);
       // final EditText editText = (EditText) findViewById(R.id.editHideMessage);
        // non final copy for the asynch postExecute to write to.
        textView = (TextView) findViewById(R.id.txtShowMessage);
        imageView = (ImageView) findViewById(R.id.imgView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // first run, there's no decoded message yet.
        // view text invisible
        // edit text visible
        textView.setVisibility(View.GONE);
       newtextView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);

        String strMessage =  newtextView.getText().toString();

      //  String strMessage =  abc;

        // this code decodes
        if (requestCode == RESULT_SHOW_MESSAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            ImageView imageView = (ImageView) findViewById(R.id.imgView);
            final Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            imageView.setImageBitmap(bitmap);

            // get ready to show the hidden message
            textView.setVisibility(View.VISIBLE);
            String temp="";

                DecodeAsyncWrapper task = new DecodeAsyncWrapper();
                task.execute(new String[] { picturePath });

            newtextView.setVisibility(View.GONE);


        }

        // this code encodes the message in editText
        if (requestCode == RESULT_HIDE_MESSAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            strPicturePath = cursor.getString(columnIndex);//actual path to d image
            cursor.close();
            newtextView.setVisibility(View.GONE);

             EncodeAsyncWrapper task = new EncodeAsyncWrapper();
            task.execute(new String[] { strPicturePath, strMessage });
            uploadFile();



        }
    }

    private void uploadFile() {
         Uri filetexturi = Uri.fromFile(new File(strPicturePath));
        if (filetexturi != null) {
            Toast.makeText(getApplicationContext(), "in Uploaded ", Toast.LENGTH_LONG).show();


            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            Toast.makeText(getApplicationContext(), "bfore File Uploaded ", Toast.LENGTH_LONG).show();

             riversRef = mStorageRef.child("new/pic.jpg");

            riversRef.putFile(filetexturi)//filePath changed to newroot
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog  progressDialog.dismiss();

                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                            ;
                        }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying error message
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        }
        //if there is not any file
        else {
            //you can display an error toast
        }



    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        switch(permsRequestCode){
            case 200:
                boolean writeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
 //   public native String stringFromJNI();

    private void SaveToDisk(Bitmap toDisk) {
        try {
            // String path = Environment.getExternalStorageDirectory().toString();
            String path = absPath(strPicturePath);
            String originalName = strPicturePath.replace(path, "").replace("/", "");
            String[] aryNameAndExt = originalName.split("\\.");
            String originalExt = aryNameAndExt[aryNameAndExt.length - 1];
            OutputStream fOut = null;

            File file = new File(path, originalName + ".msg." + originalExt);
            fOut = new FileOutputStream(file);

            toDisk.compress(Bitmap.CompressFormat.PNG, 0, fOut);
            fOut.flush(); // Not really required
            fOut.close(); // do not forget to close the stream

           MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), originalName);
            // MediaStore.Images.Media.insertImage(getContentResolver(), toDisk, "WithMessage", "With Message");

            // http://www.grokkingandroid.com/adding-files-to-androids-media-library-using-the-mediascanner/
            MediaScannerConnection.scanFile(
                    getApplicationContext(),
                    new String[]{file.getAbsolutePath()},
                    null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.v("Steganography",
                                    "file " + path + " was scanned seccessfully: " + uri);
                        }
                    });



        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private String absPath(String filePath) {
        // returns the absolute path to the file
        String[] pathFolders = filePath.split("/");
        int nAbsPathLength = pathFolders.length - 1;
        String[] outputPath = new String[nAbsPathLength];

        for (int i = 0; i < nAbsPathLength; i++) {
            outputPath[i] = pathFolders[i];
        }

        return TextUtils.join("/", outputPath);
    }
}



