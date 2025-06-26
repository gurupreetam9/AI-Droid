package com.example.ai_droid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Size;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaceExpression extends AppCompatActivity {

    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private Interpreter tflite;
    private int inputSize = 48;
    private static final int CAMERA_REQUEST_CODE = 100;

    private ImageAnalysis imageAnalysis;
    private ProcessCameraProvider cameraProvider;

    private long lastToastTime = 0;
    private final int toastCooldown = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_layout);

        previewView = findViewById(R.id.previewView);
        cameraExecutor = Executors.newFixedThreadPool(2);

        loadModel(); // Load model once

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setTargetResolution(new Size(640, 480))
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    analyzeImage(image);
                });

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void loadModel() {
        try {
            ByteBuffer buffer = loadModelFile("Face_Expression1.tflite");
            tflite = new Interpreter(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ByteBuffer loadModelFile(String modelPath) throws IOException {
        FileInputStream fis = new FileInputStream(getAssets().openFd(modelPath).getFileDescriptor());
        FileChannel fileChannel = fis.getChannel();
        long startOffset = getAssets().openFd(modelPath).getStartOffset();
        long declaredLength = getAssets().openFd(modelPath).getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void analyzeImage(ImageProxy image) {
        if (tflite == null) return;

        Bitmap bitmap = convertYUVToBitmap(image);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true);
        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(resizedBitmap);

        float[] inputData = new float[inputSize * inputSize];
        int[] pixels = new int[inputSize * inputSize];
        resizedBitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize);

        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int red = (pixel >> 16) & 0xFF;
            inputData[i] = red / 255.0f; // Grayscale normalization
        }

        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(inputSize * inputSize * 4);
        inputBuffer.order(java.nio.ByteOrder.nativeOrder()); // Ensure correct byte order
        inputBuffer.clear();

        for (float value : inputData) {
            inputBuffer.putFloat(value);
        }

        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(new int[]{1, 7}, DataType.FLOAT32);
        tflite.run(inputBuffer, outputBuffer.getBuffer());

        float[] results = outputBuffer.getFloatArray();
        displayEmotion(getMaxIndex(results));

        image.close(); // Ensure image is closed
    }

    private Bitmap convertYUVToBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        buffer.rewind();
        Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixelValue = buffer.get() & 0xFF;
                int grayColor = (0xFF << 24) | (pixelValue << 16) | (pixelValue << 8) | pixelValue;
                bitmap.setPixel(x, y, grayColor);
            }
        }
        return bitmap;
    }

    private int getMaxIndex(float[] arr) {
        int maxIndex = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > arr[maxIndex]) maxIndex = i;
        }
        return maxIndex;
    }

    private void displayEmotion(int emotionIndex) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastToastTime < toastCooldown) return;
        lastToastTime = currentTime;

        String[] emotions = {"Angry", "Disgust", "Fear", "Happy", "Neutral", "Sad", "Surprise"};
        runOnUiThread(() -> Toast.makeText(FaceExpression.this, "Detected: " + emotions[emotionIndex], Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (cameraProvider != null) cameraProvider.unbindAll();
    }
}
