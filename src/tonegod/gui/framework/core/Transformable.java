/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tonegod.gui.framework.core;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import tonegod.gui.framework.animation.TemporalAction;

/**
 *
 * @author t0neg0d
 */
public interface Transformable {
	public void setPositionX(float x);
	public void setPositionY(float y);
	public void setPositionZ(float z);
	public void setPosition(float x, float y);
	public void setPosition(Vector2f pos);
	public void setRotation(float rotation);
	public void setScale(float x, float y);
	public void setScale(Vector2f scale);
	public void setScaleX(float scaleX);
	public void setScaleY(float scaleY);
	public void setOrigin(Vector2f origin);
	public void setOrigin(float x, float y);
	public void setOriginX(float originX);
	public void setOriginY(float originY);
	public void setColor(ColorRGBA color);
	public void setColorR(float r);
	public void setColorG(float g);
	public void setColorB(float b);
	public void setColorA(float a);
	public void setTCOffsetX(float x);
	public void setTCOffsetY(float y);
	public void setDimensions(Vector2f dim);
	public void setDimensions(float w, float h);
	public void setWidth(float w);
	public void setHeight(float h);
	public void setSkew(Vector2f skew);
	public void setSkew(float x, float y);
	public void setSkewX(float x);
	public void setSkewY(float y);
	public void setIgnoreMouse(boolean ignoreMouse);
	public void setIsMovable(boolean isMovable);
	
	public Vector2f getPosition();
	public float getPositionX();
	public float getPositionY();
	public float getPositionZ();
	public float getRotation();
	public Vector2f getScale();
	public float getScaleX();
	public float getScaleY();
	public Vector2f getOrigin();
	public float getOriginX();
	public float getOriginY();
	public ColorRGBA getColor();
	public float getColorR();
	public float getColorG();
	public float getColorB();
	public float getColorA();
	public Vector2f getDimensions();
	public float getWidth();
	public float getHeight();
	public Vector2f getTCOffset();
	public float getTCOffsetX();
	public float getTCOffsetY();
	public Vector2f getSkew();
	public float getSkewX();
	public float getSkewY();
	public boolean getIgnoreMouse();
	public boolean getIsMovable();
	
	public void addAction(TemporalAction action);
	
	public boolean getContainsAction(TemporalAction action);
}
