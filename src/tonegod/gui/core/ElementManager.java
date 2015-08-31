/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tonegod.gui.core;

import tonegod.gui.style.Style;
import com.jme3.app.Application;
import com.jme3.collision.CollisionResult;
import com.jme3.font.BitmapFont;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import tonegod.gui.controls.util.ModalBackground;
import tonegod.gui.core.utils.ScaleUtil;
import tonegod.gui.style.StyleManager.CursorType;
import tonegod.gui.effects.EffectManager;
import tonegod.gui.effects.cursor.CursorEffects;
import tonegod.gui.framework.core.AnimLayer;
import tonegod.gui.framework.core.AnimManager;

/**
 *
 * @author t0neg0d
 */
public interface ElementManager {
	
	public Application getApplication();
	
	public float getWidth();
	public float getHeight();
	public Vector2f getMouseXY();
	public Vector2f getTouchXY();
	public CollisionResult getLastCollision();
	public Node getGUINode();
	
	public void addElement(Element element);
	public void addElement(Element element, boolean hide);
	public void removeElement(Element element);
	public Element getElementById(String UID);
	public void setKeyboardElement(Element element);
	public void setTabFocusElement(Element element);
	public void resetTabFocusElement();
	public Element getDropElement();
	
	public float getZOrderStepMinor();
	public float getZOrderStepMajor();
	public void updateZOrder(Element element);
	
	public Style getStyle(String key);
	public void setClipboardText(String text);
	public String getClipboardText();
	
	public boolean getUseTextureAtlas();
	public float[] parseAtlasCoords(String coords);
	public Texture getAtlasTexture();
	public Texture createNewTexture(String texturePath);
	public void setGlobalAlpha(float alpha);
	public float getGlobalAlpha();
	
	public ScaleUtil getScaleManager();
	public float scaleFloat(float in);
	public Vector2f scaleVector2f(Vector2f in);
	public Vector3f scaleVector3f(Vector3f in);
	public Vector4f scaleVector4f(Vector4f in);
	public float scaleFontSize(float in);
	
	public BitmapFont getDefaultGUIFont();
	public EffectManager getEffectManager();
	public AnimManager getAnimManager();
	
	public boolean getUseUIAudio();
	public void setUseUIAudio(boolean use);
	public void setUIAudioVolume(float volume);
	
	public boolean getUseToolTips();
	public void setUseToolTips(boolean use);
	public void updateToolTipLocation();
	public Element getToolTipFocus();
	public void hideToolTip();
	
	public void setUseCustomCursors(boolean use);
	public boolean getUseCustomCursors();
	public void setCursor(CursorType cursorType);
	public void setUseCursorEffects(boolean use);
	public CursorEffects getCursorEffects();
//	public boolean getUseCursorEffects();
	
	public void onKeyEvent(KeyInputEvent evt);
	
	public ModalBackground getModalBackground();
	public void showAsModal(Element el, boolean showWithEffect);
	public void hideModalBackground();
	
	public void showVirtualKeyboard();
	public void hideVirtualKeyboard();
	public void handleAndroidMenuState(Element element);
	
	// 2D Framework
	public AnimLayer addAnimLayer(String UID);
	public void addAnimLayer(String UID, AnimLayer layer);
	public AnimLayer removeAnimLayer(String UID);
	public void removeAnimLayer(AnimLayer animLayer);
}
