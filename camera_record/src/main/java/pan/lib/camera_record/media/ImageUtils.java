package pan.lib.camera_record.media;

/**
 * @author pan qi
 * @since 2024/2/3
 */

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;
import android.util.Size;

import androidx.camera.core.ImageProxy;

import java.nio.ByteBuffer;

public class ImageUtils {
    private static final String TAG = "ImageUtils";
    private static ByteBuffer planeBuffer;
    private static byte[] scaleBytes;
    private static byte[] result;


    private static ByteBuffer yuv420;

    public static byte[] getBytes(ImageProxy image, int rotationDegrees, int width, int height) {
        int format = image.getFormat();
        if (format != ImageFormat.YUV_420_888) {
            // https://developer.android.google.cn/training/camerax/analyze
            throw new IllegalStateException("not support image format!");
        }

        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        int size = image.getWidth() * image.getHeight() * 3 / 2;

        // 避免内存抖动
        if (yuv420 == null || yuv420.capacity() < size) {
            yuv420 = ByteBuffer.allocate(size);
        }
        yuv420.position(0);



        /**
         * Y 数据
         */
        int pixelStride = planes[0].getPixelStride();  // Y 只会是1
        ByteBuffer yBuffer = planes[0].getBuffer();
        int rowStride = planes[0].getRowStride();

        // 1. rowStride == width,  则是一个空数组
        // 2. rowStride > width, 是每行为了字节对齐，多出来的空字节
        byte[] skipRow = new byte[rowStride - image.getWidth()];

        byte[] row = new byte[image.getWidth()];
        for (int i = 0; i < image.getHeight(); i++) {
            yBuffer.get(row);
            yuv420.put(row);

            // 最后一行因为后面是 U数据，后面没有无效数据，无需跳过
            if (i < image.getHeight() - 1) {
                yBuffer.get(skipRow);
            }
        }


        try {

            /**
             *  U, V数据
             */
            for (int i = 1; i < 3; i++) {
                ImageProxy.PlaneProxy plane = planes[i];
                pixelStride = plane.getPixelStride();
                rowStride = plane.getRowStride();
                planeBuffer = plane.getBuffer();

//                Log.e(TAG, "planeBuffer size: " + planeBuffer.array().length);

                int uvWidth = image.getWidth() / 2;
                int uvHeight = image.getHeight() / 2;

                Log.e(TAG, "i = " + i + " uvHeight:" + uvHeight + ", rowStride: " + rowStride);

                for (int j = 0; j < uvHeight; j++) {
                    for (int k = 0; k < rowStride; k++) {
                        if (j == uvHeight - 1) {   // 单独处理最后一行
                            if (pixelStride == 1) { // uv没混合
                                if (k >= uvWidth) break;
                            } else if (pixelStride == 2) { // uv混合
                                if (k >= image.getWidth()) break;
                            }
                        }
                        if (planeBuffer.remaining() == 0) {
                            // 在 pos: 153599, j = 239, k = 639 时，get会越界。 此处防止越界
                            break;
                        }

                        byte b = planeBuffer.get();
                        if (pixelStride == 1) {
                            if (k < uvWidth) {
                                yuv420.put(b);
                            }
                        } else if (pixelStride == 2) {
                            // 1. 偶数位下标是我们要的U/V数据
                            // 2. 丢弃无效占位数据
                            if (k < image.getWidth() && k % 2 == 0) {
                                yuv420.put(b);
                            }
                        }
                    }
                }
            }


            int srcWidth = image.getWidth();
            int srcHeight = image.getHeight();
            result = yuv420.array();

            if (rotationDegrees == 90 || rotationDegrees == 270) {
//                rotation(result, image.getWidth(), image.getHeight(), rotationDegrees);
                srcWidth = image.getWidth();
                srcHeight = image.getHeight();
            }

            if (srcWidth != width || srcHeight != height) {
                Log.e(TAG, "scale bytes, srcWidth = " + srcWidth + ", srcHeight = " + srcHeight + ", width = " + width + ", height = " + height);
                // 调整scaleBytes, 避免内存抖动
                int scaleSize = width * height * 3 / 2;
                if (scaleBytes == null || scaleBytes.length < scaleSize) {
                    scaleBytes = new byte[scaleSize];
                }
//                scale(result, scaleBytes, srcWidth, srcHeight, width, height);
                return scaleBytes;
            }
        } catch (Exception e) {
            Log.e(TAG, "BufferUnderflowException pos: " + planeBuffer.position());
        }

        return result;
    }


    public static Size[] getSupportSize(Context context) {
        CameraManager cm = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cm.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cm.getCameraCharacteristics(cameraId);
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map != null) {
                    return map.getOutputSizes(ImageFormat.YUV_420_888);
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

//    private static native void rotation(byte[] data, int width, int height, int degrees);
//
//    private static native void scale(byte[] src, byte[] dst, int srcWidth, int srcHeight, int dstWidth, int dstHeight);
}