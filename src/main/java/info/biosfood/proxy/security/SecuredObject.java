public class SecuredObject {

    private static final Logger LOG = Logger.getLogger(SecuredObject.class);

    SecuredObject() {

    }

    @Security(role = SecurityRole.ROOT)
    public void highlySecurityMethod() {
        LOG.debug("hello from highlySecurityMethod");
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
