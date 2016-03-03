package com.realnumworks.focustimer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class DrawableHelper {

	public static void setBackGroundDrawable(View view, Drawable drawable) {
		if (view == null || drawable == null) {
			return;
		}
		if (Build.VERSION.SDK_INT >= 16/* android.os.Build.VERSION_CODES.JELLY_BEAN */) {
			view.setBackground(drawable);
		} else {
			view.setBackgroundDrawable(drawable);
		}
	}

	public static Drawable getDrawable(Context context, int id) {
		if (Build.VERSION.SDK_INT >= 21 /* Build.VERSION_CODES.LOLLIPOP */) {
			return context.getResources().getDrawable(id, context.getTheme());
		} else {
			return context.getResources().getDrawable(id);
		}
	}
	
	/** To Image Sampling cuz Out Of Memory Error **/
	public static Drawable getResizedDrawbleResource(Resources r, int id, int width, int height) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 8;   // can be changed
		Bitmap src = BitmapFactory.decodeResource(r, id, options);
		Bitmap resized = Bitmap.createScaledBitmap(src, width, height, true);
		return new BitmapDrawable(resized);
	}
}
