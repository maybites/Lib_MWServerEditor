/*
 * Created on 14.05.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package org.meshwarpserver.util;

/**
 * @author mf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class random {

	public static float max(float howbig) {
	    // for some reason (rounding error?) Math.random() * 3
	    // can sometimes return '3' (once in ~30 million tries)
	    // so a check was added to avoid the inclusion of 'howbig'
	
	    // avoid an infinite loop
	    if (howbig == 0) return 0;
	
	    float value = 0;
	    do {
	      value = (float)Math.random() * howbig;
	    } while (value == howbig);
	    return value;
	}


  /**
   * Return a random number in the range [howsmall, howbig).
   * <P>
   * The number returned will range from 'howsmall' up to
   * (but not including 'howbig'.
   * <P>
   * If howsmall is >= howbig, howsmall will be returned,
   * meaning that random(5, 5) will return 5 (useful)
   * and random(7, 4) will return 7 (not useful.. better idea?)
   */
	public static float range(float howsmall, float howbig) {
	    if (howsmall >= howbig) return howsmall;
	    float diff = howbig - howsmall;
	    return max(diff) + howsmall;
	}

}
