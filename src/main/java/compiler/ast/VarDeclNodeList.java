package compiler.ast;

import java.util.Vector;

public class VarDeclNodeList {
	private Vector<VarDeclNode> list;

	public VarDeclNodeList() {
		list = new Vector<>();
	}

	public void addElement(VarDeclNode n) {
		list.addElement(n);
	}

	public VarDeclNode elementAt(int i) {
		return list.elementAt(i);
	}

	public int size() {
		return list.size();
	}
}
