/**
 * 
 */
package com.nstech.oa.signalgen.ui;

import java.util.LinkedList;

import com.nstech.oa.signalgen.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Lloyd
 *
 */
public class Slider extends View {

	public static class SliderValueChangedEvent {

		private final float oldValue;

		private final float newValue;

		public SliderValueChangedEvent(float oldValue, float newValue) {
			super();
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		public float getOldValue() {
			return oldValue;
		}

		public float getNewValue() {
			return newValue;
		}

	}

	public static interface OnSlidervalueChangedListener {

		void onSliderChanged(SliderValueChangedEvent args);

	}

	private class Attributes {

		public boolean horizontal;
		public int grooveColor;
		public int knobColor;
		public int knobOpacity;
		public int grooveDisabledColor;
		public int knobDisabledColor;
		public int knobDisabledOpacity;
		public float grooveThickness;

		public float sliderMin;
		public float sliderMax;
		public float sliderInit;

		public Attributes() {

		}

		/*
		 * public Attributes(boolean horizontal, int grooveColor, int knobColor,
		 * int knobOpacity, int grooveDisabledColor, int knobDisabledColor, int
		 * knobDisabledOpacity, float grooveThickness, float sliderMin, float
		 * sliderMax, float sliderInit) { super(); this.horizontal = horizontal;
		 * this.grooveColor = grooveColor; this.knobColor = knobColor;
		 * this.knobOpacity = knobOpacity; this.grooveDisabledColor =
		 * grooveDisabledColor; this.knobDisabledColor = knobDisabledColor;
		 * this.knobDisabledOpacity = knobDisabledOpacity; this.grooveThickness
		 * = grooveThickness; this.sliderMin = sliderMin; this.sliderMax =
		 * sliderMax; this.sliderInit = sliderInit; }
		 */

	}

	private boolean horizontal;
	private int grooveColor;
	private int knobColor;
	private int knobOpacity;
	private int grooveDisabledColor;
	private int knobDisabledColor;
	private int knobDisabledOpacity;
	private float grooveThickness;

	private Paint groovePaint;
	private Paint knobPaint;
	private Paint grooveDisabledPaint;
	private Paint knobDisabledPaint;

	private float grooveSize;
	private float knobSize;
	private float centerPosition;

	private float sliderMin;
	private float sliderMax;

	private float sliderValue;

	private boolean active;
	
	private LinkedList<OnSlidervalueChangedListener> listeners = new LinkedList<OnSlidervalueChangedListener>();

	public Slider(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		Attributes as = initAttributes(context, attrs, defStyleAttr, 0);
		horizontal = as.horizontal;
		grooveColor = as.grooveColor;
		knobColor = as.knobColor;
		knobOpacity = as.knobOpacity;
		grooveDisabledColor = as.grooveDisabledColor;
		knobDisabledColor = as.knobDisabledColor;
		knobDisabledOpacity = as.knobDisabledOpacity;
		grooveThickness = as.grooveThickness;
		sliderMin = as.sliderMin;
		sliderMax = as.sliderMax;
		sliderValue = as.sliderInit;
		Paint[] ps = initPaint();
		groovePaint = ps[0];
		knobPaint = ps[1];
		grooveDisabledPaint = ps[2];
		knobDisabledPaint = ps[3];
	}

	public Slider(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public Slider(Context context) {
		this(context, null, 0);
	}

	private Attributes initAttributes(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		TypedArray ta = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.Slider, defStyleAttr, defStyleRes);
		Attributes aout = new Attributes();
		aout.horizontal = ta.getInteger(R.styleable.Slider_orientation, 0) == 0;
		aout.grooveColor = ta.getColor(R.styleable.Slider_grooveColor,
				0xff000000);
		aout.grooveDisabledColor = ta.getColor(
				R.styleable.Slider_grooveDisabledColor, 0xffbbbbbb);
		aout.knobColor = ta.getColor(R.styleable.Slider_knobColor, 0xff888888);
		aout.knobDisabledColor = ta.getColor(
				R.styleable.Slider_knobDisabledColor, 0xffdddddd);
		aout.knobOpacity = ta.getInteger(R.styleable.Slider_knobOpacity, 192);
		aout.knobDisabledOpacity = ta.getInteger(
				R.styleable.Slider_knobDisabledOpacity, 255);
		aout.grooveThickness = ta.getFloat(R.styleable.Slider_grooveThickness,
				2);
		aout.sliderMin = ta.getFloat(R.styleable.Slider_sliderMinimum, 0);
		aout.sliderMax = ta.getFloat(R.styleable.Slider_sliderMaximum, 1);
		aout.sliderInit = ta.getFloat(R.styleable.Slider_sliderInitial, 0.5f);
		return aout;
		/*
		 * new Attributes( ta.getInteger(R.styleable.Slider_orientation, 0) ==
		 * 0, ta.getColor(R.styleable.Slider_grooveColor, 0xff000000),
		 * ta.getColor(R.styleable.Slider_knobColor, 0xff888888),
		 * ta.getInteger(R.styleable.Slider_knobOpacity, 192),
		 * ta.getColor(R.styleable.Slider_grooveDisabledColor, 0xffbbbbbb),
		 * ta.getColor(R.styleable.Slider_knobDisabledColor, 0xffdddddd),
		 * ta.getInteger(R.styleable.Slider_knobDisabledOpacity, 255), ta
		 * .getFloat(R.styleable.Slider_grooveThickness, 2), ta
		 * .getFloat(R.styleable.Slider_sliderMinimum, 0), ta
		 * .getFloat(R.styleable.Slider_sliderMaximum, 1), ta
		 * .getFloat(R.styleable.Slider_sliderInitial, .5f));
		 */
	}

	private Paint[] initPaint() {
		Paint[] ps = new Paint[4];
		ps[0] = new Paint(Paint.ANTI_ALIAS_FLAG);
		ps[0].setColor(grooveColor);
		ps[0].setStrokeWidth(grooveThickness);
		ps[1] = new Paint(Paint.ANTI_ALIAS_FLAG);
		ps[1].setColor(knobColor);
		ps[1].setAlpha(knobOpacity);
		ps[2] = new Paint(Paint.ANTI_ALIAS_FLAG);
		ps[2].setColor(grooveDisabledColor);
		ps[2].setStrokeWidth(grooveThickness);
		ps[3] = new Paint(Paint.ANTI_ALIAS_FLAG);
		ps[3].setColor(knobDisabledColor);
		ps[3].setAlpha(knobDisabledOpacity);
		return ps;
	}

	private void refreshPaint() {
		groovePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		groovePaint.setColor(grooveColor);
		groovePaint.setStrokeWidth(grooveThickness);
		knobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		knobPaint.setColor(knobColor);
		knobPaint.setAlpha(knobOpacity);
		grooveDisabledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		grooveDisabledPaint.setColor(grooveDisabledColor);
		grooveDisabledPaint.setStrokeWidth(grooveThickness);
		knobDisabledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		knobDisabledPaint.setColor(knobDisabledColor);
		knobDisabledPaint.setAlpha(knobDisabledOpacity);
	}

	public boolean isHorizontal() {
		return horizontal;
	}

	public void setHorizontal(boolean horizontal) {
		this.horizontal = horizontal;
		invalidate();
		requestLayout();
	}

	public int getGrooveColor() {
		return grooveColor;
	}

	public void setGrooveColor(int grooveColor) {
		this.grooveColor = grooveColor;
		refreshPaint();
		invalidate();
	}

	public int getKnobColor() {
		return knobColor;
	}

	public void setKnobColor(int knobColor) {
		this.knobColor = knobColor;
		refreshPaint();
		invalidate();
	}

	public int getKnobOpacity() {
		return knobOpacity;
	}

	public void setKnobOpacity(int knobOpacity) {
		this.knobOpacity = knobOpacity;
		refreshPaint();
		invalidate();
	}

	public int getDisabledGrooveColor() {
		return grooveDisabledColor;
	}

	public void setGrooveDisabledColor(int grooveDisabledColor) {
		this.grooveDisabledColor = grooveDisabledColor;
		refreshPaint();
		invalidate();
	}

	public int getKnobDisabledColor() {
		return knobDisabledColor;
	}

	public void setKnobDisabledColor(int knobDisabledColor) {
		this.knobDisabledColor = knobDisabledColor;
		refreshPaint();
		invalidate();
	}

	public int getKnobDisabledOpacity() {
		return knobDisabledOpacity;
	}

	public void setKnobDisabledOpacity(int knobDisabledOpacity) {
		this.knobDisabledOpacity = knobDisabledOpacity;
		refreshPaint();
		invalidate();
	}

	public float getGrooveThickness() {
		return grooveThickness;
	}

	public void setGrooveThickness(float grooveThickness) {
		this.grooveThickness = grooveThickness;
		invalidate();
	}

	public float getSliderMin() {
		return sliderMin;
	}

	public void setSliderMin(float sliderMin) {
		this.sliderMin = sliderMin;
		invalidate();
	}

	public float getSliderMax() {
		return sliderMax;
	}

	public void setSliderMax(float sliderMax) {
		this.sliderMax = sliderMax;
		invalidate();
	}

	public float getSliderValue() {
		return sliderValue;
	}

	public void setSliderValue(float sliderValue) {
		if (sliderValue >= sliderMin && sliderValue <= sliderMax) {
			this.sliderValue = sliderValue;
			invalidate();
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		float xpad = (float) (getPaddingLeft() + getPaddingRight());
		float ypad = (float) (getPaddingTop() + getPaddingBottom());

		float width = w - xpad;
		float height = h - ypad;

		grooveSize = horizontal ? width : height;
		knobSize = Math.min(Math.min(width, height), Math.min(
				horizontal ? getPaddingLeft() : getPaddingTop(),
				horizontal ? getPaddingRight() : getPaddingBottom()));
		centerPosition = horizontal ? h / 2 : w / 2;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (horizontal) {
			canvas.drawLine(getPaddingLeft(), centerPosition, getPaddingLeft()
					+ grooveSize, centerPosition, isEnabled() ? groovePaint
					: grooveDisabledPaint);
			canvas.drawCircle(getPaddingLeft() + grooveSize
					* (sliderValue - sliderMin) / (sliderMax - sliderMin),
					centerPosition, knobSize / 2, isEnabled() ? knobPaint
							: knobDisabledPaint);
		} else {
			canvas.drawLine(centerPosition, getPaddingTop(), centerPosition,
					getPaddingTop() + grooveSize, isEnabled() ? groovePaint
							: grooveDisabledPaint);
			canvas.drawCircle(centerPosition, getPaddingTop() + grooveSize
					* (sliderValue - sliderMin) / (sliderMax - sliderMin),
					knobSize / 2, isEnabled() ? knobPaint : knobDisabledPaint);
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (horizontal
					&& (event.getY() > getPaddingTop() || event.getY() < this
							.getHeight() - getPaddingBottom())
					|| !horizontal
					&& (event.getX() > getPaddingLeft() || event.getX() < getWidth()
							- getPaddingRight())) {
				active = true;
				return true;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (active) {
				float y = event.getY();
				setSliderValue((y - getPaddingTop()) * (sliderMax - sliderMin)
						/ (getHeight() - getPaddingTop() - getPaddingBottom()));
				active = false;
				return true;
			}
		} else if (event.getAction() == MotionEvent.ACTION_MOVE)
			if (active) {
				float y = event.getY();
				setSliderValue((y - getPaddingTop()) * (sliderMax - sliderMin)
						/ (getHeight() - getPaddingTop() - getPaddingBottom()));
				return true;
			}
		return super.onTouchEvent(event);
	}
	
	public void setOnSlidervalueChangedListener(OnSlidervalueChangedListener listener) {
		listeners.add(listener);
	}

}