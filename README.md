<h2>Proxying an object</h2>

I discovered two options for creating proxy for an object:
- create a proxy with Java Reflection;
- use CGLIB for creating a proxy object.

<h3>Content</h3>
- creating of simple proxies using two approaches mentioned above;
- create restricted access methods, all method are public, but a user with an appropriate entitlement can call the method, otherwise the application will throw an exception.
