/* Generated By:JJTree: Do not edit this line. ASTBody.java */

package org.opensails.viento.parser;

public class ASTBody extends SimpleNode {
	public ASTBody(int id) {
		super(id);
	}

	public ASTBody(Parser p, int id) {
		super(p, id);
	}

	/** Accept the visitor. * */
	public Object jjtAccept(ParserVisitor visitor, Object data) {
		childrenAccept(visitor, data);
		return visitor.visit(this, data);
	}
}