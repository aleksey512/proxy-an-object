package info.biosfood.proxy.security;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class SecuredObjectMethodInterceptor implements MethodInterceptor {

    @Override
    public Object intercept(Object o, Method method, Object[] arguments, MethodProxy methodProxy) throws Throwable {
        if(isContextRoleEnoughToInvoke(getRequiredRole(method))) {
            return methodProxy.invokeSuper(o, arguments);
        } else {
            throw new SecurityException(
                String.format(
                    "User %s is not allowed to call a method %s",
                    Context.getInstance().getUserName(),
                    method.getName()
                )
            );
        }
    }

    protected SecurityRole getRequiredRole(Method method) {
        Security security = method.getAnnotation(Security.class);

        return security == null ? SecurityRole.DEFAULT : security.role();
    }

    protected boolean isContextRoleEnoughToInvoke(SecurityRole methodsLowestSecurityRole) {
        return isRoleEnoughToInvoke(Context.getInstance().getSecurityRole(), methodsLowestSecurityRole);
    }

    protected boolean isRoleEnoughToInvoke(SecurityRole hasSecurityRole, SecurityRole methodsLowestSecurityRole) {
        return hasSecurityRole.getLevel() >= methodsLowestSecurityRole.getLevel();
    }


}
