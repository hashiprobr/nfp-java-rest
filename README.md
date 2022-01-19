nfp-java-rest
=============

**[Not-For-Production](https://github.com/hashiprobr/nfp) REST framework based
on Jetty and Gson.**

If your project is a class assignment or a simple prototype, use this framework
to build local or remote REST endpoints with minimum boilerplate. All you need
is a couple of lines of code.


Quick start
-----------

[Add the framework to your
project.](https://mvnrepository.com/artifact/io.github.hashiprobr/nfp-rest)

### Creating an endpoint

An endpoint is a subclass of `Endpoint`, with a concrete class as its parameter.
This parameter is required because the framework establishes that an endpoint is
always "of something". It can be, for example, an endpoint of integers, an
endpoint of strings, or an endpoint of one of your own classes.

The super constructor must receive the URI from where you want the endpoint to
be.

In the example below, we define an endpoint of integers with "/sum" as the URI.

``` java
package name.of.a.package;

import br.pro.hashi.nfp.rest.server.Endpoint;

public class SumEndpoint extends Endpoint<Integer> {
    public SumEndpoint() {
        super("/sum");
    }
}
```

### Starting a server

The first parameter of the `RestServer` constructor is the name of the package
where the endpoints are located. The second parameter is the port where the
server should listen. If omitted, it defaults to 8080.

``` java
public static void main(String[] args) {
    RestServer server = new RestServer("name.of.a.package", 8080);
    server.start();
}
```

The server automatically finds and instantiates all endpoints in the package,
including subpackages. If everything works, you should see a message in the
console with the local address that can be used to access the server. In normal
circumstances, `localhost` should also work.

``` plaintext
REST server started on http://192.168.123.456:8080
```

You should be able to access the URL `http://192.168.123.456:8080/sum` using
your internet browser, but the output will not be very exciting...

```
GET not implemented
```

...because no HTTP method has been implemented for this endpoint.

Before continuing, please note that **any change to the endpoints require a
server restart**.

### Implementing HTTP methods

In order to implement GET, we only need to override the `get` method.

``` java
public class SumEndpoint extends Endpoint<Integer> {
    public SumEndpoint() {
        super("/sum");
    }

    @Override
    public Integer get(Args args) {
        return 0;
    }
}
```

Why this method returns an integer? Because `SumEndpoint` is an endpoint of
integers, as established by the choice of `Integer` as its parameter. If a
client wants to "GET /sum", it is assumed that it wants to get an integer.

For the same reason, the `post` and `put` methods receive an integer...

``` java
@Override
public Object post(Args args, Integer i) {
    return null;
}

@Override
public Object put(Args args, Integer i) {
    return null;
}
```

...but they can return anything, as can the `delete` method.

``` java
@Override
public Object delete(Args args) {
    return null;
}
```

But what does the client actually receive? If the return value is a string, the
client simply receives this string. If the return value is anything else, the
client receives it serialized as a JSON representation.

Analogously, if the client sends a POST or PUT, it either needs to send a string
as the body. For an endpoint of strings, it can be an arbitrary one. For an
endpoint of another type, it must be a JSON representation of the type.

### Receiving query strings

The query strings are received as an instance of `Args`. The class `Args` is
simply a subclass of `HashMap<String, String>` with the convenience methods
below.

* `boolean getBoolean(String key)`: returns the value of `key` converted to
  boolean.

* `int getInt(String key)`: returns the value of `key` converted to integer.

* `double getDouble(String key)`: returns the value of `key` converted to
  double.

* `List<String> getList(String key, String regex)`: returns the value of `key`
  as a list of strings, using `regex` as the separator.

* `List<Boolean> getBoolean(String key)`: returns the value of `key` as a list
  of booleans, using `regex` as the separator.

* `List<Integer> getListInt(String key, String regex)`: returns the value of
  `key` as a list of integers, using `regex` as the separator.

* `List<Double> getListDouble(String key, String regex)`: returns the value of
  `key` as a list of double, using `regex` as the separator.

If, for example, we want `GET /sum` to actually do a sum, we can write...

``` java
@Override
public Integer get(Args args) {
    int a = args.getInt("a");
    int b = args.getInt("b");
    return a + b;
}
```

...and point the internet browser to `http://192.168.123.456:8080/sum?a=1&b+2`
for a slightly more exciting output.

``` plaintext
3
```

Or, if we want `GET /sum` to do a sum of an arbitrary number of integers, we can
write...

``` java
@Override
public Integer get(Args args) {
    int s = 0;
    for (int i : args.getListInt("n", ",")) {
        s += 1;
    }
    return s;
}
```

...and point the internet browser to `http://192.168.123.456:8080/sum?n=1,2,3,4`
for another output.

``` plaintext
10
```

Again, remember that **any change to the endpoints require a server restart**.


Batch URI suffixes
------------------

If the client sends a request to `/sum/list`, the class `SumEndpoint` is still
responsible for receiving this request. However, it is handled by a list version
of the method.

* `public List<Integer> getList(Args args)`

* `public Object postList(Args args, List<Integer> integers)`

* `public Object putList(Args args, List<Integer> integers)`

* `public Object deleteList(Args args)`


File URI suffixes
-----------------

If the client sends a request to `/sum/file`, the class `SumEndpoint` is still
responsible for receiving this request. However, it is handled by a file version
of the method.

* `public String getFile(Args args)`

* `public String postFile(Args args, InputStream stream)`

* `public String putFile(Args args, InputStream stream)`

* `public Object deleteFile(Args args)`

These methods are supposed to be used to store files and return URLs that can be
used to access the files as static resources. For example, as the `src` of an
`img`.


Remote server access
--------------------

The `start` method has an argument that defaults to `false` if omitted. If it is
`true`, however...

``` java
server.start(true);
```

...the server opens a public tunnel using [ngrok](https://ngrok.com/), allowing
uses in other networks to access it.


Full CRUD example
-----------------

Read the [nfp-java-dao](https://github.com/hashiprobr/nfp-java-dao)
documentation to understand the `User` and `UserDAO` classes.

``` java
public class UserEndpoint extends Endpoint<User> {
    private UserDAO dao;

    public UserEndpoint() {
        super("/user");
        this.dao = new UserDAO();
    }

    @Override
    public User get(Args args) {
        String key = args.get("key");
        return dao.retrieve(key);
    }

    @Override
    public List<User> getList(Args args) {
        List<String> keys = args.getListInt("keys");
        UserDAO.Selection selection = dao.select(keys);
        return dao.retrieve(selection);
    }

    @Override
    public String getFile(Args args) {
        String key = args.get("key");
        String name = args.get("name");
        return dao.retrieve(key, name);
    }

    @Override
    public Object post(Args args, User user) {
        return dao.create(user);
    }

    @Override
    public Object postList(Args args, List<User> users) {
        return dao.create(users);
    }

    @Override
    public String postFile(Args args, InputStream stream) {
        String key = args.get("key");
        String name = args.get("name");
        return dao.create(key, name, stream);
    }

    @Override
    public Object put(Args args, User user) {
        dao.update(user);
        return null;
    }

    @Override
    public Object putList(Args args, List<User> users) {
        dao.update(users);
        return null;
    }

    @Override
    public String putFile(Args args, InputStream stream) {
        String key = args.get("key");
        String name = args.get("name");
        return dao.update(key, name, stream);
    }

    @Override
    public Object delete(Args args) {
        String key = args.get("key");
        dao.delete(key);
        return null;
    }

    @Override
    public Object deleteList(Args args) {
        List<String> keys = args.getListInt("keys");
        UserDAO.Selection selection = dao.select(keys);
        return null;
    }

    @Override
    public Object deleteFile(Args args) {
        String key = args.get("key");
        String name = args.get("name");
        dao.delete(key, name);
        return null;
    }
}

```
