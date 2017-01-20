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
    public void testCallingSecuredMethodsWithDefaultAccessLevel() {
        LOG.debug("\n\n-- calling all methods with default access level");

        Context.getInstance().userName = "DEFAULT";
        Context.getInstance().securityRole = SecurityRole.DEFAULT;

        subject.publicMethod();

        try {
            subject.lowSecurityMethod();
        } catch (SecurityException e) {
            LOG.debug("Caught security exception for low secured method", e);
        }

        try {
            subject.highSecurityMethod();
        } catch (SecurityException e) {
            LOG.debug("Caught security exception for high secured method", e);
        }
    }

    @Test
    public void testCallingSecuredMethodsWithExperiencedSecutiryLevel() {
        LOG.debug("\n\n-- calling all methods with experienced access level");

        Context.getInstance().userName = "EXPERIENCED";
        Context.getInstance().securityRole = SecurityRole.EXPERIENCED;

        subject.publicMethod();
        subject.lowSecurityMethod();

        try {
            subject.highSecurityMethod();
        } catch (SecurityException e) {
            LOG.debug("Caught security exception for high secured method", e);
        }
    }

    @Test
    public void testCallingSecuredMethodsWithHighSecurityLevel() {
        LOG.debug("\n\n-- calling all methods with root access level");

        Context.getInstance().userName = "ROOT";
        Context.getInstance().securityRole = SecurityRole.ROOT;

        subject.publicMethod();
        subject.lowSecurityMethod();
        subject.highSecurityMethod();
    }

}
