package compiler.gen;

import compiler.ast.Node;
import compiler.cafelang.ir.*;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

public class ASTToCafeIrVisitor implements Node.Visitor {

    private static final class Context{
        final static Context context = new Context();

        public CafeModule module;
        private final Deque<ReferenceTable> referenceTableStack = new LinkedList<>();
        private final Deque<Deque<Object>> objectStack = new LinkedList<>();
        private boolean isModuleScope = true;

        private Context(){}

        public CafeModule createModule(String name){
            ReferenceTable global = new ReferenceTable();
            referenceTableStack.push(global);
            module = CafeModule.create(name,global);
            return module;
        }

        public Block enterScope(){
            ReferenceTable blockReferenceTable = referenceTableStack.peek().fork();
            referenceTableStack.push(blockReferenceTable);
            isModuleScope = false;
            return Block.create(blockReferenceTable);
        }

        public void leaveScope(){
            referenceTableStack.pop();
            isModuleScope = true;
        }

        public void newObjectStack() {
            objectStack.push(new LinkedList<>());
        }

        public void popObjectStack() {
            objectStack.pop();
        }

        public void push(Object object) {
            if (objectStack.isEmpty()) {
                newObjectStack();
            }
            objectStack.peek().push(object);
        }

        public Object pop(){
            return objectStack.peek().pop();
        }

        public Object peek(){
            return objectStack.peek();
        }

        SymbolReference createSymbolReference(String name, SymbolReference.Kind kind){
            SymbolReference ref = SymbolReference.of(name, kind);
            referenceTableStack.peek().add(ref);
            return ref;
        }

        public SymbolReference createSymbolReference(String name, Class<?> clazz){
            return createSymbolReference(name,getSymbolKind(clazz));
        }

        SymbolReference.Kind getSymbolKind(Class<?> clazz){
            if(clazz == Node.VarDeclNode.class) {
                if (isModuleScope)
                    return SymbolReference.Kind.GLOBAL_VAR;
                else
                    return SymbolReference.Kind.VAR;
            }
            else if(clazz == Node.ConstDeclNode.class){
                if(isModuleScope)
                    return SymbolReference.Kind.GLOBAL_CONST;
                else
                    return SymbolReference.Kind.CONST;
            }
            throw new AssertionError("Invalid Symbol Kind");
        }
    }

    private <T extends Node> void iterChildren(Collection<T> nodes){
        for(T child: nodes)
            child.accept(this);
    }

    public CafeModule transform(Node.ProgramNode n,String name){
        Context.context.createModule(name);
        Context.context.newObjectStack();
        visitProgram(n);
        return Context.context.module;
    }

    @Override
    public void visitProgram(Node.ProgramNode n) {
        Context context = Context.context;
        for(Node.StmtNode stmt: n.stmts){
            stmt.accept(this);
            context.module.add((CafeStatement) context.pop());
        }
    }

    @Override
    public void visitVarDecl(Node.VarDeclNode n) {
        Node.IdenNode iden = n.var;
        SymbolReference sym = Context.context.createSymbolReference(iden.name, Node.VarDeclNode.class);

        if(n.value != null)
            n.value.accept(this);
        else
            Context.context.push(null);
        AssignmentStatement stmt = AssignmentStatement.create(sym,Context.context.pop(),true);
        Context.context.push(stmt);
    }

    @Override
    public void visitIden(Node.IdenNode n) {
        Context context = Context.context;
        context.push(ReferenceLookup.of(n.name));
    }

    @Override
    public void visitConstDecl(Node.ConstDeclNode n) {

    }

    @Override
    public void visitNumLit(Node.NumLitNode n) {
        ConstantStatement c = ConstantStatement.of(n.lit);
        Context.context.push(c);
    }

    @Override
    public void visitStrLit(Node.StrLitNode n) {
        ConstantStatement c = ConstantStatement.of(n.lit);
        Context.context.push(c);
    }

    @Override
    public void visitBoolLit(Node.BoolLitNode n) {
        ConstantStatement c = ConstantStatement.of(n.lit);
        Context.context.push(c);
    }

    @Override
    public void visitFuncDecl(Node.FuncDeclNode n) {

    }

    @Override
    public void visitObjCreation(Node.ObjCreationNode n) {

    }

    @Override
    public void visitBlock(Node.BlockNode n) {

    }

    @Override
    public void visitAnnFunc(Node.AnnFuncNode n) {

    }

    @Override
    public void visitListColl(Node.ListCollNode n) {

    }

    @Override
    public void visitSetColl(Node.SetCollNode n) {

    }

    @Override
    public void visitLinkColl(Node.LinkCollNode n) {

    }

    @Override
    public void visitMapColl(Node.MapCollNode n) {

    }

    @Override
    public void visitBinaryExpr(Node.BinaryExprNode n) {
        n.e1.accept(this);
        n.e2.accept(this);
        BinaryExpression expr = BinaryExpression.of(n.op)
                .left(Context.context.pop())
                .right(Context.context.pop());
    }

    @Override
    public void visitUnaryExpr(Node.UnaryExprNode n) {
        n.e.accept(this);
        UnaryExpression expr = UnaryExpression.create(
                n.op,
                Context.context.pop()
        );
    }

    @Override
    public void visitThis(Node.ThisNode n) {

    }

    @Override
    public void visitNull(Node.NullNode n) {

    }

    @Override
    public void visitFuncCall(Node.FuncCallNode n) {

    }

    @Override
    public void visitSubscript(Node.SubscriptNode n) {

    }

    @Override
    public void visitObjAccess(Node.ObjectAccessNode n) {

    }

    @Override
    public void visitSlice(Node.SliceNode n) {

    }

    @Override
    public void visitArgsList(Node.ArgsListNode n) {

    }

    @Override
    public void visitParamList(Node.ParameterListNode n) {

    }

    @Override
    public void visitImportStmt(Node.ImportStmtNode n) {

    }

    @Override
    public void visitAsgnStmt(Node.AsgnStmtNode n) {

    }

    @Override
    public void visitIfStmt(Node.IfStmtNode n) {

    }

    @Override
    public void visitElseStmt(Node.ElseStmtNode n) {

    }

    @Override
    public void visitForStmt(Node.ForStmtNode n) {

    }

    @Override
    public void visitLoopStmt(Node.LoopStmtNode n) {

    }

    @Override
    public void visitReturnStmt(Node.ReturnStmtNode n) {

    }

    @Override
    public void visitContinueStmt(Node.ContinueStmtNode n) {

    }

    @Override
    public void visitBreakStmt(Node.BreakStmtNode n) {

    }

    @Override
    public void visitListComp(Node.ListCompNode n) {

    }

    @Override
    public void visitLinkComp(Node.LinkCompNode n) {

    }

    @Override
    public void visitSetComp(Node.SetCompNode n) {

    }

    @Override
    public void visitMapComp(Node.MapCompNode n) {

    }

    @Override
    public void visitCompLoop(Node.CompLoopNode n) {

    }

    @Override
    public void visitCompIf(Node.CompIfNode n) {

    }

    @Override
    public void visitListRange(Node.RangeNode n) {

    }
}
