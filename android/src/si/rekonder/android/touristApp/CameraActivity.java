package si.rekonder.android.touristApp;

import android.content.Context;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.Button;
import android.location.Location;
import android.view.View;
import android.content.Intent;
import android.location.LocationListener;

import android.location.LocationManager;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

public class CameraActivity extends Activity {

    public static final String LOG_TAG = "NativeCamera";

    private CameraView preview;

    private RelativeLayout layout;

    private LocationManager mlocManager;

    private LocationListener mlocListener;

    private  int detected;

    private Button btn;

    private int my_counter;

    static {

        System.loadLibrary("NativeCamera");

    }
    /*
    * Convert image to bitmap and store image into native vector
    * */
    public void addImages(Drawable d, double lat, double lon) {
        Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();
        processImage(bitmapdata, bitmap.getWidth(), bitmap.getHeight(), lat, lon);
    }

    /*
    * Load all possible images and its location to vector.
    * Create layout
    * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeNative(getResources().getAssets());
        Resources resources = this.getResources();
        Drawable d = resources.getDrawable(R.drawable.univerza);
        addImages(d, 46.049275, 14.504125);
        d = resources.getDrawable(R.drawable.zvezda);
        addImages(d, 46.050223, 14.503778);
        d = resources.getDrawable(R.drawable.vodnik);
        addImages(d, 46.050771, 14.509509);
        d = resources.getDrawable(R.drawable.grad);
        addImages(d, 46.049008, 14.508304);
        d = resources.getDrawable(R.drawable.zmajski);
        addImages(d, 46.051897, 14.510378);
        d = resources.getDrawable(R.drawable.nuk);
        addImages(d, 46.047647, 14.503988);
        d = resources.getDrawable(R.drawable.presern);
        addImages(d, 46.051440, 14.506244);
        d = resources.getDrawable(R.drawable.mestna);
        addImages(d, 46.049908, 14.506966);
        d = resources.getDrawable(R.drawable.franciskanska);
        addImages(d, 46.051668, 14.506063);
        d = resources.getDrawable(R.drawable.robbov);
        addImages(d, 46.050184, 14.506961);

        layout = new RelativeLayout(this);
        setContentView(layout);

    }

    /*
    * Imlement locationlistner for knowing gps location and set callback on every gps location change.
    * Callback call function which fill vector with possible buildings to detect.
    * */
    public class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            fillVector(loc.getLatitude(), loc.getLongitude());
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText( getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT ).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText( getApplicationContext(),"Gps Enabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

    }
    /*
    * Stop specifiic activities
    * */
    @Override
    public void onPause() {
        super.onPause();
        preview.setPreviewCallback(null);
        preview.stop();
        layout.removeView(preview);
        preview = null;

        mlocManager.removeUpdates(mlocListener);
        mlocManager = null;
        btn.setVisibility(View.GONE);
    }

    /*
    * Initialize gps, button and camera.
    * Initialize camera callback. On every callback call predisction function
    * and return prediction id. If prediction id is 1 or more show button on display with
    * right text.
    * Set button callback and save prediction id into our singelton class
    * */
    @Override
    public void onResume() {
        super.onResume();

        detected = -1;
        my_counter = 0;

        mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();
        mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);

        btn = new Button(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT );
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        btn.setLayoutParams(params);

        preview = new CameraView(this, 0, CameraView.LayoutMode.FitToParent);

        preview.setCameraReadyCallback(new CameraView.CameraReadyCallback() {
            public void onPreviewReady(CameraView p) {
                if (preview != p)
                    return;

                Camera.Size size = preview.getPreviewSize();

                preview.setPreviewCallback(new PreviewCallback() {
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        if (preview == null)
                            return;

                        Camera.Size size = preview.getPreviewSize();
                        detected = (int) CameraActivity
                                   .recognizeObject(data, size.width, size.height,
                                                    preview.getPreviewFormat());
                        my_counter += 1;

                        if(detected == 1) {
                            btn.setText(getResources().getString(R.string.univerza_name));
                        } else if(detected == 2) {
                            btn.setText(getResources().getString(R.string.zvezda_name));
                        } else if(detected == 3) {
                            btn.setText(getResources().getString(R.string.vodnik_name));
                        } else if(detected == 4) {
                            btn.setText(getResources().getString(R.string.grad_name));
                        } else if(detected == 5) {
                            btn.setText(getResources().getString(R.string.zmajski_name));
                        } else if(detected == 6) {
                            btn.setText(getResources().getString(R.string.nuk_name));
                        } else if(detected == 7) {
                            btn.setText(getResources().getString(R.string.presern_name));
                        } else if(detected == 8) {
                            btn.setText(getResources().getString(R.string.mestna_name));
                        } else if(detected == 9) {
                            btn.setText(getResources().getString(R.string.franciskanska_name));
                        } else if(detected == 10) {
                            btn.setText(getResources().getString(R.string.robbov_name));
                        }
                        if(detected > 0 ) {
                            btn.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    Intent inf = new Intent(CameraActivity.this, ChoosenImage.class);
                                    startActivity(inf);
                                }
                            });
                            Sigleton sin = (Sigleton) getApplicationContext();
                            sin.setData(detected);
                            btn.setVisibility(View.VISIBLE);
                            my_counter = 0;
                        }
                        if(detected < 1 && my_counter >= 12) {
                            my_counter = 0;
                            btn.setVisibility(View.INVISIBLE);
                        }
                    }
                });

            }
        });


        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        layout.addView(preview, 0, layoutParams);
        layout.addView(btn);

    }

    /*
    * Initialize functions to call from native
    * */
    public static native void processImage(byte[] buffer, int width,
                                           int height, double lat, double lon);

    public static native void initializeNative(Object assets);

    public static native Object detectFaces(byte[] buffer, int width,
                                            int height, int type);

    public static native int recognizeObject(byte[] buffer, int width, int height,
            int type);
    public static native int fillVector(double lan, double lon);

}
