package com.example.myapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ayush.steganography.Steganography;
import com.ayush.steganography.SteganographyException;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_PERMISSIONS = 123;

    private Button btnSelectImage;
    private Button btnSaveData;
    private ImageView imageView;
    private EditText editTextData;

    private Bitmap originalBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSaveData = findViewById(R.id.btnSaveData);
        imageView = findViewById(R.id.imageView);
        editTextData = findViewById(R.id.editTextData);

        checkAndRequestPermissions();

        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        btnSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = editTextData.getText().toString();
                if (originalBitmap == null) {
                    Toast.makeText(MainActivity.this, "Please select an image first", Toast.LENGTH_SHORT).show();
                } else if (data.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter data to save", Toast.LENGTH_SHORT).show();
                } else {
                    new EncodeTask(MainActivity.this).execute(data);
                }
            }
        });
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions denied. App may not function correctly.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imageView.setImageBitmap(originalBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class EncodeTask extends AsyncTask<String, Void, Bitmap> {
        private WeakReference<MainActivity> activityReference;
        private Exception exception = null;

        EncodeTask(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                return null;
            }

            String data = params[0];
            Bitmap encodedBitmap = null;
            try {
                Bitmap mutableBitmap = activity.originalBitmap.copy(activity.originalBitmap.getConfig(), true);
                encodedBitmap = Steganography.encode(mutableBitmap, data.getBytes());
            } catch (SteganographyException e) {
                exception = e;
                e.printStackTrace();
            }
            return encodedBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap encodedBitmap) {
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            if (exception != null) {
                Toast.makeText(activity, "Error encoding image: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            if (encodedBitmap != null) {
                File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "Steganography");

                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }

                String fileName = "encoded_" + System.currentTimeMillis() + ".png";
                File imageFile = new File(storageDir, fileName);

                try {
                    Steganography.bitmapToFile(encodedBitmap, imageFile);
                    Toast.makeText(activity, "Encoded image saved to " + imageFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    activity.imageView.setImageBitmap(encodedBitmap); // Show the new image
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(activity, "Failed to save image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity, "Encoding failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
