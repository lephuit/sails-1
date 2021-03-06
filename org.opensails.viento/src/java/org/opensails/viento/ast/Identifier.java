package org.opensails.viento.ast;

import org.opensails.viento.VientoVisitor;
import org.opensails.viento.parser.Token;

public class Identifier extends Node {
	protected String value;
	
	public Identifier(Token t) {
		token(t);
		value = t.image;
	}

	public String evaluate() {
		return value;
	}
	
	public void visit(VientoVisitor visitor) {
		visitor.visit(this);
	}
}
