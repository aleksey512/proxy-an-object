package info.biosfood.proxy.security;

import net.sf.cglib.proxy.Enhancer;

public class SecuredObjectFactory {

    public static SecuredObject create() {
        return (SecuredObject) Enhancer.create(SecuredObject.class, new SecuredObjectMethodInterceptor());
    }

}
