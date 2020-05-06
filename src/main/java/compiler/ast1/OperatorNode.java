package compiler.ast1;

import compiler.lexer.tokentypes.TokenType.OpTokenType;
import compiler.visitor.Visitor;

public class OperatorNode implements ExprNode {
	
	public OpTokenType type;

	public OperatorNode(OpTokenType type) {
		this.type = type;
	}
	
	@Override
	public void accept(Visitor v) {
		// TODO Auto-generated method stub

	}
}
