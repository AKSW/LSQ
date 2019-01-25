package org.aksw.simba.lsq.util;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/**
 * Helper class to conveniently create nested resources
 *
 * Resources in the given model are only created upon calling .get().
 *
 * For that reason, 'current' merely acts as a cache
 * current.getModel() should always equal 'model'.
 *
 * @author raven
 *
 */
public class NestedResource {
    protected String str;
    protected Resource current = null;
    protected Model model;
    protected NestedResource parent;

    public NestedResource(Model model, String str) {
        this(null, model, str, null);
    }

    // Use NestedResource.from(..) instead
    public NestedResource(Resource current) {
        this(null, current.getModel(), current.getURI(), current);
    }

    // Avoid calling this ctor directly
    public NestedResource(NestedResource parent, Model model, String str, Resource current) {
        this.parent = parent;
        this.model = model;
        this.str = str;
        this.current = current;
    }

    /**
     * A resource instance is only added to the model when .get() is called
     *
     * @return
     */
    public Resource get() {
        if(current == null) {
            current = model.createResource(str);
        }
        return current;
    }

    public String str() {
        return str;
    }

    public NestedResource nest(String name) {
        NestedResource result = name.isEmpty()
                ? this
                : new NestedResource(this, model, str + name, null);

        return result;
    }


    public static NestedResource from(Resource r) {
        return new NestedResource(r);
    }
    
    public static NestedResource from(Model model, String baseUri) {
        return new NestedResource(model, baseUri);
    }

}
