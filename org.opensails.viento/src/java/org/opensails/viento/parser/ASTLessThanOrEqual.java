/* Generated By:JJTree: Do not edit this line. ASTLessThanOrEqual.java */

package org.opensails.viento.parser;

public class ASTLessThanOrEqual extends SimpleNode {
  public ASTLessThanOrEqual(int id) {
    super(id);
  }

  public ASTLessThanOrEqual(Parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
