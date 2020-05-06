package compiler.visitor;

import compiler.ast.ArgsNode;
import compiler.ast.BinaryExprNode;
import compiler.ast.Block;
import compiler.ast.ElseStmtNode;
import compiler.ast.FuncCallNode;
import compiler.ast.FuncDeclNode;
import compiler.ast.IdentifierNode;
import compiler.ast.IfElseStmtNode;
import compiler.ast.IfStmtNode;
import compiler.ast.LiteralNode;
import compiler.ast.OperatorNode;
import compiler.ast.ParameterNode;
import compiler.ast.ProgramNode;
import compiler.ast.ReturnStmtNode;
import compiler.ast.UnaryExprNode;
import compiler.ast.VarDeclNode;
import compiler.ast.VarDeclWithAsgnNode;

public interface Visitor {
	void visit(ProgramNode n);
	
	void visit(FuncDeclNode n);
	
	void visit(VarDeclNode n);
	
	void visit(VarDeclWithAsgnNode n);
	
	<T> void visit(ArgsNode<T> n);
	
	void visit (ParameterNode n);
	
	void visit(ReturnStmtNode n);
	
	void visit(LiteralNode n);
	
	void visit(BinaryExprNode n);
	
	void visit(UnaryExprNode n);
	
	void visit(FuncCallNode n);
	
	void visit(IdentifierNode n);
	
	void visit(OperatorNode n);
	
	void visit(IfStmtNode n);
	
	void visit(ElseStmtNode n);
	
	void visit(IfElseStmtNode n);
	
	void visit(Block n);
}
