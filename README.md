# Holon platform JPA Datastore

This is the reference __JPA__ `Datastore` implementation of the [Holon Platform](https://holon-platform.com), using the Java `JPA` API for data access and manipulation.

The JPA Datastore relies on the following conventions regarding __DataTarget__ and __Path__ naming strategy:

* The [DataTarget](https://docs.holon-platform.com/current/reference/holon-core.html#DataTarget) _name_ is interpreted as the JPA _entity_ name.
* The [Path](https://docs.holon-platform.com/current/reference/holon-core.html#Path) _name_ is interpreted as a JPA _entity_ attribute name, supporting nested classes through the conventional _dot_ notation.

As a _relational Datastore_, standard [relational expressions](https://docs.holon-platform.com/current/reference/holon-datastore-jpa.html#Relational-expressions) are supported (alias, joins and sub-queries).

The JPA Datastore supports any standard JPA __ORM__ library, altough is tested and optimized specifically for:

* [Hibernate](http://hibernate.org/orm) version __4.x__ or __5.x__
* [EclipseLink](http://www.eclipse.org/eclipselink) version __2.5 or above__

A complete __Spring__ and __Spring Boot__ support is provided for JPA Datastore integration in a Spring environment and for __auto-configuration__ facilities.

## Code structure

See [Holon Platform code structure and conventions](https://github.com/holon-platform/platform/blob/master/CODING.md) to learn about the _"real Java API"_ philosophy with which the project codebase is developed and organized.

## Getting started

### System requirements

The Holon Platform is built using __Java 8__, so you need a JRE/JDK version 8 or above to use the platform artifacts.

The __JPA API version 2.x__ or above support by the __ORM__ in use is reccomended to use all the functionalities of the JPA Datastore.

### Releases

See [releases](https://github.com/holon-platform/holon-datastore-jpa/releases) for the available releases. Each release tag provides a link to the closed issues.

### Obtain the artifacts

The [Holon Platform](https://holon-platform.com) is open source and licensed under the [Apache 2.0 license](LICENSE.md). All the artifacts (including binaries, sources and javadocs) are available from the [Maven Central](https://mvnrepository.com/repos/central) repository.

The Maven __group id__ for this module is `com.holon-platform.jpa` and a _BOM (Bill of Materials)_ is provided to obtain the module artifacts:

_Maven BOM:_
```xml
<dependencyManagement>
    <dependency>
        <groupId>com.holon-platform.jpa</groupId>
        <artifactId>holon-datastore-jpa-bom</artifactId>
        <version>5.0.3</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
</dependencyManagement>
```

See the [Artifacts list](#artifacts-list) for a list of the available artifacts of this module.

### Using the Platform BOM

The [Holon Platform](https://holon-platform.com) provides an overall Maven _BOM (Bill of Materials)_ to easily obtain all the available platform artifacts:

_Platform Maven BOM:_
```xml
<dependencyManagement>
    <dependency>
        <groupId>com.holon-platform</groupId>
        <artifactId>bom</artifactId>
        <version>${platform-version}</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
</dependencyManagement>
```

See the [Artifacts list](#artifacts-list) for a list of the available artifacts of this module.

### Build from sources

You can build the sources using Maven (version 3.3.x or above is recommended) like this: 

`mvn clean install`

## Getting help

* Check the [platform documentation](https://docs.holon-platform.com/current/reference) or the specific [module documentation](https://docs.holon-platform.com/current/reference/holon-datastore-jpa.html).

* Ask a question on [Stack Overflow](http://stackoverflow.com). We monitor the [`holon-platform`](http://stackoverflow.com/tags/holon-platform) tag.

* Report an [issue](https://github.com/holon-platform/holon-datastore-jpa/issues).

* A [commercial support](https://holon-platform.com/services) is available too.

## Examples

See the [Holon Platform examples](https://github.com/holon-platform/holon-examples) repository for a set of example projects.

## Contribute

See [Contributing to the Holon Platform](https://github.com/holon-platform/platform/blob/master/CONTRIBUTING.md).

[![Gitter chat](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/holon-platform/contribute?utm_source=share-link&utm_medium=link&utm_campaign=share-link) 
Join the __contribute__ Gitter room for any question and to contact us.

## License

All the [Holon Platform](https://holon-platform.com) modules are _Open Source_ software released under the [Apache 2.0 license](LICENSE).

## Artifacts list

Maven _group id_: `com.holon-platform.jpa`

Artifact id | Description
----------- | -----------
`holon-datastore-jpa` | __JPA__ `Datastore` implementation
`holon-datastore-jpa-spring` | __Spring__ integration using the `@EnableJpa` and  `@EnableJpaDatastore` annotations
`holon-datastore-jpa-spring-boot` | __Spring Boot__ integration for JPA stack and Datastore auto-configuration
`holon-starter-jpa-hibernate` | __Spring Boot__ _starter_ for JPA stack and Datastore auto-configuration using [Hibernate](http://hibernate.org/orm) ORM
`holon-starter-jpa-eclipselink` | __Spring Boot__ _starter_ for JPA stack and Datastore auto-configuration using [EclipseLink](http://www.eclipse.org/eclipselink) ORM
`holon-datastore-jpa-bom` | Bill Of Materials
`documentation-datastore-jpa` | Documentation
