package org.ginsim.service.tool.maximalsymbolicsteadystates;

import java.util.Arrays;


public class Implicant {
    
    public final int[] p;
    public final int c;
    public final int v;
    
    public Implicant(int[] p, int c, int v) {
	this.p = p;
	this.c = c;
	this.v = v;
    }
    
    public String toString() {
	return "(" + Arrays.toString(p) + ", " + c + ", " + v + ")";
    }
    
    @Override
    public boolean equals (Object o) {
	if (!(o instanceof Implicant)) {
	    return false;
	} else {
	    Implicant implicant = (Implicant) o;
	    boolean b_p = Arrays.equals(this.p, implicant.p);
	    boolean b_c = (this.c == implicant.c);
	    boolean b_v = (this.v == implicant.v);
	    return (b_p && b_c && b_v);
	}
    }
    
    @Override
    public int hashCode() {
	return (this.c * 31 + this.v);
    }

}
