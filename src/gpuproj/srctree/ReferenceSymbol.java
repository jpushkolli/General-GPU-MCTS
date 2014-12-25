package gpuproj.srctree;

import gpuproj.srctree.Scope.ScopeProvider;

import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public abstract class ReferenceSymbol extends TypeSymbol implements ScopeProvider
{
    public static final int ANNOTATION = 0x00002000;
    public static final int ENUM = 0x00004000;

    public final Scope scope;
    public final Object source;
    public int modifiers;
    public List<TypeParam> typeParams = new LinkedList<>();
    public TypeRef parent;
    public List<TypeRef> interfaces = new LinkedList<>();
    public List<ReferenceSymbol> innerClasses = new LinkedList<>();
    public List<FieldSymbol> fields = new LinkedList<>();
    public List<MethodSymbol> methods = new LinkedList<>();

    public ReferenceSymbol(String fullname, Scope scope, Object source) {
        super(fullname);
        this.scope = new Scope(scope, this);
        this.source = source;
        TypeIndex.instance.register(this);
    }

    public ReferenceSymbol load() {
        loadSymbols();
        loadSignatures();
        return this;
    }

    public abstract ReferenceSymbol loadSymbols();
    public abstract ReferenceSymbol loadSignatures();

    public FieldSymbol getField(String name) {
        LinkedList<Symbol> list = new LinkedList<>();
        resolveOnce(name, FIELD_SYM, list);
        return list.isEmpty() ? null : (FieldSymbol)list.getFirst();
    }

    public List<MethodSymbol> getMethods(String name) {
        LinkedList<Symbol> list = new LinkedList<>();
        resolveOnce(name, METHOD_SYM, list);
        return (List<MethodSymbol>)(List)list;
    }

    @Override
    public void resolveOnce(String name, int type, List<Symbol> list) {
        if((type & FIELD_SYM) != 0 && list.isEmpty()) {//fields are shadowed by subclasses
            for(FieldSymbol sym : fields)
                if(sym.name.equals(name))
                    list.add(sym);
        }
        if((type & METHOD_SYM) != 0) {
            for(MethodSymbol sym : methods)
                if(sym.name.equals(name))
                    list.add(sym);
        }
        if((type & CLASS_SYM) != 0) {
            for (ReferenceSymbol sym : innerClasses)
                if (sym.name.equals(name))
                    list.add(sym);
        }
        if((type & TYPE_PARAM) != 0) {
            for(TypeParam p : typeParams)
                if(p.name.equals(name))
                    list.add(p);
        }

        if(parent != null)
            parent.refType().resolveOnce(name, type, list);

        for(TypeRef iface : interfaces)
            iface.refType().resolveOnce(name, type, list);

    }

    @Override
    public TypeSymbol concrete() {
        return this;
    }

    @Override
    public int getType() {
        return Symbol.CLASS_SYM;
    }

    @Override
    public String signature() {
        return 'L'+fullname.replace('.', '/')+';';
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(modifiers != 0)
            sb.append(Modifier.toString(modifiers)).append(' ');

        if((modifiers & ENUM) != 0)
            sb.append("enum ");
        else if((modifiers & ANNOTATION) != 0)
            sb.append("@interface ");
        else if(!isInterface())
            sb.append("class ");

        sb.append(fullname);

        if(!typeParams.isEmpty())
            sb.append('<').append(SourceUtil.listString(typeParams)).append('>');
        if(parent != null && !parent.type.fullname.equals("java.lang.Object"))
            sb.append(" extends ").append(parent);
        if(!interfaces.isEmpty())
            sb.append(" implements ").append(SourceUtil.listString(interfaces));

        return sb.toString();
    }

    public boolean isInterface() {
        return (modifiers & Modifier.INTERFACE) != 0;
    }
}