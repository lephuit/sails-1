/* Generated By:JJTree: Do not edit this line. ASTCall.java */

package org.opensails.viento.parser;

public class ASTCall extends SimpleNode {
  public ASTCall(int id) {
    super(id);
  }

  public ASTCall(Parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}