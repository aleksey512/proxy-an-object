# Proxying an object

## Introduction
In this article I will show how in Java is possible to control an object. It's very useful thing when you have an elegant 
way how to control which and when the method is called. It allows us to control creating process of huge objects which 
we don't need them entirely right now. The approach is used in many popular frameworks like
Hibernate, Spring, Mockito, JMockit and other frameworks. The cornerstone of the approach is `Proxy pattern`.

### Pattern proxy

*Proxy pattern* description from [www.oodesign.com](http://www.oodesign.com/proxy-pattern.html)

> Sometimes we need the ability to control the access to an object. For example if we need to use only a few methods 
> of some costly objects we'll initialize those objects when we need them entirely. Until that point we can use some 
> light objects exposing the same interface as the heavy objects. These light objects are called proxies and they will 
> instantiate those heavy objects when they are really need and by then we'll use some light objects instead.
> 
> This ability to control the access to an object can be required for a variety of reasons: controlling when a costly 
> object needs to be instantiated and initialized, giving different access rights to an object, as well as providing 
> a sophisticated means of accessing and referencing objects running in other processes, on other machines.
> 
> Consider for example an image viewer program. An image viewer program must be able to list and display high resolution 
> photo objects that are in a folder, but how often do someone open a folder and view all the images inside. 
> Sometimes you will be looking for a particular photo, sometimes you will only want to see an image name. 
> The image viewer must be able to list all photo objects, but the photo objects must not be loaded into memory until 
> they are required to be rendered.

### Options for implementation
In this article I will discuss two approaches how to proxy an object in Java. First way is Java Reflection tools 
and second way is CGLIB framework which is more handy then Java Reflection in my opinion.

### Proxy an object with Java Reflection
To proxy an object with Java Reflection consist of three steps. In the first step I have to implement an interface. 
In the second step I have to create a class which implement the interface.

##### Interface
```java
package info.biosfood.proxy.basic;

public interface ICompoundObject {

    String doSomething();
    
    String doSomethingWithArguments(int arg1, String arg2);

}
```

##### Implementation of the interface
```java
package info.biosfood.proxy.basic;

import org.apache.log4j.Logger;

public class CompoundObject implements ICompoundObject {
    
    private static final Logger LOG = Logger.getLogger(CompoundObject.class);
    
    public String doSomething() {
        LOG.debug("** We are in " + Thread.currentThread().getStackTrace()[1].getMethodName());
        
        return Thread.currentThread().getStackTrace()[1].getMethodName();
    }
    
    public String doSomethingWithArguments(int arg1, String arg2) {
        LOG.debug("** We are in " + Thread.currentThread().getStackTrace()[1].getMethodName());
        LOG.debug(String.format("Arguments {arg1: %s, arg2: %s}", arg1, arg2));
        
        return Thread.currentThread().getStackTrace()[1].getMethodName();
    }

}
```

Third step is to create an interceptor for the instance of the class.

##### Interceptor
```java
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
```

We are nearly done. Lets implement a sample code of how to assign an interceptor

```java
CompoundObject proxied = new CompoundObject();
ICompoundObject o = (ICompoundObject) Proxy.newProxyInstance(
    CompoundObject.class.getClassLoader(),
    new Class[]{ICompoundObject.class},
    new CompoundObjectInvocationHandler(proxied)
);
```

A snapshot above create a proxied object. I pass a real instance of the class `CompoundObject` to `CompoundObjectInvocationHandler` 
and inside the handler I implemented a log output before the real method is called. The log output shows if something 
is called before calling the real method. I have implemented a test method, take a look below.

```java
class CompoundObjectTest {
    @Test
    public void runTestJavaReflectionProxy() {
        LOG.debug("\n\n-- Testing reflection proxy");
        CompoundObject proxied = new CompoundObject();
        ICompoundObject o = (ICompoundObject) Proxy.newProxyInstance(
        CompoundObject.class.getClassLoader(),
        new Class[]{ICompoundObject.class},
        new CompoundObjectInvocationHandler(proxied)
        );
        
        LOG.debug("~~~~~~~~~~~~~~~~~~~~~");
        LOG.debug("result of calling 'doSomething': " + o.doSomething());
        LOG.debug("~~~~~~~~~~~~~~~~~~~~~");
        LOG.debug("result of calling 'doSomething' with arguments: " + o.doSomethingWithArguments(1, "test"));
        LOG.debug("~~~~~~~~~~~~~~~~~~~~~");
    }
}
```

In the output you can find by message `CompoundObjectInvocationHandler: Message from CompoundObjectInvocationHandler (Java reflection). Calling method doSomething` 
that before invocation the method `doSomething()` an interceptor method is called and only then `doSomething()` is invoked.

```text
DEBUG CompoundObjectTest:

-- Testing reflection proxy
DEBUG CompoundObjectTest: ~~~~~~~~~~~~~~~~~~~~~
DEBUG CompoundObjectInvocationHandler: Message from CompoundObjectInvocationHandler (Java reflection). Calling method doSomething
DEBUG CompoundObject: ** We are in doSomething
DEBUG CompoundObjectTest: result of calling 'doSomething': return value from doSomething
DEBUG CompoundObjectTest: ~~~~~~~~~~~~~~~~~~~~~
DEBUG CompoundObjectInvocationHandler: Message from CompoundObjectInvocationHandler (Java reflection). Calling method doSomethingWithArguments
DEBUG CompoundObject: ** We are in doSomethingWithArguments
DEBUG CompoundObject: Arguments {arg1: 1, arg2: test}
DEBUG CompoundObjectTest: result of calling 'doSomething' with arguments: return value from doSomethingWithArguments
DEBUG CompoundObjectTest: ~~~~~~~~~~~~~~~~~~~~~
```

As you see, create a proxy by Java Reflection requires much code. I don't like that I need to create an interface. 
What if I have existing project and I need to add proxy managing there. It will take much time for that implementation. 
Lets consider the second option - it's more optimistic.

### Another option - CGLIB
CGLIB doesn't require to create an interface, it only requires to have a class to be proxied. Lets take a look below 
at a small snapshot. Quite the same need to do: create a class, an interceptor. But you don't need to create 
an interface for the class and initialization of the proxy is simpler.

```java
CompoundObject o = (CompoundObject) Enhancer.create(
    CompoundObject.class,
    new CompoundObjectMethodInterceptor()
);
```

I have implemented a test method. It looks less complicated with the same outcome.

```java
class CompoundObjectTest {
    @Test
    public void runTestCglib() {
        LOG.debug("\n\n-- Testing CGLIB proxy");
        
        CompoundObject o = (CompoundObject) Enhancer.create(
        CompoundObject.class,
        new CompoundObjectMethodInterceptor()
        );
        
        LOG.debug("~~~~~~~~~~~~~~~~~~~~~");
        LOG.debug("result of calling 'doSomething': " + o.doSomething());
        LOG.debug("~~~~~~~~~~~~~~~~~~~~~");
        LOG.debug("result of calling 'doSomething' with arguments: " + o.doSomethingWithArguments(2, "another test"));
        LOG.debug("~~~~~~~~~~~~~~~~~~~~~");
    }
}
```

```text
DEBUG CompoundObjectTest:

-- Testing CGLIB proxy
DEBUG CompoundObjectTest: ~~~~~~~~~~~~~~~~~~~~~
DEBUG CompoundObjectMethodInterceptor: Message from CompoundObjectMethodInterceptor (CGLIB). Calling method doSomething
DEBUG CompoundObject: ** We are in doSomething
DEBUG CompoundObjectTest: result of calling 'doSomething': return value from doSomething
DEBUG CompoundObjectTest: ~~~~~~~~~~~~~~~~~~~~~
DEBUG CompoundObjectMethodInterceptor: Message from CompoundObjectMethodInterceptor (CGLIB). Calling method doSomethingWithArguments
DEBUG CompoundObject: ** We are in doSomethingWithArguments
DEBUG CompoundObject: Arguments {arg1: 2, arg2: another test}
DEBUG CompoundObjectTest: result of calling 'doSomething' with arguments: return value from doSomethingWithArguments
DEBUG CompoundObjectTest: ~~~~~~~~~~~~~~~~~~~~~
```

## Where to apply
A scope of application could be seen now a bit vague in our applications. But the proxies are very useful for unit 
testing when you need to emulate work of a service, which connect to different resource and fetch some data. 
No need to have real connect, we can return required values from a properties value in a resource folder in a test environment.

I would introduce one of possible applications of the proxy. It's a secured method invocation. API methods are public, 
but I want to restrict access by a user role. I would say it is possible to use in SOAP, REST services when you have 
public API and possible a user can call a method which should not be called with the user's role.

### Secure a method invocation
I would implement an application which demonstrates one of applications of proxy an object. The application has 
an object with some methods. The methods have public access modifiers. Any code can call the methods. But I would 
restrict invocation of the methods by a user role. Lets say the application has three access levels:

- `DEFAULT` - any user can call the method, even if the user is not logged in the application;
- `EXPERIENCED` - user has to be logged in and can do some extended operation comparing with `DEFAULT`;
- `ROOT` - user has to be logged in and can do everything in the application.

##### SecurityRole.java
```java
package info.biosfood.proxy.security;

public enum SecurityRole {
    DEFAULT(0), EXPERIENCED(200), ROOT(300);
    
    private int level = 0;
    
    SecurityRole(int level) {
        this.level = level;
    }
    
    public int getLevel() {
        return level;
    }

}
```

Then I implement an annotation which will be applied to methods to restrict access for users with different access level.

##### Annotation Security.java
```java
package info.biosfood.proxy.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Security {
    SecurityRole role() default SecurityRole.DEFAULT;
}
```

Then I implement a secured class `SecuredObject`. The class contains three methods with different access level.
- `highlySecuredMethod` - a strongly secured method, only a user with `ROOT` can invoke the method;
- `mediumSecuredMethod` - a medium secured method, a user with `EXPERIENCED` or higher can invoke the method;
- `publicAccessMethod` - anybody can invoke the method.

##### SecuredObject.java
```java
package info.biosfood.proxy.security;

import org.apache.log4j.Logger;

/**
* The class can be created only by SecuredObjectFactory
* */
public class SecuredObject {

    private static final Logger LOG = Logger.getLogger(SecuredObject.class);
    
    SecuredObject() {
    
    }
    
    @Security(role = SecurityRole.ROOT)
    public void highlySecuredMethod() {
        LOG.debug("hello from highlySecuredMethod");
    }
    
    @Security(role = SecurityRole.EXPERIENCED)
    public void mediumSecuredMethod() {
        LOG.debug("hello from mediumSecuredMethod ");
    }
    
    @Security()
    public void publicAccessMethod() {
        LOG.debug("hello from publicAccessMethod");
    }

}
```

All preparation are done, then we need bind the implemented classes together. First, I implement a `Context` class 
which stores a user credentials. The `Context` is supposed to be one for each user, don't judge the code of the class 
too harshly - it's just en example.

##### Context.java
```java
package info.biosfood.proxy.security;

/**
* Absolutely dummy user-specific context, only for holding user's credentials. For each user context is different.
* Supposed to be stored in a session. Purpose - demonstration only.
* */
public class Context {

    private static final Context context = new Context();
    
    public static Context getInstance() {
        return context;
    }
    
    SecurityRole securityRole = SecurityRole.DEFAULT;
    
    String userName = "ANONYMOUS";
    
    private Context() {
    
    }
    
    public SecurityRole getSecurityRole() {
        return securityRole;
    }
    
    public String getUserName() {
        return userName;
    }

}
```

Next step is to implement a method invocation interceptor. The class intercepts a method's invocation and a method `intercept()`
is called instead of calling actual method. Inside the method `intercept()` the application checks if the called method 
has access restrictions and compares required access level with a user's access level. If the user's access level is 
enough then the method is called and result is returned. But if the user's access level is not enough then a `SecurityException` is thrown.

##### SecuredObjectMethodInterceptor.java
```java
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
```

##### SecurityException.java
```java
package info.biosfood.proxy.security;

public class SecurityException extends RuntimeException {

public SecurityException(String message) {
super(message);
}

}
```

Finally, I need to implement a factory which creates an instance of `SecuredObject` class. A user can't create an instance of `SecuredObject` directly, 
because the user can miss assigning interceptors.

##### SecuredObjectFactory.java
```java
package info.biosfood.proxy.security;

import net.sf.cglib.proxy.Enhancer;

public class SecuredObjectFactory {
    
    public static SecuredObject create() {
        return (SecuredObject) Enhancer.create(SecuredObject.class, new SecuredObjectMethodInterceptor());
    }

}
```

I implement a test, which demonstrates three cases of invocation. In each case I invoke all three secured methods with require different access level.

A test method `testCallingSecuredMethodsWithDefaultSecurityLevel()` emulates calling all three secured method with `DEFAULT` access level.

A test method `testCallingSecuredMethodsWithExperiencedSecurityLevel()` emulates calling all three secured method with `EXPERIENCED` access level.

A test method `testCallingSecuredMethodsWithRootSecurityLevel()` emulates calling all three secured method with `ROOT` access level.

##### SecurityObjectTest.java
```java
package info.biosfood.proxy.security;

import org.junit.Before;
import org.junit.Test;

import org.apache.log4j.Logger;

public class SecurityObjectTest {
    
    static final Logger LOG = Logger.getLogger(SecurityObjectTest.class);
    
    SecuredObject subject;
    
    @Before
    public void setup() {
        subject = SecuredObjectFactory.create();
    }
    
    @Test
    public void testCallingSecuredMethodsWithDefaultSecurityLevel() {
        LOG.debug("\n\n-- calling all methods with default access level");
        
        Context.getInstance().userName = "DEFAULT";
        Context.getInstance().securityRole = SecurityRole.DEFAULT;
        
        subject.publicAccessMethod();
        
        try {
            subject.mediumSecuredMethod();
        } catch (SecurityException e) {
            LOG.debug("Caught security exception for low secured method", e);
        }
        
        try {
            subject.highlySecuredMethod();
        } catch (SecurityException e) {
            LOG.debug("Caught security exception for high secured method", e);
        }
    }
    
    @Test
    public void testCallingSecuredMethodsWithExperiencedSecurityLevel() {
        LOG.debug("\n\n-- calling all methods with experienced access level");
        
        Context.getInstance().userName = "EXPERIENCED";
        Context.getInstance().securityRole = SecurityRole.EXPERIENCED;
        
        subject.publicAccessMethod();
        subject.mediumSecuredMethod();
        
        try {
            subject.highlySecuredMethod();
        } catch (SecurityException e) {
            LOG.debug("Caught security exception for high secured method", e);
        }
    }
    
    @Test
    public void testCallingSecuredMethodsWithRootSecurityLevel() {
        LOG.debug("\n\n-- calling all methods with root access level");
        
        Context.getInstance().userName = "ROOT";
        Context.getInstance().securityRole = SecurityRole.ROOT;
        
        subject.publicAccessMethod();
        subject.mediumSecuredMethod();
        subject.highlySecuredMethod();
    }

}
```

### Test run

First test is with `DEFAULT` access level and the user is able to call only one method `publicAccessMethod()` without an exception.
The user is not able to call `mediumSecuredMethod()` and `highlySecuredMethod` methods, a `SecurityException` is thrown as the methods require higher access level then the user has.

Second test is with `EXPERIENCED` access level and the user is able to call only two methods `publicAccessMethod()` and `mediumSecuredMethod()` without an exception. A `SecurityException` is thrown only when the user calls `highlySecuredMethod()` as the method requires a `ROOT` access level.

Third test is with `ROOT` access level and the user is able to call all method, no exception is thrown.

##### test's output
```text
DEBUG SecurityObjectTest:

-- calling all methods with default access level
DEBUG SecuredObject: hello from publicAccessMethod
DEBUG SecurityObjectTest: Caught security exception for low secured method
info.biosfood.proxy.security.SecurityException: User DEFAULT is not allowed to call a method mediumSecuredMethod
at info.biosfood.proxy.security.SecuredObjectMethodInterceptor.intercept(SecuredObjectMethodInterceptor.java:16)
at info.biosfood.proxy.security.SecuredObject$$EnhancerByCGLIB$$611bc305.mediumSecuredMethod(&lt;generated&gt;)
at info.biosfood.proxy.security.SecurityObjectTest.testCallingSecuredMethodsWithDefaultSecurityLevel(SecurityObjectTest.java:29)

DEBUG SecurityObjectTest: Caught security exception for high secured method
info.biosfood.proxy.security.SecurityException: User DEFAULT is not allowed to call a method highlySecuredMethod
at info.biosfood.proxy.security.SecuredObjectMethodInterceptor.intercept(SecuredObjectMethodInterceptor.java:16)
at info.biosfood.proxy.security.SecuredObject$$EnhancerByCGLIB$$611bc305.highlySecuredMethod(&lt;generated&gt;)
at info.biosfood.proxy.security.SecurityObjectTest.testCallingSecuredMethodsWithDefaultSecurityLevel(SecurityObjectTest.java:35)

DEBUG SecurityObjectTest:

-- calling all methods with experienced access level
DEBUG SecuredObject: hello from publicAccessMethod
DEBUG SecuredObject: hello from mediumSecuredMethod
DEBUG SecurityObjectTest: Caught security exception for high secured method
info.biosfood.proxy.security.SecurityException: User EXPERIENCED is not allowed to call a method highlySecuredMethod
at info.biosfood.proxy.security.SecuredObjectMethodInterceptor.intercept(SecuredObjectMethodInterceptor.java:16)
at info.biosfood.proxy.security.SecuredObject$$EnhancerByCGLIB$$611bc305.highlySecuredMethod(&lt;generated&gt;)
at info.biosfood.proxy.security.SecurityObjectTest.testCallingSecuredMethodsWithExperiencedSecurityLevel(SecurityObjectTest.java:52)

DEBUG SecurityObjectTest:

-- calling all methods with root access level
DEBUG SecuredObject: hello from publicAccessMethod
DEBUG SecuredObject: hello from mediumSecuredMethod
DEBUG SecuredObject: hello from highlySecuredMethod
```

- [CGLIB Github project](https://github.com/cglib/cglib) and [Manual](http://mydailyjava.blogspot.de/2013/11/cglib-missing-manual.html)
- [OODesign.com](http://www.oodesign.com/proxy-pattern.html)
