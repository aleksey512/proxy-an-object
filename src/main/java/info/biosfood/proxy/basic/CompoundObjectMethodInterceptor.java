package info.biosfood.proxy.basic;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;

public class CompoundObjectMethodInterceptor implements MethodInterceptor {

    private static final Logger LOG = Logger.getLogger(CompoundObjectMethodInterceptor.class);

    public Object intercept(Object o, Method method, Object[] arguments, MethodProxy methodProxy) throws Throwable {
        LOG.debug("Message from CompoundObjectMethodInterceptor (CGLIB). Calling method " + method.getName());

        return methodProxy.invokeSuper(o, arguments);
    }

}
