package info.biosfood.proxy.basic;

import net.sf.cglib.proxy.Enhancer;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.lang.reflect.Proxy;

public class CompoundObjectTest {

    static final Logger LOG = Logger.getLogger(CompoundObjectTest.class);

    @Test
    public void runTestJavaReflectionProxy() {
        LOG.debug("\n\nTesting reflection proxy");

        ICompoundObject o = (ICompoundObject) Proxy.newProxyInstance(
                CompoundObject.class.getClassLoader(),
                new Class[]{ICompoundObject.class},
                new CompoundObjectInvocationHandler(new CompoundObject())
        );

        LOG.debug("~~~~~~~~~~~~~~~~~~~~~");
        LOG.debug("do something: " + o.doSomething());
        LOG.debug("~~~~~~~~~~~~~~~~~~~~~");
        LOG.debug("do something with arguments: " + o.doSomethingWithArguments(1, "test"));
        LOG.debug("~~~~~~~~~~~~~~~~~~~~~");
    }

    @Test
    public void runTestCglib() {
        LOG.debug("\n\nTesting CGLIB proxy");

        CompoundObject o = (CompoundObject) Enhancer.create(
                CompoundObject.class,
                new CompoundObjectMethodInterceptor()
        );

        LOG.debug("~~~~~~~~~~~~~~~~~~~~~");
        LOG.debug("do something: " + o.doSomething());
        LOG.debug("~~~~~~~~~~~~~~~~~~~~~");
        LOG.debug("do something with arguments: " + o.doSomethingWithArguments(2, "another test"));
        LOG.debug("~~~~~~~~~~~~~~~~~~~~~");
    }
}
