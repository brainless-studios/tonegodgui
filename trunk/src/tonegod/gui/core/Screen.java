package tonegod.gui.core;

import com.jme3.app.Application;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.font.plugins.BitmapFontLoader;
import com.jme3.input.KeyInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import tonegod.gui.controls.form.Form;
import tonegod.gui.controls.menuing.Menu;
import tonegod.gui.controls.text.TextField;
import tonegod.gui.core.Element.Borders;
import tonegod.gui.core.utils.XMLHelper;
import tonegod.gui.effects.Effect;
import tonegod.gui.effects.EffectManager;
import tonegod.gui.fonts.BitmapFontLoaderX;
import tonegod.gui.listeners.*;

/**
 *
 * @author t0neg0d
 */
public class Screen implements Control, RawInputListener {
	private Application app;
	private Spatial spatial;
	private Map<String, Element> elements = new HashMap();
	private Ray elementZOrderRay = new Ray();
	private Vector3f guiRayOrigin = new Vector3f();
	
	private Element eventElement = null;
	private Element keyboardElement = null;
	private Element tabFocusElement = null;
	private Form focusForm = null;
	private Vector2f eventElementOriginXY = new Vector2f();
	private float eventElementOffsetX = 0;
	private float eventElementOffsetY = 0;
	private Borders eventElementResizeDirection = null;
	private Element mouseFocusElement = null;
	private Element previousMouseFocusElement = null;
	private boolean focusElementIsMovable = false;
	private boolean mousePressed = false;
	private boolean mouseLeftPressed = false;
	private boolean mouseRightPressed = false;
	private boolean mouseWheelPressed = false;
	
	private float zOrderCurrent = 50f;
	private float zOrderStepMajor = 10f;
	private float zOrderStepMinor = 0.1f;
	
	private String clipboardText = "";
	
	private String styleMap;
	private Map<String, Style> styles = new HashMap();
	
	private EffectManager effectManager;
	private Node t0neg0dGUI = new Node("t0neg0dGUI");
	
	private Vector2f mouseXY = new Vector2f(0,0);
	private boolean SHIFT = false;
	
	/**
	 * Creates a new instance of the Screen control using the default style information
	 * provided with the library.
	 * 
	 * @param app A JME Application
	 */
	public Screen(Application app) {
		this(app, "tonegod/gui/style/def/style_map.xml");
	}
	
	/**
	 * Creates an instance of the Screen control.
	 * 
	 * @param app A JME Application
	 * @param styleMap A path to the style_map.xml file containing the custom theme information
	 */
	public Screen(Application app, String styleMap) {
		this.app = app;
		this.elementZOrderRay.setDirection(Vector3f.UNIT_Z);
		app.getAssetManager().unregisterLoader(BitmapFontLoader.class);
		app.getAssetManager().registerLoader(BitmapFontLoaderX.class, "fnt");
		
		this.styleMap = styleMap;
		parseStyles(styleMap);
		
		effectManager = new EffectManager();
	}
	
	/**
	 * Returns the JME application associated with the Screen
	 * @return Application app
	 */
	public Application getApplication() {
		return this.app;
	}
	
	/**
	 * Return the width of the current Viewport
	 * 
	 * @return float width
	 */
	public float getWidth() {
		return app.getViewPort().getCamera().getWidth();
	}
	
	/**
	 * Returns the height of the current Viewport
	 * 
	 * @return  float height
	 */
	public float getHeight() {
		return app.getViewPort().getCamera().getHeight();
	}
	
	/**
	 * Initializes the Screen control
	 */
	public void initialize() {
		app.getInputManager().addRawInputListener(this);
	}
	
	@Override
	public void update(float tpf) {
	//	throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void render(RenderManager rm, ViewPort vp) {
	//	throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 *  Adds an Element to the Screen and scene graph
	 * @param element The Element to add
	 */
	public void addElement(Element element) {
		elements.put(element.getUID(), element);
		
		element.setY(getHeight()-element.getHeight()-element.getY());
		
		t0neg0dGUI.attachChild(element);
		
		// Set initla z-order
		getNextZOrder(true);
		element.initZOrder(zOrderCurrent);
		element.resize(element.getX()+element.getWidth(), element.getY()+element.getHeight(), Borders.SE);
	}
	
	/**
	 * Removes an Element from the Screen and scene graph
	 * @param element The Element to remove
	 */
	public void removeElement(Element element) {
		elements.remove(element.getUID());
		float shiftZ = element.getLocalTranslation().getZ();
		Set<String> keys = elements.keySet();
		for (String key : keys) {
			if (elements.get(key).getLocalTranslation().getZ() > shiftZ) {
				elements.get(key).move(0,0,-zOrderStepMajor);
			}
		}
		zOrderCurrent -= zOrderStepMajor;
		element.removeFromParent();
	}
	
	/**
	 * Returns the Element with the associated ID.  If not found, returns null
	 * @param UID The String ID of Element to find
	 * @return Element element
	 */
	public Element getElementById(String UID) {
		Element ret = null;
		if (elements.containsKey(UID)) {
			ret = elements.get(UID);
		} else {
			Set<String> keys = elements.keySet();
			for (String key : keys) {
				ret = elements.get(key).getChildElementById(UID);
				if (ret != null) {
					break;
				}
			}
		}
		return ret;
	}
	
	// Z-ORDER
	/**
	 * Returns the next available z-order
	 * @param stepMajor Return the z-order incremented by a major step if true, a minor step if false
	 * @return float zOrder
	 */
	public float getNextZOrder(boolean stepMajor) {
		if (stepMajor)
			zOrderCurrent += zOrderStepMajor;
		else
			zOrderCurrent += zOrderStepMinor;
		return zOrderCurrent;
	}
	
	/**
	 * Brings the element specified to the front of the zOrder list shifting other below to keep all
	 * Elements within the current z-order range.
	 * 
	 * @param topMost The Element to bring to the front
	 */
	public void updateZOrder(Element topMost) {
	//	zOrderCurrent = zOrderInit;
		String topMostUID = topMost.getUID();
		float shiftZ = topMost.getLocalTranslation().getZ();
		
		Set<String> keys = elements.keySet();
		for (String key : keys) {
			if (elements.get(key).getLocalTranslation().getZ() > shiftZ) {
				elements.get(key).move(0,0,-zOrderStepMajor);
			}
		}
		topMost.setLocalTranslation(topMost.getLocalTranslation().setZ(Float.valueOf(zOrderCurrent)));
	}
	
	/**
	 * Returns the zOrder major step value
	 * @return float
	 */
	public float getZOrderStepMajor() {
		return this.zOrderStepMajor;
	}
	
	/**
	 * Returns the zOrder minor step value
	 * @return float
	 */
	public float getZOrderStepMinor() {
		return this.zOrderStepMinor;
	}
	
	/**
	 * Stored the current mouse position as a Vector2f
	 * @param x The mouse's current X coord
	 * @param y The mouse's current Y coord
	 */
	private void setMouseXY(float x, float y) {
		mouseXY.set(x, y);
	}
	
	/**
	 * Returns a Vector2f containing the last stored mouse X/Y coords
	 * @return Vector2f mouseXY
	 */
	public Vector2f getMouseXY() {
		return this.mouseXY;
	}
	
	// Raw Input handlers
	@Override
	public void beginInput() {
	//	throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void endInput() {
	//	throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void onJoyAxisEvent(JoyAxisEvent evt) {
	//	throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void onJoyButtonEvent(JoyButtonEvent evt) {
	//	throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void onMouseMotionEvent(MouseMotionEvent evt) {
		setMouseXY(evt.getX(),evt.getY());
		if (!mousePressed) {
			mouseFocusElement = getEventElement(evt.getX(), evt.getY());
			if (mouseFocusElement != previousMouseFocusElement) {
				if (previousMouseFocusElement instanceof MouseFocusListener) {
					((MouseFocusListener)previousMouseFocusElement).onLoseFocus(evt);
				}
				if (mouseFocusElement instanceof MouseFocusListener) {
					((MouseFocusListener)mouseFocusElement).onGetFocus(evt);
				}
				previousMouseFocusElement = mouseFocusElement;
			}
			if (mouseFocusElement != null) {
				focusElementIsMovable = mouseFocusElement.getIsMovable();
				
				if (mouseFocusElement instanceof MouseWheelListener) {
					if (evt.getDeltaWheel() > 0) {
						((MouseWheelListener)mouseFocusElement).onMouseWheelDown(evt);
					} else if (evt.getDeltaWheel() < 0) {
						((MouseWheelListener)mouseFocusElement).onMouseWheelUp(evt);
					}
				}
			}
			if (mouseFocusElement instanceof MouseMovementListener) {
				((MouseMovementListener)mouseFocusElement).onMouseMove(evt);
			}
		} else {
			if (eventElement != null) {
				if (mouseLeftPressed) {
					if (eventElementResizeDirection != null) {
						eventElement.resize(evt.getX(), evt.getY(), eventElementResizeDirection);
					} else if (focusElementIsMovable) {
						eventElement.moveTo(evt.getX()-eventElementOffsetX, evt.getY()-eventElementOffsetY);
					}
				}
				
				if (eventElement instanceof MouseMovementListener) {
					((MouseMovementListener)eventElement).onMouseMove(evt);
				}
			}
		}
	}

	@Override
	public void onMouseButtonEvent(MouseButtonEvent evt) {
		if (evt.isPressed()) {
			mousePressed = true;
			resetTabFocusElement();
			switch (evt.getButtonIndex()) {
				case 0:
					mouseLeftPressed = true;
					eventElement = getEventElement(evt.getX(), evt.getY());
					if (eventElement != null) {
						updateZOrder(eventElement.getAbsoluteParent());
						this.setTabFocusElement(eventElement);
						if (eventElement.getIsResizable()) {
							float offsetX = evt.getX();
							float offsetY = evt.getY();
							Element el = eventElement;
							if (offsetX > el.getAbsoluteX() && offsetX < el.getAbsoluteX()+el.getResizeBorderWestSize()) {
								if (offsetY > el.getAbsoluteY() && offsetY < el.getAbsoluteY()+el.getResizeBorderNorthSize()) {
									eventElementResizeDirection = Borders.NW;
								} else if (offsetY > (el.getAbsoluteHeight()-el.getResizeBorderSouthSize()) && offsetY < el.getAbsoluteHeight()) {
									eventElementResizeDirection = Borders.SW;
								} else {
									eventElementResizeDirection = Borders.W;
								}
							} else if (offsetX > (el.getAbsoluteWidth()-el.getResizeBorderEastSize()) && offsetX < el.getAbsoluteWidth()) {
								if (offsetY > el.getAbsoluteY() && offsetY < el.getAbsoluteY()+el.getResizeBorderNorthSize()) {
									eventElementResizeDirection = Borders.NE;
								} else if (offsetY > (el.getAbsoluteHeight()-el.getResizeBorderSouthSize()) && offsetY < el.getAbsoluteHeight()) {
									eventElementResizeDirection = Borders.SE;
								} else {
									eventElementResizeDirection = Borders.E;
								}
							} else {
								if (offsetY > el.getAbsoluteY() && offsetY < el.getAbsoluteY()+el.getResizeBorderNorthSize()) {
									eventElementResizeDirection = Borders.N;
								} else if (offsetY > (el.getAbsoluteHeight()-el.getResizeBorderSouthSize()) && offsetY < el.getAbsoluteHeight()) {
									eventElementResizeDirection = Borders.S;
								}
							}
							if (keyboardElement != null) {
								if (keyboardElement instanceof TextField) ((TextField)keyboardElement).resetTabFocus();
							}
							keyboardElement = null;
						} else if (eventElement.getIsMovable() && eventElementResizeDirection == null) {
							eventElementResizeDirection = null;
							if (keyboardElement != null) {
								if (keyboardElement instanceof TextField) ((TextField)keyboardElement).resetTabFocus();
							}
							keyboardElement = null;
							eventElementOriginXY.set(eventElement.getPosition());
						} else if (eventElement instanceof KeyboardListener) {
							if (keyboardElement != null) {
								if (keyboardElement instanceof TextField) ((TextField)keyboardElement).resetTabFocus();
							}
							keyboardElement = eventElement;
							if (keyboardElement instanceof TextField) {
								((TextField)keyboardElement).setTabFocus();
								((TextField)keyboardElement).setCaretPositionByX(evt.getX());
							}
							// TODO: Update target element's font shader
						} else {
							eventElementResizeDirection = null;
							if (keyboardElement != null) {
								if (keyboardElement instanceof TextField) ((TextField)keyboardElement).resetTabFocus();
							}
							keyboardElement = null;
						}
					}
					if (eventElement instanceof MouseButtonListener) {
						((MouseButtonListener)eventElement).onMouseLeftPressed(evt);
					}
					break;
				case 1:
					mouseRightPressed = true;
					eventElement = getEventElement(evt.getX(), evt.getY());
					if (eventElement != null) {
						updateZOrder(eventElement.getAbsoluteParent());
						if (eventElement instanceof MouseButtonListener) {
							((MouseButtonListener)eventElement).onMouseRightPressed(evt);
						}
					}
					break;
				case 2:
					mouseWheelPressed = true;
					
					if (eventElement instanceof MouseWheelListener) {
						((MouseWheelListener)eventElement).onMouseWheelPressed(evt);
					}
					break;
			}
		} else if (evt.isReleased()) {
			handleMenuState();
			switch (evt.getButtonIndex()) {
				case 0:
					mouseLeftPressed = false;
					eventElementResizeDirection = null;
					if (eventElement instanceof MouseButtonListener) {
						((MouseButtonListener)eventElement).onMouseLeftReleased(evt);
					}
					break;
				case 1:
					mouseRightPressed = false;
					if (eventElement instanceof MouseButtonListener) {
						((MouseButtonListener)eventElement).onMouseRightReleased(evt);
					}
					break;
				case 2:
					mouseWheelPressed = true;
					
					if (eventElement instanceof MouseWheelListener) {
						((MouseWheelListener)eventElement).onMouseWheelReleased(evt);
					}
					break;
			}
			mousePressed = false;
			eventElement = null;
		}
	}
	
	@Override
	public void onKeyEvent(KeyInputEvent evt) {
		if (evt.getKeyCode() == KeyInput.KEY_LSHIFT || evt.getKeyCode() == KeyInput.KEY_RSHIFT) {
			if (evt.isPressed()) SHIFT = true;
			else SHIFT = false;
		}
		if (evt.getKeyCode() == KeyInput.KEY_TAB && evt.isPressed()) {
			if (focusForm != null) {
				if (!SHIFT)	focusForm.tabNext();
				else		focusForm.tabPrev();
			}
		} else {
			if (keyboardElement != null) {
				if (evt.isPressed()) {
					((KeyboardListener)keyboardElement).onKeyPress(evt);
				} else if (evt.isReleased()) {
					((KeyboardListener)keyboardElement).onKeyRelease(evt);
				}
			}
		}
	}

	@Override
	public void onTouchEvent(TouchEvent evt) {
	//	throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Determines and returns the current mouse focus Element
	 * @param x The current mouse X coord
	 * @param y The current mouse Y coord
	 * @return Element eventElement
	 */
	private Element getEventElement(float x, float y) {
		guiRayOrigin.set(x, y, 0f);
		
		elementZOrderRay.setOrigin(guiRayOrigin);
		CollisionResults results = new CollisionResults();

		t0neg0dGUI.collideWith(elementZOrderRay, results);

		float z = 0;
		Element testEl = null, el = null;
		for (CollisionResult result : results) {
			boolean discard = false;
			if (result.getGeometry().getParent() instanceof Element) {
				testEl = ((Element)(result.getGeometry().getParent()));
				if (testEl.getIgnoreMouse()) {
					discard = true;
				} else if (testEl.getIsClipped()) {
					if (result.getContactPoint().getX() < testEl.getClippingBounds().getX() ||
						result.getContactPoint().getX() > testEl.getClippingBounds().getZ() ||
						result.getContactPoint().getY() < testEl.getClippingBounds().getY() ||
						result.getContactPoint().getY() > testEl.getClippingBounds().getW()) {
						discard = true;
					}
				}
			}
		//	System.out.println(testEl.getUID() + ": " + discard + ": " + testEl.getLocalTranslation().getZ() + ": " + z + ": " + result.getContactPoint().getZ());
			if (!discard) {
				
				if (result.getContactPoint().getZ() > z) {
					z = result.getContactPoint().getZ();
					if (result.getGeometry().getParent() instanceof Element) {
						el = testEl;//((Element)(result.getGeometry().getParent()));
					}
				}
			}
		}
		if (el != null) {
			Element parent = null;
			if (el.getEffectParent() && mousePressed) {
				parent = el.getElementParent();
			} else if (el.getEffectAbsoluteParent() && mousePressed) {
				parent = el.getAbsoluteParent();
			}
			if (parent != null) {
				el = parent;
			}
			eventElementOffsetX = x-el.getX();
			eventElementOffsetY = y-el.getY();
			return el;
		} else {
			return null;
		}
	}
	
	/**
	 * Sets the current stored text to the internal clipboard.  This is probably going
	 * to vanish quickly.
	 * @param text The text to store
	 */
	public void setClipboardText(String text) {
		this.clipboardText = text;
	}
	
	/**
	 * Returns the internal clipboard's current stored text
	 * @return String text
	 */
	public String getClipboardText() {
		return this.clipboardText;
	}
	
	/**
	 * Returns a pointer to the EffectManager
	 * @return EffectManager effectManager
	 */
	public EffectManager getEffectManager() {
		return this.effectManager;
	}
	
	@Override
	public Control cloneForSpatial(Spatial spatial) {
		Screen screen = new Screen(this.app, this.styleMap);
		screen.elements.putAll(this.elements);
		return screen;
	}

	@Override
	public void setSpatial(Spatial spatial) {
		this.spatial = spatial;
		((Node)spatial).attachChild(t0neg0dGUI);
		t0neg0dGUI.addControl(effectManager);
	}
	
	@Override
	public void write(JmeExporter ex) throws IOException {  }
	
	@Override
	public void read(JmeImporter im) throws IOException {  }
	
	// Styles
	private void parseStyles(String path) {
		List<String> docPaths = new ArrayList();
		try {
			InputStream file = Screen.class.getClassLoader().getResourceAsStream(
				path
			);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("style");
			
			for (int s = 0; s < nodeLst.getLength(); s++) {
				org.w3c.dom.Node fstNode = nodeLst.item(s);
				if (fstNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
					org.w3c.dom.Element fstElmnt = (org.w3c.dom.Element) fstNode;
					String styleName = XMLHelper.getNodeAttributeValue(fstNode, "control");
					String styleDocPath = XMLHelper.getNodeAttributeValue(fstNode, "path");
					docPaths.add(styleDocPath);
				}
			}

			if (file != null)
				file.close();
			
			for (String docPath : docPaths) {
				try {
					file = Screen.class.getClassLoader().getResourceAsStream(
						docPath
					);
					
					dbf = DocumentBuilderFactory.newInstance();
					db = dbf.newDocumentBuilder();
					doc = db.parse(file);
					doc.getDocumentElement().normalize();
					NodeList nLst = doc.getElementsByTagName("element");
					
					for (int s = 0; s < nLst.getLength(); s++) {
						org.w3c.dom.Node fstNode = nLst.item(s);
						if (fstNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
							org.w3c.dom.Element fstElmnt = (org.w3c.dom.Element) fstNode;
							String key = XMLHelper.getNodeAttributeValue(fstNode, "name");
							
							Style style = new Style();
							
							try {
								org.w3c.dom.Node nds = fstElmnt.getElementsByTagName("attributes").item(0);
								org.w3c.dom.Element el = (org.w3c.dom.Element) nds;
								NodeList nodes = el.getElementsByTagName("property");
								
								for (int n = 0; n < nodes.getLength(); n++) {
									org.w3c.dom.Node nNode = nodes.item(n);
									if (nNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
										org.w3c.dom.Element nElmnt = (org.w3c.dom.Element) nNode;
										addStyleTag(style, nNode, nElmnt);
									}
								}
								
								nds = fstElmnt.getElementsByTagName("images").item(0);
								el = (org.w3c.dom.Element) nds;
								nodes = el.getElementsByTagName("property");
								
								for (int n = 0; n < nodes.getLength(); n++) {
									org.w3c.dom.Node nNode = nodes.item(n);
									if (nNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
										org.w3c.dom.Element nElmnt = (org.w3c.dom.Element) nNode;
										addStyleTag(style, nNode, nElmnt);
									}
								}
								
								nds = fstElmnt.getElementsByTagName("font").item(0);
								el = (org.w3c.dom.Element) nds;
								nodes = el.getElementsByTagName("property");
								
								for (int n = 0; n < nodes.getLength(); n++) {
									org.w3c.dom.Node nNode = nodes.item(n);
									if (nNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
										org.w3c.dom.Element nElmnt = (org.w3c.dom.Element) nNode;
										addStyleTag(style, nNode, nElmnt);
									}
								}
								
								nds = fstElmnt.getElementsByTagName("effects").item(0);
								el = (org.w3c.dom.Element) nds;
								nodes = el.getElementsByTagName("property");
								
								for (int n = 0; n < nodes.getLength(); n++) {
									org.w3c.dom.Node nNode = nodes.item(n);
									if (nNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
										org.w3c.dom.Element nElmnt = (org.w3c.dom.Element) nNode;
										addStyleTag(style, nNode, nElmnt);
									}
								}
							} catch (Exception ex) {
								System.err.println("Problem parsing attributes: " + ex);
							}
							styles.put(key, style);
						}
					}
					if (file != null)
						file.close();
				} catch (Exception ex) {
					System.err.println("Problem loading control definition: " + ex);
				}
			}
			
			if (file != null)
				file.close();
			
			doc = null;
			db = null;
			dbf = null;
		} catch (Exception e) {
			System.err.println("Problem loading style map: " + e);
		}
		/*
		Set<String> keys = styles.keySet();
		for (String key : keys) {
			System.out.println("Reading style: " + key);
			Style s = styles.get(key);
			System.out.println(s.getVector4f("resizeBorders"));
		}
		*/
	}
	
	private void addStyleTag(Style style, org.w3c.dom.Node nNode, org.w3c.dom.Element nElmnt) {
		String name = XMLHelper.getNodeAttributeValue(nNode, "name");
		String type = XMLHelper.getNodeAttributeValue(nNode, "type");
		System.out.println(name + " : " + type);
		if (type.equals("Vector2f")) {
			style.putTag(
				name,
				new Vector2f(
					Float.parseFloat(XMLHelper.getNodeAttributeValue(nElmnt.getElementsByTagName("x").item(0), "value")),
					Float.parseFloat(XMLHelper.getNodeAttributeValue(nElmnt.getElementsByTagName("y").item(0), "value"))
				)
			);
			System.out.println(style.getVector2f(name));
		} else if (type.equals("Vector3f")) {
			style.putTag(
				name,
				new Vector3f(
					Float.parseFloat(XMLHelper.getNodeAttributeValue(nElmnt.getElementsByTagName("x").item(0), "value")),
					Float.parseFloat(XMLHelper.getNodeAttributeValue(nElmnt.getElementsByTagName("y").item(0), "value")),
					Float.parseFloat(XMLHelper.getNodeAttributeValue(nElmnt.getElementsByTagName("z").item(0), "value"))
				)
			);
		} else if (type.equals("Vector4f")) {
			style.putTag(
				name,
				new Vector4f(
					Float.parseFloat(XMLHelper.getNodeAttributeValue(nElmnt.getElementsByTagName("x").item(0), "value")),
					Float.parseFloat(XMLHelper.getNodeAttributeValue(nElmnt.getElementsByTagName("y").item(0), "value")),
					Float.parseFloat(XMLHelper.getNodeAttributeValue(nElmnt.getElementsByTagName("z").item(0), "value")),
					Float.parseFloat(XMLHelper.getNodeAttributeValue(nElmnt.getElementsByTagName("w").item(0), "value"))
				)
			);
		} else if (type.equals("ColorRGBA")) {
			style.putTag(
				name,
				new ColorRGBA(
					Float.parseFloat(XMLHelper.getNodeAttributeValue(nElmnt.getElementsByTagName("r").item(0), "value")),
					Float.parseFloat(XMLHelper.getNodeAttributeValue(nElmnt.getElementsByTagName("g").item(0), "value")),
					Float.parseFloat(XMLHelper.getNodeAttributeValue(nElmnt.getElementsByTagName("b").item(0), "value")),
					Float.parseFloat(XMLHelper.getNodeAttributeValue(nElmnt.getElementsByTagName("a").item(0), "value"))
				)
			);
		} else if (type.equals("float")) {
			style.putTag(
				name,
				Float.parseFloat(XMLHelper.getNodeAttributeValue(nNode, "value"))
			);
		} else if (type.equals("int")) {
			style.putTag(
				name,
				Integer.parseInt(XMLHelper.getNodeAttributeValue(nNode, "value"))
			);
		} else if (type.equals("boolean")) {
			style.putTag(
				name,
				Boolean.parseBoolean(XMLHelper.getNodeAttributeValue(nNode, "value"))
			);
		} else if (type.equals("String")) {
			style.putTag(
				name,
				XMLHelper.getNodeAttributeValue(nNode, "value")
			);
		} else if (type.equals("Effect")) {
			style.putTag(
				name,
				new Effect(
					Effect.EffectType.valueOf(XMLHelper.getNodeAttributeValue(nElmnt.getElementsByTagName("effect").item(0), "value")),
					Effect.EffectEvent.valueOf(XMLHelper.getNodeAttributeValue(nElmnt.getElementsByTagName("event").item(0), "value")),
					Float.parseFloat(XMLHelper.getNodeAttributeValue(nElmnt.getElementsByTagName("speed").item(0), "value"))
				)
			);
		}
	}
	
	/**
	 * Returns the Style object associated to the provided key
	 * @param key The String key of the Style
	 * @return Style style
	 */
	public Style getStyle(String key) {
		return styles.get(key);
	}
	
	// Menu handling
	private void handleMenuState() {
		if (eventElement == null) {
			Set<String> keys = elements.keySet();
			for (String key :keys) {
				if (elements.get(key) instanceof Menu) {
					elements.get(key).hide();
				}
			}
		} else {
			if (!(eventElement.getAbsoluteParent() instanceof Menu)) {
				Set<String> keys = elements.keySet();
				for (String key :keys) {
					if (elements.get(key) instanceof Menu) {
						elements.get(key).hide();
					}
				}
			} else {
				Set<String> keys = elements.keySet();
				for (String key :keys) {
					if (elements.get(key) instanceof Menu && elements.get(key) != eventElement.getAbsoluteParent()) {
						elements.get(key).hide();
					}
				}
			}
		}
	}
	
	// Forms and tab focus
	/**
	 * Method for setting the tab focus element
	 * @param element The Element to set tab focus to
	 */
	public void setTabFocusElement(Element element) {
		resetFocusElement();
		focusForm = element.getForm();
		if (focusForm != null) {
			tabFocusElement = element;
			focusForm.setSelectedTabIndex(element);
			if (tabFocusElement instanceof TabFocusListener) {
				((TabFocusListener)element).setTabFocus();
			}
		}
	}
	
	/**
	 * Resets the tab focus element to null after calling the TabFocusListener's
	 * resetTabFocus method.
	 */
	public void resetTabFocusElement() {
		resetFocusElement();
		this.tabFocusElement = null;
		this.focusForm = null;
	}
	
	private void resetFocusElement() {
		if (tabFocusElement != null) {
			if (tabFocusElement instanceof TabFocusListener) {
				((TabFocusListener)tabFocusElement).resetTabFocus();
			}
		}
	}
	
	/**
	 * Sets the current Keyboard focus Element
	 * @param element The Element to set keyboard focus to
	 */
	public void setKeyboardElemeent(Element element) {
		keyboardElement = element;
	}
}
