package org.opensails.sails.adapter;

import org.opensails.rigging.IScopedContainer;

/**
 * Resovles IAdapter instances for a parameter class. If the IAdapter
 * implementation is annotated with
 * {@link org.opensails.sails.Scope), it will be instantiated using the provided {@link org.opensails.rigging.IScopedContainer}
 * 
 * @author aiwilliams
 */
public interface IAdapterResolver {
    IAdapter resolve(Class<?> parameterClass, IScopedContainer container);
}
