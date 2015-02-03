package org.ginsim.service.tool.maximalsymbolicsteadystates;

import java.util.Arrays;


public class PrimeImplicant {
    
    public final int[] p;
    public final int c;
    public final int v;
    
    public PrimeImplicant(int[] p, int c, int v) {
	this.p = p;
	this.c = c;
	this.v = v;
    }
    
    public String toString() {
	return "(" + Arrays.toString(p) + ", " + c + ", " + v + ")";
    }
    
}
