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
    public void highSecurityMethod() {
        LOG.debug("hello from highSecurityMethod");
    }

    @Security(role = SecurityRole.EXPERIENCED)
    public void lowSecurityMethod() {
        LOG.debug("hello from lowSecurityMethod ");
    }

    @Security()
    public void publicMethod() {
        LOG.debug("hello from publicMethod");
    }

}
