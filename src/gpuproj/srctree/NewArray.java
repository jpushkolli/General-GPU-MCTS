package gpuproj.srctree;

import java.util.ArrayList;
import java.util.List;

public class NewArray extends Expression
{
    public ArraySymbol type;
    /**
     * Is guaranteed to have the same length as the dimensionality of the array
     * will have null contents for unspecified dimensions
     */
    public List<Expression> dimensions;
    public InitialiserList init;

    public NewArray(ArraySymbol type) {
        this.type = type;
        dimensions = new ArrayList<Expression>(type.dimension());
    }

    @Override
    public TypeRef returnType() {
        return new TypeRef(type);
    }

    @Override
    public int precedence() {
        return 2;
    }

    public TypeSymbol componentType() {
        TypeSymbol component = type;
        while(component instanceof ArraySymbol)
            component = ((ArraySymbol) component).type;

        return component.concrete();
    }

    public void printDimensions(StringBuilder sb) {
        for(Expression exp : dimensions) {
            sb.append('[');
            if(exp != null)
                sb.append(exp);
            sb.append(']');
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("new ").append(componentType().fullname);
        printDimensions(sb);

        if(init != null)
            sb.append(init);

        return sb.toString();
    }

    @Override
    public NewArray copy(Scope scope) {
        NewArray copy = new NewArray(type);
        copy.dimensions = new ArrayList<Expression>(dimensions.size());
        for(Expression exp : dimensions)
            copy.dimensions.add(exp == null ? null : exp.copy(scope));

        if(init != null)
            copy.init = init.copy(scope);

        return copy;
    }
}
