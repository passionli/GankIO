package com.liguang.imageloaderdemo.album;


import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.samples.zoomable.AnimatedZoomableController;
import com.facebook.samples.zoomable.ZoomableDraweeView;
import com.liguang.imageloaderdemo.ImageDetailActivity;
import com.liguang.imageloaderdemo.LGViewUtils;
import com.liguang.imageloaderdemo.R;

/**
 * This fragment will populate the children of the ViewPager from {@link ImageDetailActivity}.
 */
public class ImageDetailFragment extends Fragment {
    private static final String TAG = "ImageDetailFragment";
    private static final String IMAGE_DATA_EXTRA = "extra_image_data";
    private String mImageUrl;
    private ZoomableDraweeView mDraweeView;
    private int mImageWidth;
    private int mImageHeight;

    public ImageDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method to generate a new instance of the fragment given an image number.
     *
     * @param imageUrl The image url to load
     * @return A new instance of ImageDetailFragment with imageNum extras
     */
    public static ImageDetailFragment newInstance(String imageUrl) {
        final ImageDetailFragment f = new ImageDetailFragment();

        final Bundle args = new Bundle();
        args.putString(IMAGE_DATA_EXTRA, imageUrl);
        f.setArguments(args);

        return f;
    }

    /**
     * Populate image using a url from extras, use the convenience factory method
     * {@link ImageDetailFragment#newInstance(String)} to create this fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageUrl = getArguments() != null ? getArguments().getString(IMAGE_DATA_EXTRA) : null;
        mImageWidth = (LGViewUtils.getScreenWidth(getContext()) - LGViewUtils.dp2px(getContext(), 20));
        mImageHeight = (LGViewUtils.getScreenHeight(getContext()) - LGViewUtils.dp2px(getContext(), 64));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate and locate the main ImageView
        final View v = inflater.inflate(R.layout.fragment_image_detail, container, false);
        mDraweeView = (ZoomableDraweeView) v.findViewById(R.id.my_image_view);
        mDraweeView.setAllowTouchInterceptionWhileZoomed(false);
        mDraweeView.setTapListener(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // Pass clicks on the ImageView to the parent activity to handle
                if (View.OnClickListener.class.isInstance(getActivity()) && Utils.hasHoneycomb()) {
                    ((View.OnClickListener) getActivity()).onClick(mDraweeView);
                }
                return true;
            }


            @Override
            public boolean onDoubleTap(MotionEvent e) {
                AnimatedZoomableController zoomableController = (AnimatedZoomableController) mDraweeView.getZoomableController();
                RectF imageBounds = zoomableController.getImageBounds();
                float x = (e.getX() - imageBounds.left) / imageBounds.width();
                float y = (e.getY() - imageBounds.top) / imageBounds.height();
                PointF imagePoint = new PointF(x, y);

                RectF viewBounds = zoomableController.getViewBounds();
                PointF viewPoint = new PointF(e.getX(), e.getY());
                if (zoomableController.getScaleFactor() > zoomableController.getMinScaleFactor()) {
                    zoomableController.zoomToPoint(0.01f, imagePoint, viewPoint, AnimatedZoomableController.LIMIT_ALL, 250, new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: double tap to zoom complete");
                        }
                    });
                } else {
                    zoomableController.zoomToPoint(zoomableController.getMaxScaleFactor(), imagePoint,
                            new PointF(viewBounds.centerX(), viewBounds.centerY()),
                            AnimatedZoomableController.LIMIT_ALL, 250, new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "run: double tap to zoom complete");
                                }
                            });
                }
                return true;
            }
        });
        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FrescoUtils.load(mImageUrl, mDraweeView, mImageWidth, mImageHeight);
        AnimatedZoomableController zoomableController = (AnimatedZoomableController) mDraweeView.getZoomableController();
        zoomableController.setMaxScaleFactor(3.0f);
    }
}
