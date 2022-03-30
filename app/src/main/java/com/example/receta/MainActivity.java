package com.example.receta;


import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private TextView tvName, tvDescription, tvAge, tvDate, tvWeight;
    private Button btnSave, btnForm, btnShare;
    private LinearLayout linear;
    private Bitmap bitmap;

    private Toast msj;
    private String path = null;
    private String name = "";
    private String description = "";
    private String age = "";
    private String date = "";
    private String weight = "";

    private String namePDF = null;

    private UiModeManager uiModeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_NO);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        tvName = findViewById(R.id.tvName);
        tvAge = findViewById(R.id.tvAge);
        tvDate = findViewById(R.id.tvDate);
        tvWeight = findViewById(R.id.tvWeight);
        tvDescription = findViewById(R.id.tvDescription);
        linear = findViewById(R.id.llContainer);
        btnSave = findViewById(R.id.btnSave);
        btnForm = findViewById(R.id.btnForm);
        btnShare = findViewById(R.id.btnShare);
        btnShare.setEnabled(false);

        validarPermisos();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("size", "" + linear.getWidth() + " " + linear.getWidth());
                bitmap = LoadBitmap(linear, linear.getWidth(), linear.getHeight());
                createPdf();
                btnShare.setEnabled(true);
            }
        });

        btnForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogForm();
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(namePDF != null) {

                    String carpeta = "Recetas";

                    String ExportPath = Environment.getExternalStorageDirectory() + File.separator + carpeta + File.separator + namePDF;

                    Intent share = new Intent();
                    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    share.setAction(Intent.ACTION_SEND);
                    share.setType("application/pdf");
                    share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + ExportPath));
                    share.setPackage("com.whatsapp");

                    startActivity(share);

                    btnShare.setEnabled(false);
                }
            }
        });
    }

    private Bitmap LoadBitmap(View v, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }

    private void createPdf() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        //  Display display = wm.getDefaultDisplay();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        float hight = displaymetrics.heightPixels;
        float width = displaymetrics.widthPixels;

        int convertHighet = (int) hight, convertWidth = (int) width;

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(convertWidth, convertHighet, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        canvas.drawPaint(paint);

        bitmap = Bitmap.createScaledBitmap(bitmap, convertWidth, convertHighet, true);

        paint.setColor(Color.BLUE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);

        // write the document content
        String carpeta = "Recetas";
        File fileJson = new File(Environment.getExternalStorageDirectory(), carpeta);
        boolean isCreada = fileJson.exists();
        //String namePDF = "";

        if (isCreada == false) {

            isCreada = fileJson.mkdir();
            Log.w("existe", "no");
            if (isCreada) {
                Toast.makeText(getApplicationContext(), "Carpeta creada : " + fileJson.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            } else {
                Log.w("existe2", "no");
            }
            Log.d("carpeta creada", fileJson.getAbsolutePath());
        }

        if (isCreada == true) {

            Log.w("existe", "si");
            namePDF = "receta_"+name+"_"+date+".pdf";

        }

        path = Environment.getExternalStorageDirectory() + File.separator + carpeta + File.separator + namePDF;

        //String targetPdf = "/sdcard/page.pdf";
        File filePath;
        filePath = new File(path);
        try {
            document.writeTo(new FileOutputStream(filePath));

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "error: " + e.toString(), Toast.LENGTH_LONG).show();
            Log.w("path", path);
            Log.w("error", e.toString());
        }////////////////////

        // close the document
        document.close();
        Toast.makeText(this, "Se Guardo correctamente", Toast.LENGTH_SHORT).show();

    }

    private boolean validarPermisos() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

            return true;

        }
        if ((checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {

            return true;

        }

        if ((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) || (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE))) {

            cargardialogo();

        } else {

            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 100);

        }

        return false;

    }

    private void cargardialogo() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Permisos Desactivados");
        builder.setMessage("Debe aceptar los permisos para el correcto funcionamiento de la App");

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 100);
                }

            }
        });
        builder.show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {

            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {


            } else {

                cargardialogo2();

            }

        }

    }

    private void cargardialogo2() {

        final CharSequence[] op = {"si", "no"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Desea configurar los permisos manualmente?");
        builder.setItems(op, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (op[which].equals("si")) {

                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);

                } else {

                    msj = Toast.makeText(MainActivity.this, "los permisos no fueron aceptados", Toast.LENGTH_LONG);
                    msj.show();
                    dialog.dismiss();

                }

            }
        });
        builder.show();

    }

    public void dialogForm(){

        LinearLayout llCon = new LinearLayout(MainActivity.this);
        //***********************************************************************************
        LinearLayout llCreatedDialog = new LinearLayout(MainActivity.this);
        LinearLayout.LayoutParams paramsDialog = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        paramsDialog.setMargins(20, 10, 20, 10);
        llCreatedDialog.setLayoutParams(paramsDialog);
        llCreatedDialog.setOrientation(LinearLayout.VERTICAL);
        llCreatedDialog.setPadding(10, 10, 10, 10);
        llCon.addView(llCreatedDialog);

        //***********************EditTexts********************************
        TextView tvDialogName = new TextView(MainActivity.this);
        LinearLayout.LayoutParams paramsDialogName = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsDialogName.setMargins(0, 30, 0, 0);
        tvDialogName.setLayoutParams(paramsDialogName);
        tvDialogName.setText("Nombre:");
        llCreatedDialog.addView(tvDialogName);

        EditText etName = new EditText(MainActivity.this);
        LinearLayout.LayoutParams paramsName = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsName.setMargins(0, 30, 0, 0);
        etName.setLayoutParams(paramsName);
        etName.setText(name);
        llCreatedDialog.addView(etName);

        TextView tvDialogAge = new TextView(MainActivity.this);
        LinearLayout.LayoutParams paramsDialogAge = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsDialogAge.setMargins(0, 30, 0, 0);
        tvDialogAge.setLayoutParams(paramsDialogAge);
        tvDialogAge.setText("Edad:");
        tvDialogAge.setVisibility(View.GONE);
        llCreatedDialog.addView(tvDialogAge);

        EditText etAge = new EditText(MainActivity.this);
        LinearLayout.LayoutParams paramsAge = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsAge.setMargins(0, 30, 0, 0);
        etAge.setLayoutParams(paramsAge);
        etAge.setText(age);
        etAge.setVisibility(View.GONE);
        llCreatedDialog.addView(etAge);

        TextView tvDialogDate = new TextView(MainActivity.this);
        LinearLayout.LayoutParams paramsDialogDate = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsDialogDate.setMargins(0, 30, 0, 0);
        tvDialogDate.setLayoutParams(paramsDialogDate);
        tvDialogDate.setText("Fecha:");
        llCreatedDialog.addView(tvDialogDate);

        EditText etDate = new EditText(MainActivity.this);
        LinearLayout.LayoutParams paramsDate = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsDate.setMargins(0, 30, 0, 0);
        etDate.setLayoutParams(paramsDate);
        etDate.setText(date);
        llCreatedDialog.addView(etDate);

        TextView tvDialogWeight = new TextView(MainActivity.this);
        LinearLayout.LayoutParams paramsDialogWeight = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsDialogWeight.setMargins(0, 30, 0, 0);
        tvDialogWeight.setLayoutParams(paramsDialogWeight);
        tvDialogWeight.setText("Peso:");
        tvDialogWeight.setVisibility(View.GONE);
        llCreatedDialog.addView(tvDialogWeight);

        EditText etWeight = new EditText(MainActivity.this);
        LinearLayout.LayoutParams paramsWeight = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsWeight.setMargins(0, 30, 0, 0);
        etWeight.setLayoutParams(paramsWeight);
        etWeight.setText(weight);
        etWeight.setVisibility(View.GONE);
        llCreatedDialog.addView(etWeight);

        TextView tvDialogDescription = new TextView(MainActivity.this);
        LinearLayout.LayoutParams paramsDialogDescription = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsDialogDescription.setMargins(0, 30, 0, 0);
        tvDialogDescription.setLayoutParams(paramsDialogDescription);
        tvDialogDescription.setText("Description:");
        llCreatedDialog.addView(tvDialogDescription);

        EditText etDescription = new EditText(MainActivity.this);
        LinearLayout.LayoutParams paramsDescription = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsDescription.setMargins(0, 30, 0, 0);
        etDescription.setSingleLine(false);
        etDescription.setLayoutParams(paramsDescription);
        etDescription.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        etDescription.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        etDescription.setLines(1);
        etDescription.setMaxLines(6);
        etDescription.setVerticalScrollBarEnabled(true);
        etDescription.setMovementMethod(ScrollingMovementMethod.getInstance());
        etDescription.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        etDescription.setText(description);
        llCreatedDialog.addView(etDescription);
        
        //***********************BUTTONS********************************
        Button btnClear = new Button(MainActivity.this);
        LinearLayout.LayoutParams paramsClear = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsClear.setMargins(0, 30, 0, 0);
        btnClear.setLayoutParams(paramsClear);
        btnClear.setBackground(getDrawable(R.color.blue));
        btnClear.setText("Limpiar");
        btnClear.setTextColor(getResources().getColor(R.color.white));
        llCreatedDialog.addView(btnClear);

        Button btnAccept = new Button(MainActivity.this);
        LinearLayout.LayoutParams paramsAccept = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsAccept.setMargins(0, 30, 0, 0);
        btnAccept.setLayoutParams(paramsAccept);
        btnAccept.setBackground(getDrawable(R.color.blue));
        btnAccept.setText("Aceptar");
        btnAccept.setTextColor(getResources().getColor(R.color.white));
        llCreatedDialog.addView(btnAccept);

        final AlertDialog formDialog = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen).create();

        formDialog.setTitle("Formulario de Receta");

        formDialog.setView(llCon);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                name = "";
                age = "";
                date = "";
                weight = "";
                description = "";

                etName.setText(name);
                etAge.setText(age);
                etDate.setText(date);
                etWeight.setText(weight);
                etDescription.setText(description);

                tvName.setText(name);
                tvAge.setText(age);
                tvDate.setText(date);
                tvWeight.setText(weight);
                tvDescription.setText(description);

            }


        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                name = etName.getText().toString();
                age = etAge.getText().toString();
                date = etDate.getText().toString();
                weight = etWeight.getText().toString();
                description = etDescription.getText().toString();

                tvName.setText(name);
                tvAge.setText(age);
                tvDate.setText(date);
                tvWeight.setText(weight);
                tvDescription.setText(description);

                formDialog.dismiss();

            }

        });

        formDialog.show();

    }
}