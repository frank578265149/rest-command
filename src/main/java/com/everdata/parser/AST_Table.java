/* Generated By:JJTree: Do not edit this line. AST_Table.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.everdata.parser;

public
class AST_Table extends SimpleNode {
  
  public AST_Table(int id) {
    super(id);
  }

  public AST_Table(CommandParser p, int id) {
    super(p, id);
  }
  
  public String[] getTables(){
	  if( children.length == 0)
		  return null;
	  else{
		  return ((AST_IdentList) children[0]).getNames();
	  }		  
  }

}
/* JavaCC - OriginalChecksum=9f4141fe2732946f07c2bfd9950441a9 (do not edit this line) */