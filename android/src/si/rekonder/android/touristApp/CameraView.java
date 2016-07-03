package si.rekonder.android.touristApp;

/*
* Taken from si.vicos package and little modified
*
* */

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.PictureCallback;
import android.graphics.ImageFormat;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import android.widget.Toast;
/**
 * This class assumes the parent layout is RelativeLayout.LayoutParams.
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String CAMERA_PARAM_ORIENTATION = "orientation";
    private static final String CAMERA_PARAM_LANDSCAPE = "landscape";
    private static final String CAMERA_PARAM_PORTRAIT = "portrait";
    private Activity activity;
    private SurfaceHolder holder;
    private Camera camera;
    private List<Camera.Size> previewSizeList;
    private List<Camera.Size> pictureSizeList;
    private Camera.Size previewSize;
    private Camera.Size pictureSize;
    private int previewFormat, pictureFormat;
    private int surfaceChangedCallDepth = 0;
    private int cameraId;
    private LayoutMode layoutMode;

    private PreviewCallback previewCallback = null;
    private PictureCallback pictureCallback = null;
    private float previewScale = 1;
    private boolean takingPicture = false;

    private CameraReadyCallback cameraReadyCallback = null;

    public static enum LayoutMode {
        FitToParent, // Scale to the size that no side is larger than the parent
        NoBlank // Scale to the size that no side is smaller than the parent
    };

    public interface CameraReadyCallback {
        public void onPreviewReady(CameraView preview);
    }

    /**
     * State flag: true when surface's layout size is set and surfaceChanged()
     * process has not been completed.
     */
    protected boolean isSurfaceConfiguring = false;

    private Handler delayedStarted = new Handler();

    private PreviewCallback internalPreviewCallback = new PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {

            if (camera == null || previewCallback == null)
                return;

            if (previewCallback != null) {
                previewCallback.onPreviewFrame(data, camera);
                camera.setOneShotPreviewCallback(internalPreviewCallback);
            }

        }
    };

    public CameraView(Activity activity, int cameraId, LayoutMode mode) {
        super(activity); // Always necessary
        this.activity = activity;
        layoutMode = mode;
        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            if (Camera.getNumberOfCameras() > cameraId) {
                this.cameraId = cameraId;
            } else {
                this.cameraId = 0;
            }
        } else {
            this.cameraId = 0;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            this.camera = Camera.open(this.cameraId);
        } else {
            this.camera = Camera.open();
        }
        Camera.Parameters cameraParams = camera.getParameters();
        previewSizeList = cameraParams.getSupportedPreviewSizes();
        pictureSizeList = cameraParams.getSupportedPictureSizes();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            camera.release();
            camera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        surfaceChangedCallDepth++;
        doSurfaceChanged(width, height);
        surfaceChangedCallDepth--;
    }

    private void doSurfaceChanged(int width, int height) {
        camera.stopPreview();

        Camera.Parameters cameraParams = camera.getParameters();

        boolean portrait = isPortrait();
        this.pictureFormat = cameraParams.getPictureFormat();
        this.previewFormat = cameraParams.getPreviewFormat();

        // The code in this if-statement is prevented from executed again when
        // surfaceChanged is
        // called again due to the change of the layout size in this
        // if-statement.
        if (!isSurfaceConfiguring) {
            Camera.Size previewSize = determinePreviewSize(portrait, width,
                                      height);
            Camera.Size pictureSize = determinePictureSize(previewSize);
            this.previewSize = previewSize;
            this.pictureSize = pictureSize;

            isSurfaceConfiguring = adjustSurfaceLayoutSize(previewSize,
                                   portrait, width, height);

            if (isSurfaceConfiguring && (surfaceChangedCallDepth <= 1)) {
                return;
            }
        }

        configureCameraParameters(cameraParams, portrait);
        isSurfaceConfiguring = false;

        try {
            camera.startPreview();
        } catch (Exception e) {
            Log.w(CameraActivity.LOG_TAG,
                  "Failed to start preview: " + e.getMessage());

            // Remove failed size
            previewSizeList.remove(previewSize);
            this.previewSize = null;

            // Reconfigure
            if (previewSizeList.size() > 0) { // prevent infinite loop
                surfaceChanged(null, 0, width, height);
            } else {
                Toast.makeText(activity, "Cannot access camera",
                               Toast.LENGTH_LONG).show();
                Log.w(CameraActivity.LOG_TAG, "Unable to start camera preview");
            }
        }

        if (null != cameraReadyCallback) {
            cameraReadyCallback.onPreviewReady(this);
        }
    }

    protected Camera.Size determinePreviewSize(boolean portrait, int reqWidth,
            int reqHeight) {
        // Meaning of width and height is switched for preview when portrait,
        // while it is the same as user's view for surface and metrics.
        // That is, width must always be larger than height for setPreviewSize.
        int reqPreviewWidth; // requested width in terms of camera hardware
        int reqPreviewHeight; // requested height in terms of camera hardware
        if (portrait) {
            reqPreviewWidth = reqHeight;
            reqPreviewHeight = reqWidth;
        } else {
            reqPreviewWidth = reqWidth;
            reqPreviewHeight = reqHeight;
        }

        // Adjust surface size with the closest aspect-ratio
        float reqRatio = ((float) reqPreviewWidth) / reqPreviewHeight;
        float deltaRatioMin = Float.MAX_VALUE;
        Camera.Size retSize = null;
        for (Camera.Size size : previewSizeList) {
            float curRatio = ((float) size.width) / size.height;
            float deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }

        return retSize;
    }

    protected Camera.Size determinePictureSize(Camera.Size previewSize) {
        Camera.Size retSize = null;

        float reqRatio = ((float) previewSize.width) / previewSize.height;
        float deltaRatioMin = Float.MAX_VALUE;
        int pixelsMax = 0;
        for (Camera.Size size : pictureSizeList) {
            float curRatio = ((float) size.width) / size.height;
            float deltaRatio = Math.abs(reqRatio - curRatio);
            int pixels = size.width * size.height;
            if (deltaRatio < deltaRatioMin
                    || (deltaRatio == deltaRatioMin && pixels > pixelsMax)) {
                deltaRatioMin = deltaRatio;
                pixelsMax = pixels;
                retSize = size;
            }
        }

        return retSize;
    }

    protected boolean adjustSurfaceLayoutSize(Camera.Size previewSize,
            boolean portrait, int availableWidth, int availableHeight) {

        float tmpLayoutHeight, tmpLayoutWidth;
        if (portrait) {
            tmpLayoutHeight = previewSize.width;
            tmpLayoutWidth = previewSize.height;
        } else {
            tmpLayoutHeight = previewSize.height;
            tmpLayoutWidth = previewSize.width;
        }

        float factorHeight, factorWidth, factor;
        factorHeight = availableHeight / tmpLayoutHeight;
        factorWidth = availableWidth / tmpLayoutWidth;
        if (layoutMode == LayoutMode.FitToParent) {
            // Select smaller factor, because the surface cannot be set to the
            // size larger than display metrics.
            factor = Math.min(factorWidth, factorHeight);
        } else {
            factor = Math.max(factorWidth, factorHeight);
        }

        previewScale = factor;

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this
                .getLayoutParams();

        int layoutHeight = (int) (tmpLayoutHeight * factor);
        int layoutWidth = (int) (tmpLayoutWidth * factor);

        boolean layoutChanged;
        if ((layoutWidth != this.getWidth())
                || (layoutHeight != this.getHeight())) {
            layoutParams.height = layoutHeight;
            layoutParams.width = layoutWidth;
            this.setLayoutParams(layoutParams); // this will trigger another
            // surfaceChanged invocation.
            layoutChanged = true;
        } else {
            layoutChanged = false;
        }

        return layoutChanged;
    }

    protected void configureCameraParameters(Camera.Parameters cameraParams,
            boolean portrait) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) { // for 2.1 and
            // before
            if (portrait) {
                cameraParams.set(CAMERA_PARAM_ORIENTATION,
                                 CAMERA_PARAM_PORTRAIT);
            } else {
                cameraParams.set(CAMERA_PARAM_ORIENTATION,
                                 CAMERA_PARAM_LANDSCAPE);
            }
        } else { // for 2.2 and later
            int angle;
            Display display = activity.getWindowManager().getDefaultDisplay();
            switch (display.getRotation()) {
            case Surface.ROTATION_0: // This is display orientation
                angle = 90; // This is camera orientation
                break;
            case Surface.ROTATION_90:
                angle = 0;
                break;
            case Surface.ROTATION_180:
                angle = 270;
                break;
            case Surface.ROTATION_270:
                angle = 180;
                break;
            default:
                angle = 90;
                break;
            }

            camera.setDisplayOrientation(angle);
        }

        cameraParams.setPreviewSize(previewSize.width, previewSize.height);
        cameraParams.setPictureSize(pictureSize.width, pictureSize.height);

        camera.setParameters(cameraParams);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop();
    }

    public void stop() {
        if (null == camera) {
            return;
        }
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    public boolean isPortrait() {
        return (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
    }

    public void setPreviewCallback(PreviewCallback callback) {
        if (camera == null) {
            return;
        }
        previewCallback = callback;
        camera.setOneShotPreviewCallback(internalPreviewCallback);
    }


    public Camera.Size getPreviewSize() {

        return previewSize;

    }

    public int getPreviewFormat() {

        return previewFormat;

    }

    public int getPictureFormat() {

        return pictureFormat;

    }


    public void setCameraReadyCallback(CameraReadyCallback cb) {

        cameraReadyCallback = cb;

    }
}
