package gpuproj.srctree;

import java.util.LinkedList;
import java.util.List;

/**
 * A reference to a TypeSymbol, specifies values for parameters
 * Can be modified to represent a pointer, and corresponding C class
 */
public class TypeRef
{
    /**
     * If this flag is set, TypeRefs, statements and expressions will print openCL code instead of Java code with toString methods
     */
    public static boolean printCL = false;

    //OpenCL reference modifiers
    public static final int GLOBAL = 1;
    public static final int LOCAL = 2;
    public static final int CONSTANT = 4;
    public static final int UNSIGNED = 8;

    /**
     * The type being referenced
     */
    public final TypeSymbol type;
    /**
     * Values for the parameters of type. May be empty for unspecified parameters
     */
    public List<TypeRef> params = new LinkedList<>();
    /**
     * Pointer level. Number of asterisks before this
     */
    public int pointer;
    /**
     * OpenCL modifiers
     */
    public int modifiers;

    public TypeRef(TypeSymbol type) {
        if(type == null) throw new IllegalArgumentException("Null type");
        this.type = type;
    }

    /**
     * Chaining setter for pointer field
     */
    public TypeRef point(int i) {
        pointer = i;
        return this;
    }

    /**
     * Chaining setter for modifiers field
     */
    public TypeRef modify(int i) {
        modifiers = i;
        return this;
    }

    /**
     * @return type casted to ReferenceSymbol
     */
    public ReferenceSymbol refType() {
        return (ReferenceSymbol)type;
    }

    /**
     * @return type casted to ClassSymbol
     */
    public ClassSymbol classType() {
        return (ClassSymbol) type;
    }

    /**
     * @return A TypeRef for the component type of this, assuming this references an ArraySymbol
     */
    public TypeRef componentRef() {
        return new TypeRef(((ArraySymbol)type).type);
    }

    /**
     * @return A TypeRef to type.array()
     */
    public TypeRef arrayRef() {
        return new TypeRef(type.array());
    }

    /**
     * @return type.concrete(). The type this reference would be compiled to
     */
    public TypeSymbol concrete() {
        return type.concrete();
    }

    /**
     * Replaces TypeParam references with specific type references.
     * If type is a TypeParam, and specified.type is equal to the owner of this TypeParam, then returns the TypeRef of specific that is assigned to this parameter
     * eg, if type is T from Iterable and specifier is Iterable<String>, returns String
     *
     * @param specifier A reference to a declaration of a generic type wth arguments eg, Iterable<String>
     * @return a more specific reference if found, otherwise this
     */
    public TypeRef specify(TypeRef specifier) {
        if(specifier.params.isEmpty())
            return this;

        if(type instanceof TypeParam) {
            TypeParam param = (TypeParam) type;
            if(param.owner == specifier.type)
                return specifier.params.get(param.owner.getTypeParams().indexOf(param));
        }

        return this;
    }

    /**
     * The compiled signature of this reference
     */
    public String signature() {
        return type.signature();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if(printCL) {
            if((modifiers & GLOBAL) != 0) sb.append("global ");
            else if((modifiers & LOCAL) != 0) sb.append("local ");
            else if((modifiers & CONSTANT) != 0) sb.append("constant ");
            if((modifiers & UNSIGNED) != 0) sb.append("unsigned ");

            sb.append(type.fullname.replaceFirst("\\[\\]", "*"));
            for(int i = 0; i < pointer; i++)
                sb.append('*');
        } else {
            sb.append(type.fullname);
            if (!params.isEmpty())
                sb.append('<').append(SourceUtil.listString(params)).append('>');
        }

        return sb.toString();
    }

    /**
     * Creates or extracts a TypeRef based on the type of o
     * If o is a TypeRef, returns o
     * If o is a TypeSymbol, returns new TypeRef(o)
     * If o is an Expression, returns o.returnType()
     * If o is a Variable, returns o.getType()
     * If o is a String, returns new TypeRef(TypeIndex.resolveType(o))
     * Otherwise returns null
     */
    public static TypeRef get(Object o) {
        if(o instanceof TypeRef)
            return (TypeRef) o;
        if(o instanceof TypeSymbol)
            return new TypeRef((TypeSymbol) o);
        if(o instanceof Expression)
            return ((Expression) o).returnType();
        if(o instanceof Variable)
            return ((Variable) o).getType();
        if(o instanceof String)
            return new TypeRef(TypeIndex.resolveType((String) o));

        return null;
    }
}
