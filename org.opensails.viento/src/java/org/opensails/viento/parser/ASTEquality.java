/* Generated By:JJTree: Do not edit this line. ASTEquality.java */

package org.opensails.viento.parser;

public class ASTEquality extends SimpleNode {
  public ASTEquality(int id) {
    super(id);
  }

  public ASTEquality(Parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}