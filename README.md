# Sirusi
create indenticons similar to [Github](https://github.com/blog/1586-identicons) and [Gravatar (retro)](https://en.gravatar.com/site/implement/images/) avatars.

![5x5](https://raw.githubusercontent.com/taichi/sirusi/master/src/docs/5x5.png)
![6x6](https://raw.githubusercontent.com/taichi/sirusi/master/src/docs/6x6.png)
![7x7](https://raw.githubusercontent.com/taichi/sirusi/master/src/docs/7x7.png)

## Getting Started

execute commands below

```
./gradlew installApp
./build/install/sirusi/bin/sirusi
```

and access to `http://localhost:8080/0000.png?s=96` you will see below

![0000](https://raw.githubusercontent.com/taichi/sirusi/master/src/docs/0000.png)

Sirusi supported url template is `/{seed}.{type}?s={size}`

* seed
    * any strings
* type
    * supported content type is jpg, bmp, gif, png, wbmp, jpeg.
    * default type is png
* size
    * default size is 48. this means 48x48 pixel png will be generate.

## Install to Ubuntu

```
./gradlew buildDeb
sudo gdebi build/distributions/sirusi_0.1.0_all.deb
```

## Uninstall from Ubuntu

```
sudo apt-get remove sirusi
```

## Similar projects

* [donpark/identicon](https://github.com/donpark/identicon)
* [davidhampgonsalves/Contact-Identicons](https://github.com/davidhampgonsalves/Contact-Identicons)
* [sehrgut/node-retricon](https://github.com/sehrgut/node-retricon)


# License

Apache License, Version 2.0

