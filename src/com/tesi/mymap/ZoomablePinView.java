package com.tesi.mymap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ZoomablePinView extends ImageView{

	private double posX=0, posY=0;
	private double posXInPixels=0, posYInPixels=0;
	private double width=0, height=0;
	
	
	public ZoomablePinView(Context context) {
		super(context);
		setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.tack_green));
	}
	
	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		this.width = bm.getWidth();
		this.height = bm.getHeight();
	}
	
	public void setPosition (float posX,float posY,PointF centerPoint,PointF centerFocus,float saveScale,float inferior,float superior){
		float deltaX = (posX - centerPoint.x) / saveScale;
		float deltaY = (posY - centerPoint.y) / saveScale;
		posXInPixels = centerFocus.x + deltaX;
		posYInPixels = centerFocus.y + deltaY;
		
		this.posX =posX;
		if(posYInPixels<superior){
			this.posY=((superior-centerFocus.y)*saveScale)+centerPoint.y;
			posYInPixels=((this.posY-centerPoint.y)/saveScale)+centerFocus.y;
		}
		else if(posYInPixels > inferior){
			this.posY=((inferior-centerFocus.y)*saveScale)+centerPoint.y;
			posYInPixels=((this.posY-centerPoint.y)/saveScale)+centerFocus.y;
		}	
		else
			this.posY =posY;
		
		setMargins();
	}
	
	public void setPositionFromPix(double posX,double posY,PointF centerPoint, PointF centerFocus, float saveScale ){
		
		double deltaX=posX-centerFocus.x;
		double x=(saveScale*deltaX)+centerPoint.x;
		double deltaY=posY-centerFocus.y;
		double y=(saveScale*deltaY)+centerPoint.y;
		
		this.posX=x;
		this.posY=y;
		
		setMargins();
		
	}
	
	public void moveOnZoom (float focusX, float focusY, float scale) {
		posX = (scale * (posX - focusX)) + focusX;
		posY = (scale * (posY - focusY)) + focusY;
		setMargins();
	}
	
	public void moveOnDrag (float dx, float dy) {
		posX += dx;
		posY += dy;
		setMargins();
	}
	
	private void setMargins() {
		int leftMargin = (int) (posX - width/2);
		int topMargin = (int) (posY - height);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(getLayoutParams());
		layoutParams.setMargins( leftMargin, topMargin, 0, 0);
		setLayoutParams(layoutParams);
	}
	
	public PointF getPositionInPixels() {
		PointF pinPos = new PointF((float)posXInPixels, (float)posYInPixels);
		return pinPos;
	}
}
