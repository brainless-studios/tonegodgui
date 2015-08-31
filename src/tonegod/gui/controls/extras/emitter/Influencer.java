/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tonegod.gui.controls.extras.emitter;

import tonegod.gui.controls.extras.emitter.ElementEmitter.ElementParticle;

/**
 *
 * @author t0neg0d
 */
public interface Influencer {
	public void update(ElementParticle particle, float tpf);
	
	public void initialize(ElementParticle particle);
	
	public void setIsEnabled(boolean isEnabled);
	
	public boolean getIsEnabled();
}
