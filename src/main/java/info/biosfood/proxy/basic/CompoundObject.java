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
