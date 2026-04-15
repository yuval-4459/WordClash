package com.example.wordclash.screens;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.wordclash.R;
import com.example.wordclash.models.User;
import com.example.wordclash.services.DatabaseService;
import com.example.wordclash.utils.SharedPreferencesUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

public class ProfilePictureActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private ImageView ivProfilePicture;
    private TextView tvUserInitial;
    private Button btnDeletePicture;
    private Button btnRotate;
    private User currentUser;
    private Uri photoUri;
    private Bitmap currentBitmap;
    private int currentRotation = 0;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    handleImageSelected(uri);
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (photoUri != null) {
                    try {
                        InputStream test = getContentResolver().openInputStream(photoUri);
                        if (test != null) {
                            test.close();
                            handleImageSelected(photoUri);
                        }
                    } catch (Exception e) {
                        if (success) {
                            handleImageSelected(photoUri);
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_picture);

        currentUser = SharedPreferencesUtils.getUser(this);
        if (currentUser == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        displayCurrentPicture();
    }

    private void initializeViews() {
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        tvUserInitial = findViewById(R.id.tvUserInitial);
        TextView tvUserName = findViewById(R.id.tvUserName);
        Button btnChooseFromGallery = findViewById(R.id.btnChooseFromGallery);
        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnDeletePicture = findViewById(R.id.btnDeletePicture);
        btnRotate = findViewById(R.id.btnRotate);
        Button btnBack = findViewById(R.id.btnBack);

        tvUserName.setText(currentUser.getUserName());

        btnChooseFromGallery.setOnClickListener(v -> openGallery());
        btnTakePhoto.setOnClickListener(v -> checkCameraPermissionAndTakePhoto());
        btnDeletePicture.setOnClickListener(v -> confirmDeletePicture());
        btnRotate.setOnClickListener(v -> rotateImage());
        btnBack.setOnClickListener(v -> finish());
    }

    private void displayCurrentPicture() {
        String profilePictureUrl = currentUser.getProfilePictureUrl();

        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
            ivProfilePicture.setVisibility(ImageView.VISIBLE);
            tvUserInitial.setVisibility(TextView.GONE);

            try {
                byte[] decodedString = Base64.decode(profilePictureUrl, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                currentBitmap = decodedByte;
                ivProfilePicture.setImageBitmap(decodedByte);
            } catch (Exception e) {
                showDefaultAvatar();
            }

            btnDeletePicture.setVisibility(Button.VISIBLE);
            btnDeletePicture.setEnabled(true);
            btnDeletePicture.setAlpha(1.0f);
            showRotationControls();
        } else {
            showDefaultAvatar();
            btnDeletePicture.setVisibility(Button.GONE);
            hideRotationControls();
        }
    }

    private void showDefaultAvatar() {
        ivProfilePicture.setVisibility(ImageView.GONE);
        tvUserInitial.setVisibility(TextView.VISIBLE);

        String initial = "";
        if (currentUser.getUserName() != null && !currentUser.getUserName().isEmpty()) {
            initial = String.valueOf(currentUser.getUserName().charAt(0)).toUpperCase();
        }
        tvUserInitial.setText(initial);
    }

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private void checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        try {
            File photoFile = new File(getCacheDir(),
                    "profile_picture_" + System.currentTimeMillis() + ".jpg");
            photoFile.createNewFile();
            photoUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    photoFile);
            cameraLauncher.launch(photoUri);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleImageSelected(Uri uri) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try (InputStream sizeStream = getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(sizeStream, null, options);
            }

            options.inSampleSize = calculateInSampleSize(options, 512, 512);
            options.inJustDecodeBounds = false;

            Bitmap sampledBitmap;
            try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                sampledBitmap = BitmapFactory.decodeStream(inputStream, null, options);
            }

            if (sampledBitmap == null) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap orientedBitmap;
            try (InputStream exifStream = getContentResolver().openInputStream(uri)) {
                orientedBitmap = fixImageOrientation(exifStream, sampledBitmap);
            }

            Bitmap resized = resizeBitmap(orientedBitmap, 512, 512);
            if (resized != orientedBitmap) orientedBitmap.recycle();

            if (currentBitmap != null) currentBitmap.recycle();
            currentBitmap = resized;
            currentRotation = 0;

            String base64Image = bitmapToBase64(currentBitmap);
            currentUser.setProfilePictureUrl(base64Image);
            saveProfilePicture();

        } catch (OutOfMemoryError e) {
            Toast.makeText(this, "Image too large, please choose a smaller one", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap fixImageOrientation(InputStream input, Bitmap bitmap) {
        try {
            ExifInterface exif = new ExifInterface(input);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setScale(1, -1);
                    break;
                default:
                    return bitmap;
            }

            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return rotated;
        } catch (Exception e) {
            return bitmap;
        }
    }

    private void rotateImage() {
        if (currentBitmap == null) {
            Toast.makeText(this, "No image to rotate", Toast.LENGTH_SHORT).show();
            return;
        }

        currentRotation = (currentRotation + 90) % 360;

        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        Bitmap rotated = Bitmap.createBitmap(currentBitmap, 0, 0,
                currentBitmap.getWidth(), currentBitmap.getHeight(), matrix, true);

        currentBitmap.recycle();
        currentBitmap = rotated;

        displayBitmap(currentBitmap);

        String base64Image = bitmapToBase64(currentBitmap);
        currentUser.setProfilePictureUrl(base64Image);
        saveProfilePicture();
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private void displayBitmap(Bitmap bitmap) {
        ivProfilePicture.setVisibility(ImageView.VISIBLE);
        tvUserInitial.setVisibility(TextView.GONE);
        ivProfilePicture.setImageBitmap(bitmap);
    }

    private void showRotationControls() {
        btnRotate.setVisibility(Button.VISIBLE);
        btnRotate.setEnabled(true);
        btnRotate.setAlpha(1.0f);
    }

    private void hideRotationControls() {
        btnRotate.setVisibility(Button.GONE);
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale = Math.min(
                (float) maxWidth / width,
                (float) maxHeight / height
        );

        if (scale < 1.0f) {
            int newWidth = Math.round(width * scale);
            int newHeight = Math.round(height * scale);
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }

        return bitmap;
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void saveProfilePicture() {
        DatabaseService.getInstance().updateUser(currentUser, new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Void unused) {
                SharedPreferencesUtils.saveUser(ProfilePictureActivity.this, currentUser);
                Toast.makeText(ProfilePictureActivity.this,
                        "Profile picture updated!",
                        Toast.LENGTH_SHORT).show();
                displayCurrentPicture();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(ProfilePictureActivity.this,
                        "Failed to update: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeletePicture() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Profile Picture")
                .setMessage("Are you sure you want to remove your profile picture?")
                .setPositiveButton("Delete", (dialog, which) -> deletePicture())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePicture() {
        currentUser.setProfilePictureUrl(null);

        DatabaseService.getInstance().updateUser(currentUser, new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Void unused) {
                SharedPreferencesUtils.saveUser(ProfilePictureActivity.this, currentUser);
                Toast.makeText(ProfilePictureActivity.this,
                        "Profile picture removed",
                        Toast.LENGTH_SHORT).show();
                displayCurrentPicture();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(ProfilePictureActivity.this,
                        "Failed to delete: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}