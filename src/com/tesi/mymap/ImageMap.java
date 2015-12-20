package com.tesi.mymap;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;


public class ImageMap extends ImageView {
	
	boolean flag=true;
	Matrix matrix;
	
	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;
	
	// Remember some things for zooming
	PointF last = new PointF();
	PointF start = new PointF();
	float minScale = 1f;
	float maxScale = 3f;
	float[] m;
	
	
	int viewWidth, viewHeight;
	static final int CLICK = 15;
	float saveScale = 1f;
	protected float origWidth, origHeight;
	int oldMeasuredWidth, oldMeasuredHeight;
	
	
	float superiorMargin;
	float inferiorMargin;
	
	ScaleGestureDetector mScaleDetector;
	
	Context context;
	
	private ZoomablePinView pin = null;
	private ZoomablePinView ghost=null;
	private ZoomablePinView position=null;
	
	
	// Center of the focused area in pixels
	private PointF centerFocus = new PointF();
	
	
    
	public ImageMap(Context context) {
		super(context);
		sharedConstructing(context);
	}

	public ImageMap(Context context, AttributeSet attrs) {
		super(context, attrs);
		sharedConstructing(context);
	}
	
	private void sharedConstructing(Context context) {
		super.setClickable(true);
		this.context = context;
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		matrix = new Matrix();
		m = new float[9];
		setImageMatrix(matrix);
		setScaleType(ScaleType.MATRIX);
	
		setOnTouchListener(new OnTouchListener() {
	
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mScaleDetector.onTouchEvent(event);
				PointF curr = new PointF(event.getX(), event.getY());
				
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					last.set(curr);
					start.set(last);
					mode = DRAG;
					break;
	
				case MotionEvent.ACTION_MOVE:
					if (mode == DRAG) {
						float deltaX = curr.x - last.x;
						float deltaY = curr.y - last.y;
						float fixTransX = getFixDragTrans(deltaX, viewWidth, origWidth * saveScale);
						float fixTransY = getFixDragTrans(deltaY, viewHeight, origHeight * saveScale);
						matrix.postTranslate(fixTransX, fixTransY);
						fixTrans();
						last.set(curr.x, curr.y);
						moveCenterPointDrag(fixTransX, fixTransY);
						if (pin != null)
							pin.moveOnDrag(fixTransX, fixTransY);
						if(position!=null)
							position.moveOnDrag(fixTransX, fixTransY);
						if(ghost!=null)
							ghost.moveOnDrag(fixTransX, fixTransY);
					}
					break;
	
				case MotionEvent.ACTION_UP:
					mode = NONE;
					int xDiff = (int) Math.abs(curr.x - start.x);
					int yDiff = (int) Math.abs(curr.y - start.y);
					
					if(flag){
						if (xDiff < CLICK && yDiff < CLICK) {
							performClick();
							if (pin == null)
								addPin();
							PointF centerPoint = new PointF((float) viewWidth/2, (float) viewHeight/2);
							pin.setPosition(curr.x, curr.y, centerPoint, centerFocus, saveScale, inferiorMargin, superiorMargin);
						}
					}
					break;
	
				case MotionEvent.ACTION_POINTER_UP:
					mode = NONE;
					break;
				}
				
				setImageMatrix(matrix);
				invalidate();
				return true; // indicate event was handled
			}
	
		});
	}
	
	
	public void negativeFlag(){
		this.flag=false;
	}

	public void setMaxZoom(float x) {
		maxScale = x;
	}
	
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			mode = ZOOM;
			return true;
		}
		
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float mScaleFactor = detector.getScaleFactor();
			float origScale = saveScale;
			saveScale *= mScaleFactor;
			if (saveScale > maxScale) {
				saveScale = maxScale;
				mScaleFactor = maxScale / origScale;
			} else if (saveScale < minScale) {
				saveScale = minScale;
				mScaleFactor = minScale / origScale;
			}
		
			if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight) {
				matrix.postScale(mScaleFactor, mScaleFactor, (float) viewWidth/2, (float) viewHeight/2);
				moveCenterPointZoom((float) viewWidth/2, (float) viewHeight/2, mScaleFactor);
				if (pin != null)
					pin.moveOnZoom((float) viewWidth/2, (float) viewHeight/2, mScaleFactor);
				if(position!=null)
					position.moveOnZoom((float) viewWidth/2, (float) viewHeight/2, mScaleFactor);
				if(ghost!=null)
					ghost.moveOnZoom((float) viewWidth/2, (float) viewHeight/2, mScaleFactor);
				
			}
			else {
				matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
				moveCenterPointZoom(detector.getFocusX(), detector.getFocusY(), mScaleFactor);
				if (pin != null)
					pin.moveOnZoom(detector.getFocusX(), detector.getFocusY(), mScaleFactor);
				if(position!=null)
					position.moveOnZoom(detector.getFocusX(), detector.getFocusY(), mScaleFactor);
				if(ghost!=null)
					ghost.moveOnZoom(detector.getFocusX(), detector.getFocusY(), mScaleFactor);
				
			}
			fixTrans();
			return true;
		}
	}
	//fine scaleDetector
	void fixTrans() {
		matrix.getValues(m);
		float transX = m[Matrix.MTRANS_X];
		float transY = m[Matrix.MTRANS_Y];
	
		float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);
		float fixTransY = getFixTrans(transY, viewHeight, origHeight * saveScale);
	
		if (fixTransX != 0 || fixTransY != 0) {
			matrix.postTranslate(fixTransX, fixTransY);
			moveCenterPointDrag(fixTransX, fixTransY);
			if (pin != null)
				pin.moveOnDrag(fixTransX, fixTransY);
			if(position!=null)
				position.moveOnDrag(fixTransX, fixTransY);
			if(ghost!=null)
				ghost.moveOnDrag(fixTransX, fixTransY);
			
		}
	}
	
	float getFixTrans(float trans, float viewSize, float contentSize) {
		float minTrans, maxTrans;
	
		if (contentSize <= viewSize) {
			minTrans = 0;
			maxTrans = viewSize - contentSize;
		} else {
			minTrans = viewSize - contentSize;
			maxTrans = 0;
		}
	
		if (trans < minTrans)
			return -trans + minTrans;
		else if (trans > maxTrans)
			return -trans + maxTrans;
		else if (contentSize <= viewSize && mode == ZOOM)
			return ((minTrans + maxTrans) / 2) - trans;
		return 0;
	}
	
	float getFixDragTrans(float delta, float viewSize, float contentSize) {
		if (contentSize <= viewSize) {
			return 0;
		}
		return delta;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		viewWidth = MeasureSpec.getSize(widthMeasureSpec);
		viewHeight = MeasureSpec.getSize(heightMeasureSpec);
	
		centerFocus.x = (float) viewWidth/2;
		centerFocus.y = (float) viewHeight/2;
		//
		// Rescales image on rotation
		//
		if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight|| viewWidth == 0 || viewHeight == 0)
			return;
		oldMeasuredHeight = viewHeight;
		oldMeasuredWidth = viewWidth;
		
		
		if (saveScale == 1) {
			//Fit to screen.
			float scale;
	
			Drawable drawable = getDrawable();
			if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)
				return;
			int bmWidth = drawable.getIntrinsicWidth();
			int bmHeight = drawable.getIntrinsicHeight();
	
			Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : " + bmHeight);
	
			float scaleX = (float) viewWidth / (float) bmWidth;
			float scaleY = (float) viewHeight / (float) bmHeight;
			scale = Math.min(scaleX, scaleY);
			matrix.setScale(scale, scale);
			
			// Center the image
			float redundantYSpace = (float) viewHeight - (scale * (float) bmHeight);
			float redundantXSpace = (float) viewWidth - (scale * (float) bmWidth);
			redundantYSpace /= 2;
			redundantXSpace /= 2;
			
			Log.d("Risoluzione", "Width: " + viewWidth + " Height : " + viewHeight);
			matrix.postTranslate(redundantXSpace, redundantYSpace);
	
			origWidth = viewWidth - 2 * redundantXSpace;
			origHeight = viewHeight - 2 * redundantYSpace;
			setImageMatrix(matrix);
			
			superiorMargin=redundantYSpace;
			inferiorMargin=origHeight+superiorMargin;
			
		}
		fixTrans();
	}

	public void addPin() {
		pin = new ZoomablePinView(context);
		pin.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		ViewGroup parent = (ViewGroup) getParent();
		parent.addView(pin);
	}
	public void removePin(){
		ViewGroup parent=(ViewGroup) getParent();
		parent.removeView(pin);
		pin=null;
		
		if(ghost!=null){
			parent.removeView(ghost);
			ghost=null;
		}
	}
	public ZoomablePinView getPin() {
		return pin;
	}
	public void addGhost(double x,double y){
		ghost = new ZoomablePinView(context);
		ghost.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ghost));
		ghost.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		ViewGroup parent = (ViewGroup) getParent();
		parent.addView(ghost);
		
		PointF centerPoint = new PointF((float) viewWidth/2, (float) viewHeight/2);
		ghost.setPositionFromPix(x, y, centerPoint, centerFocus, saveScale);
		
	}
	public void addPosition(double x,double y){
		if(y<superiorMargin || y>inferiorMargin || x > viewWidth || x <= 0)
			Toast.makeText(context, R.string.out_of_map, Toast.LENGTH_SHORT).show();
		else{
			
			if(position==null){
				position=new ZoomablePinView(context);
				position.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.tack_pos));
				position.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT ));
				ViewGroup parent = (ViewGroup) getParent();
				parent.addView(position);
			}
			PointF centerPoint = new PointF((float) viewWidth/2, (float) viewHeight/2);
			position.setPositionFromPix(x, y, centerPoint, centerFocus, saveScale);
		}
		
	}
	
	private void moveCenterPointZoom (float focusX, float focusY, float scale) {
		float centerViewX = (float) viewWidth/2;
		float centerViewY = (float) viewHeight/2;
		float focusDistanceX = centerViewX - focusX;
		float focusDistanceY = centerViewY - focusY;
		float deltaX = focusDistanceX / scale - focusDistanceX;
		float deltaY = focusDistanceY / scale - focusDistanceY;
		centerFocus.x += deltaX / (saveScale / scale);
		centerFocus.y += deltaY / (saveScale / scale);
	}
	
	private void moveCenterPointDrag (float deltaX, float deltaY) {
		centerFocus.x -= deltaX / saveScale;
		centerFocus.y -= deltaY / saveScale;
	}
}