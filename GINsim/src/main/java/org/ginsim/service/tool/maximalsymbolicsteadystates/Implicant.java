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
    
}
