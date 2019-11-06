package com.parthjadav.nfcreaderexample.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.parthjadav.nfcreaderexample.ImageCaptureManager;
import com.parthjadav.nfcreaderexample.PreferenceManager;
import com.parthjadav.nfcreaderexample.R;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReportProblemActivity extends AppCompatActivity {
    @BindView(R.id.imgPhoto)
    ImageView imgPhoto;
    @BindView(R.id.edtDescription)
    EditText edtDescription;
    @BindView(R.id.tvRow)
    TextView tvRow;
    Bundle savedInstanceState;
    String pictureFilePath;
    String pictureFile;
    String tag, row;
    private ImageCaptureManager imageCaptureManager;
    PreferenceManager preferenceManager;

    public static String getFileNameFromURL(String url) {
        if (url == null) {
            return "";
        }
        try {
            URL resource = new URL(url);
            String host = resource.getHost();
            if (host.length() > 0 && url.endsWith(host)) {
                // handle ...example.com
                return "";
            }
        } catch (MalformedURLException e) {
            return "";
        }

        int startIndex = url.lastIndexOf('/') + 1;
        int length = url.length();

        // find end index for ?
        int lastQMPos = url.lastIndexOf('?');
        if (lastQMPos == -1) {
            lastQMPos = length;
        }

        // find end index for #
        int lastHashPos = url.lastIndexOf('#');
        if (lastHashPos == -1) {
            lastHashPos = length;
        }

        // calculate the end index
        int endIndex = Math.min(lastQMPos, lastHashPos);
        return url.substring(startIndex, endIndex);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_problem);
        ButterKnife.bind(this);
        this.savedInstanceState = savedInstanceState;
        preferenceManager = new PreferenceManager(this);
        imageCaptureManager = new ImageCaptureManager(this);
        tag = getIntent().getStringExtra("tag");
        row = getIntent().getStringExtra("row");
        tvRow.setText(row + " - " + tag);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Report Problem");
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        imageCaptureManager.onSaveInstanceState(savedInstanceState);
        imageCaptureManager.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.imgCaptureCamera)
    void imgCaptureCamera() {
        showDialog();
    }

    @OnClick(R.id.btnDiscard)
    void btnDiscard() {
        finish();
    }

    @OnClick(R.id.btnSave)
    void btnSave() {
        if (!edtDescription.getText().toString().isEmpty() || !pictureFilePath.isEmpty()) {
            preferenceManager.setKeyValueString("imagePath", pictureFilePath);
            preferenceManager.setKeyValueString("description", edtDescription.getText().toString());
            preferenceManager.setKeyValueBoolean("report",true);
        }
        /*Intent intent = new Intent();
        intent.putExtra("imagePath", pictureFilePath);
        intent.putExtra("description", edtDescription.getText().toString());
        setResult(153, intent);*/
        finish();
    }

    public void showDialog() {
        Dexter.withActivity(ReportProblemActivity.this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {

                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            cameraIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            cameraIntent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
                            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(cameraIntent, 111);

                                File pictureFile = null;
                                try {
                                    pictureFile = getPictureFile();
                                } catch (IOException ex) {
                                    Toast.makeText(ReportProblemActivity.this,
                                            "Photo file can't be created, please try again",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (pictureFile != null) {
                                    Uri photoURI = FileProvider.getUriForFile(ReportProblemActivity.this,
                                            "com.parthjadav.nfcreaderexample.fileprovider",
                                            pictureFile);
                                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                    startActivityForResult(cameraIntent, 111);
                                }
                            }

                            /*try {
                                Intent intent = imageCaptureManager.dispatchTakePictureIntent();
                                startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_PHOTO);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }*/
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        showPermissionRationale(token);
                    }
                }).check();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 111 && resultCode == RESULT_OK) {
            File imgFile = new File(pictureFilePath);
            if (imgFile.exists()) {
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(imgFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //imgPhoto.setImageBitmap(bitmap);
                imgPhoto.setImageURI(Uri.fromFile(imgFile));
            }
        }
    }

    @OnClick(R.id.imgPhoto)
    void imgPhoto() {
        if (!pictureFilePath.isEmpty()) {
            File file = new File(pictureFilePath);
            Log.e("*** Path - ", file.getAbsolutePath());
            openFile(file);
        }
    }

    private File getPictureFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        pictureFile = "Scanner_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile, ".jpg", storageDir);
        pictureFilePath = image.getAbsolutePath();
        return image;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void showPermissionRationale(final PermissionToken token) {
        new AlertDialog.Builder(this).setTitle("Permission")
                .setMessage("Please allow us to access photos, media and file on you device ?")
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        token.cancelPermissionRequest();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        token.continuePermissionRequest();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        token.cancelPermissionRequest();
                    }
                })
                .show();
    }

    private void scanFile(String path) {
        MediaScannerConnection.scanFile(ReportProblemActivity.this,
                new String[]{path}, null,
                (path1, uri) -> Log.d("Tag", "Scan finished. You can view the image in the gallery now."));
    }

    private void refreshGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(pictureFilePath);
        Uri picUri = Uri.fromFile(f);
        galleryIntent.setData(picUri);
        this.sendBroadcast(galleryIntent);
    }

    private void openFile(File url) {

        try {
            /*Uri uri = FileProvider.getUriForFile(
                    ReportProblemActivity.this,
                    BuildConfig.APPLICATION_ID + ".provider", url);*/
            Uri uri = Uri.fromFile(url);
            Log.e("**** URI - ", uri.toString());

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
                // Word document
                intent.setDataAndType(uri, "application/msword");
            } else if (url.toString().contains(".pdf")) {
                // PDF file
                intent.setDataAndType(uri, "application/pdf");
            } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
                // Powerpoint file
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
            } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
                // Excel file
                intent.setDataAndType(uri, "application/vnd.ms-excel");
            } else if (url.toString().contains(".zip")) {
                // ZIP file
                intent.setDataAndType(uri, "application/zip");
            } else if (url.toString().contains(".rar")) {
                // RAR file
                intent.setDataAndType(uri, "application/x-rar-compressed");
            } else if (url.toString().contains(".rtf")) {
                // RTF file
                intent.setDataAndType(uri, "application/rtf");
            } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
                // WAV audio file
                intent.setDataAndType(uri, "audio/x-wav");
            } else if (url.toString().contains(".gif")) {
                // GIF file
                intent.setDataAndType(uri, "image/gif");
            } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png") ||
                    url.toString().contains(".JPG") || url.toString().contains(".JPEG") || url.toString().contains(".PNG")) {
                // JPG file
                intent.setDataAndType(uri, "image/jpeg");
            } else if (url.toString().contains(".txt")) {
                // Text file
                intent.setDataAndType(uri, "text/plain");
            } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") ||
                    url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
                // Video files
                intent.setDataAndType(uri, "video/*");
            } else {
                intent.setDataAndType(uri, "*/*");
            }

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No application found which can open the file", Toast.LENGTH_SHORT).show();
        }
    }
}
