package org.codehaus.groovy.binding;

import java.util.LinkedHashSet;

/**
 * An aggregation of multiple bindings
 * @author Danno Ferrin
 * @since Groovy 1.6
 */
public class AggregateBinding implements BindingUpdatable {

    protected  boolean bound;

    // use linked hash set so order is preserved
    protected LinkedHashSet<BindingUpdatable> bindings = new LinkedHashSet();

    public void addBinding(BindingUpdatable binding) {
        if (bound) binding.bind(); // bind is idempotent, so no state checking
        bindings.add(binding);
    }

    public void removeBinding(BindingUpdatable binding) {
        bindings.remove(binding);
    }

    public void bind() {
        if (!bound) {
            bound = true;
            for (BindingUpdatable binding : bindings) {
                binding.bind();
            }
        }
    }

    public void unbind() {
        if (!bound) {
            for (BindingUpdatable binding : bindings) {
                binding.unbind();
            }
            bound = false;
        }
    }

    public void rebind() {
        if (bound) {
            unbind();
            bind();
        }
    }

    public void update() {
        for (BindingUpdatable binding : bindings) {
            binding.update();
        }
    }

    public void reverseUpdate() {
        for (BindingUpdatable binding : bindings) {
            binding.reverseUpdate();
        }
    }
}
