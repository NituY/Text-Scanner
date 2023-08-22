package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextToSpeech mTTS;
    Button but_capture , btn_Read, but_copy;
    TextView textdata ;
    Bitmap bitmap;
    private static final int REQUEST_CAMERA_CODE = 100;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        but_capture= findViewById(R.id.cap_btn);
        but_copy = findViewById(R.id.copy_btn);
        textdata = findViewById(R.id.Textdata);
        btn_Read = findViewById(R.id.Read_btn);
        
        //Read feature
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = mTTS.setLanguage(Locale.ENGLISH);
                    if(result == TextToSpeech.LANG_MISSING_DATA 
                            || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("TTS"," Language not Supported");
                    }
                    else {
                        btn_Read.setEnabled(true);
                    }
                }
                else {
                    Log.e("TTS", "Initialization Failed");
                }
            }
        });
        btn_Read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak();
            }
        });
        

        
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {
                    Manifest.permission.CAMERA
            }, REQUEST_CAMERA_CODE);
        }
        but_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);

            }
        });

       but_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String scanned_text = textdata.getText().toString();
                copytoClipBoard(scanned_text);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver() , resultUri);
                    getTextFromImage(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void getTextFromImage(Bitmap bitmap){
        TextRecognizer recognizer = new TextRecognizer.Builder(this).build();
        if(!recognizer.isOperational()){
            Toast.makeText(MainActivity.this, "Error Occurred" , Toast.LENGTH_SHORT).show();
        }
        else{
            Frame frame= new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> textBlockSparseArray = recognizer.detect(frame);
            StringBuilder stringBuilder =new StringBuilder();
            for(int i=0; i<textBlockSparseArray.size();i++){
                TextBlock textBlock = textBlockSparseArray.valueAt(i);
                stringBuilder.append(textBlock.getValue());
                stringBuilder.append("\n");

            }
            textdata.setText(stringBuilder.toString());
            but_capture.setText("Retake");
            but_copy.setVisibility(View.VISIBLE);
            btn_Read.setVisibility(View.VISIBLE);
        }

    }
    private void copytoClipBoard (String text){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied data", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(MainActivity.this,"Copied to clipboard!", Toast.LENGTH_SHORT).show();
    }
    
    //read 
    private void speak(){
        String readText = textdata.getText().toString();
        mTTS.speak(readText, TextToSpeech.QUEUE_FLUSH,null);
    }
   
}
