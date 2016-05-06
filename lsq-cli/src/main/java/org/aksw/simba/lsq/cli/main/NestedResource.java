package org.aksw.simba.lsq.cli.main;
import org.apache.jena.rdf.model.Resource;

public class NestedResource {
    protected String str;
    protected Resource current = null;
    protected NestedResource parent;

    public NestedResource(String str) {
        this(null, str);
    }

    public NestedResource(Resource current) {
        this(null, current.getURI());
        this.current = current;
    }

    public NestedResource(NestedResource parent, String str) {
        this.parent = parent;
        this.str = str;
    }

    /**
     * A resource instance is only added to the model when .get() is called
     *
     * @return
     */
    public Resource get() {
        if(current == null) {
            Resource result = current.getModel().createResource(str);
            return result;
        }
        return current;
    }

    public String str() {
        return str;
    }

    public NestedResource nest(String name) {
        NestedResource result = name.isEmpty()
                ? this
                : new NestedResource(this, str + name);

        return result;
    }
}
