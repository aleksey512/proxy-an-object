package info.biosfood.proxy.basic;

import org.apache.log4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class CompoundObjectInvocationHandler implements InvocationHandler {

    private static final Logger LOG = Logger.getLogger(CompoundObjectInvocationHandler.class);

    private ICompoundObject proxiedObject;

    public CompoundObjectInvocationHandler(ICompoundObject proxiedObject) {
        this.proxiedObject = proxiedObject;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        LOG.debug("Message from CompoundObjectInvocationHandler (Java reflection). Calling method " + method.getName());

        return method.invoke(proxiedObject, args);
    }
}
