/*
 * This file provided by Facebook is for non-commercial testing and evaluation
 * purposes only.  Facebook reserves all rights not expressly granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * FACEBOOK BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.facebook.samples;

import android.app.Activity;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.facebook.samples.zoomable.AnimatedZoomableController;
import com.facebook.samples.zoomable.ZoomableDraweeView;
import com.liguang.gankio.R;

public class MainActivity extends Activity {

    private MyPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_second);

        final ViewPager pager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new MyPagerAdapter(pager.getChildCount());
        mAdapter.setDoubleTapListener(new MyPagerAdapter.onDoubleTapListener() {
            @Override
            public void onDoubleTap(MotionEvent e) {
                ZoomableDraweeView zoomableDraweeView = (ZoomableDraweeView) pager.getChildAt(pager.getCurrentItem()).findViewById(R.id.zoomableView);
                AnimatedZoomableController zoomableController = (AnimatedZoomableController) zoomableDraweeView.getZoomableController();
                if (zoomableController.getScaleFactor() > zoomableController.getMinScaleFactor()) {
                    zoomableController.zoomToPoint(0.01f, new PointF(e.getX(), e.getY()), new PointF(e.getX(), e.getY()));
                } else {

                }
            }
        });

        pager.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.allow_zoomed_swiping).setChecked(mAdapter.allowsSwipingWhileZoomed());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.allow_zoomed_swiping) {
            mAdapter.toggleAllowSwipingWhileZoomed();
            item.setChecked(mAdapter.allowsSwipingWhileZoomed());
            mAdapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }
}
