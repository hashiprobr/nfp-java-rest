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

In the example below, we define an endpoint of integers with `"/sum"` as the
URI.

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

To build a server, you need the name of the package where the endpoints are
located and a port. If the port is omitted, it defaults to 8080.

``` java
import br.pro.hashi.nfp.rest.server.RESTServer;
import br.pro.hashi.nfp.rest.server.RESTServerFactory;

public static void main(String[] args) {
    RESTServerFactory factory = RESTServer.factory();
    RESTServer server = factory.build("name.of.a.package", 8080);
    server.start();
}
```

The server automatically finds and instantiates all endpoints in the package,
including subpackages. If everything works, `server.getUrl()` can be used to
obtain the local address that can be used to access the server. In normal
circumstances, `localhost` should also work.

You should be able to access the URL `http://<address>/sum` using your internet
browser, but the output will not be very exciting...

```
GET received
```

...because no HTTP method has been implemented for this endpoint.

Before continuing, please note that **any change to the endpoints requires a
server restart**.

### Implementing HTTP methods

In order to implement GET, we only need to override the `get` method.

``` java
public class SumEndpoint extends Endpoint<Integer> {
    public SumEndpoint() {
        super("/sum");
    }

    @Override
    public Object get() {
        return 0;
    }
}
```

We can also override the `post`, `put`, and `delete` methods...

``` java
@Override
public Object post(Integer i) {
    return null;
}

@Override
public Object put(Integer i) {
    return null;
}

@Override
public Object delete() {
    return null;
}
```

...with one caveat: `post` and `put` receive an integer parameter, since these
HTTP methods receive a body. Why an integer? Because `SumEndpoint` is an
endpoint of integers, as established by the choice of `Integer` as its
parameter.

On the other hand, all methods can return whatever you want. If the return value
is a string, the client simply receives this string. If the return value is
anything else, the client receives it serialized as a JSON representation.

Analogously, if the client sends a POST or PUT, it needs to send a string as the
body. For an endpoint of strings, it can be an arbitrary one. For an endpoint of
another type, it must be a JSON representation of the type.

### Receiving a key through the URI

The `get` and `delete` methods accept an optional key as part of the URI. For
example, instead of `GET /sum`, the client can `GET /sum/123`. In this case, the
behavior is implemented by a second version of `get`, which accepts the key as
its first parameter.

``` java
@Override
public Object get(String key) {
    return key;
}

@Override
public Object delete(String key) {
    return key;
}
```

### Receiving query strings

In all four methods, the query strings are received as an optional parameter of
type `Args`. The class `Args` is simply an implementation of `Map<String,
String>` with the convenience methods below.

* `boolean getBoolean(String key)`: returns the value of `key` converted to
  boolean.

* `byte getByte(String key)`: returns the value of `key` converted to byte.

* `short getShort(String key)`: returns the value of `key` converted to short.

* `int getInt(String key)`: returns the value of `key` converted to integer.

* `long getLong(String key)`: returns the value of `key` converted to long.

* `float getFloat(String key)`: returns the value of `key` converted to float.

* `double getDouble(String key)`: returns the value of `key` converted to
  double.

* `BigInteger getBigInteger(String key)`: returns the value of `key` converted
  to big integer.

* `BigDecimal getBigDecimal(String key)`: returns the value of `key` converted
  to big decimal.

* `List<String> getList(String key, String regex)`: returns the value of `key`
  as a list of strings, using `regex` as the separator.

* `List<Boolean> getListBoolean(String key)`: returns the value of `key` as a
  list of booleans, using `regex` as the separator.

* `List<Byte> getListByte(String key, String regex)`: returns the value of `key`
  as a list of bytes, using `regex` as the separator.

* `List<Short> getListShort(String key, String regex)`: returns the value of
  `key` as a list of shorts, using `regex` as the separator.

* `List<Integer> getListInt(String key, String regex)`: returns the value of
  `key` as a list of integers, using `regex` as the separator.

* `List<Long> getListLong(String key, String regex)`: returns the value of
  `key` as a list of longs, using `regex` as the separator.

* `List<Float> getListFloat(String key, String regex)`: returns the value of
  `key` as a list of floats, using `regex` as the separator.

* `List<Double> getListDouble(String key, String regex)`: returns the value of
  `key` as a list of doubles, using `regex` as the separator.

* `List<BigInteger> getListBigInteger(String key, String regex)`: returns the
  value of `key` as a list of big integers, using `regex` as the separator.

* `List<BigDecimal> getListBigDecimal(String key, String regex)`: returns the
  value of `key` as a list of big decimals, using `regex` as the separator.

If, for example, we want `GET /sum` to actually do a sum, we can write...

``` java
@Override
public Integer get(Args args) {
    int a = args.getInt("a");
    int b = args.getInt("b");
    return a + b;
}
```

...and point the internet browser to `http://<address>/sum?a=1&b=2` for a
slightly more exciting output.

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

...and point the internet browser to `http://<address>/sum?n=1,2,3,4` for
another output.

``` plaintext
10
```

Again, remember that **any change to the endpoints requires a server restart**.


Multipart requests
------------------

Multipart requests to `post` or `put` **must** have at least one part named
"body" that represents the main body (what you would send as the body in a
non-multipart request). All the others will be received as a parameter of type
`Files`. The class `Files` is simply an implementation of `Map<String,
InputStream>`, where the keys are the part names and the values are the part
data.

* `public Object post(Integer i, InputStream stream)`

* `public Object put(Integer i), InputStream stream)`


Remote server access
--------------------

The `start` method has an argument that defaults to `false` if omitted. If it is
`true`, however...

``` java
server.start(true);
```

...the server opens a public tunnel using [ngrok](https://ngrok.com/), allowing
users in other networks to access it.
