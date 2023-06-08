# Project jpwh-2e-examples: a reader's perspective

### Intro

#### About this doc

This doc tries to describe the project (sample code for the book Java Persistence with Hibernate, 2nd Edition), make the best out of the book, from a reader's perspective.

Out of respect to the original authors, we leave the original `README.md` untouched.

### Overview, arch design

#### Environment, core dependencies

Here we shall limit ourselves to the following basic dependencies. The base line is Java 8. There are very old but fundamental components that we shall not upgrade it very aggressively (e.g., btx).

| Component           | Version      | Notes                              |
| ------------------- | ------------ | ---------------------------------- |
| Java & Java EE      | 8            | Sensible for really old projects   |
| Hibernate ORM       | 5.6.15.Final | Latest for Java 8; targets JPA 2.2 |
| Hibernate Validator | 6.2.5.Final  | Latest for Java EE 8               |
| TestNG              | 7.5.0        | 7.5 series are latest for Java 8   |
| Logback             | 1.3.7        | 1.3 series are latest for Java 8   |

References:

* Require Java 11 for Hibernate ORM 6.0 #4286 https://github.com/hibernate/hibernate-orm/discussions/4286

#### Running tests with MySQL

Add the JVM options:

```
-Ddatabase=mysql -DconnectionURL=jdbc:mysql://jpwh_2e_examples:MyNewPass4!@localhost/jpwh_2e_examples
```

#### This project uses JTA and not Spring

Fortunately, it still works.

Why JTA is not very hot these days?

Answer by ChatGPT:

1. Simplicity and ease of use: Many applications can manage their transactional needs using simpler approaches like local transactions or lightweight frameworks like Spring Transaction Management. JTA, on the other hand, is more suitable for distributed transactions and complex transactional scenarios, which are not as common in all types of applications.
2. Alternative transaction management frameworks: In addition to JTA, there are other transaction management frameworks available in the Java ecosystem, such as Spring Transaction Management, which provide higher-level abstractions and ease of use. These frameworks often abstract away the complexities of JTA and provide a more developer-friendly approach to managing transactions.
3. Container-managed transactions: Many modern Java applications are deployed in container environments like application servers or cloud platforms, where transaction management is often handled by the container itself. In these scenarios, developers may not need to directly interact with JTA and can rely on the container's built-in transaction management capabilities.
4. Industry trends and technologies: The industry has seen the rise of alternative approaches to distributed transactions, such as event-driven architectures, microservices, and NoSQL databases, which may have different transactional requirements. These trends have shifted the focus away from traditional JTA-based transaction management.
5. Learning curve and complexity: JTA and distributed transactions, in general, have a steeper learning curve and can be more complex to implement and manage compared to local transactions. This complexity can make it less appealing to developers unless it is specifically required for their application's needs.

Answer by Bard:

- **The rise of microservices and event-driven architectures.** JTA is designed for monolithic applications, but microservices and  event-driven architectures are becoming increasingly popular. These  architectures are more loosely coupled and distributed, which makes it difficult to use JTA.
- **The emergence of other transaction management solutions.** There are a number of other transaction management solutions available, such as Spring Transaction and Atomikos. These solutions are often easier to use and more flexible than JTA.
- **The lack of support from major Java vendors.**  Oracle, the primary sponsor of JTA, has not released a new version of  JTA since 2013. This lack of support has made it difficult for developers to adopt JTA.
- **Complexity.** JTA is a complex API, which can make it difficult to use.
- **Performance overhead.** JTA can add some performance overhead to applications.
- **Not always necessary.** JTA is not always necessary for applications. If an application only uses a single resource, then JTA is not needed.

### Non-trivial changes from the original code

#### Dependency updates