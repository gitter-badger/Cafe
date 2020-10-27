package cafelang.ir;

import java.util.LinkedHashMap;
import java.util.Map;

public class ObjectCreationStatement extends ExpressionStatement<ObjectCreationStatement>{
    private Map<String, ExpressionStatement<?>> map;

    private ObjectCreationStatement(Map<String, ExpressionStatement<?>> map){
        this.map = map;
    }

    public static ObjectCreationStatement of(Map<String, ExpressionStatement<?>> map){
        return new ObjectCreationStatement(map);
    }

    public Map<String, ExpressionStatement<?>> getMap() {
        return map;
    }

    @Override
    protected ObjectCreationStatement self() {
        return this;
    }

    @Override
    public void accept(CafeIrVisitor visitor) {
        visitor.visitObjectCreation(this);
    }
}
