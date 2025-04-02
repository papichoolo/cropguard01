package com.example.cropguard;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class TFLiteHelper {
    private final Context context;
    private Interpreter interpreter;

    public TFLiteHelper(Context context) {
        this.context = context;
    }

    public void loadModel(String modelPath) throws IOException {
        try {
            Interpreter.Options options = new Interpreter.Options();
            interpreter = new Interpreter(loadModelFile(modelPath), options);
        } catch (IOException e) {
            Log.e("TFLiteHelper", "Error loading model: " + modelPath, e);
            throw e;
        }
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    private MappedByteBuffer loadModelFile(String modelPath) throws IOException {
        try (AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
             FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }
}