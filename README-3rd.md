# Project jpwh-2e-examples: a reader's perspective

### Intro

#### About this doc

This doc tries to describe the project (sample code for the book Java Persistence with Hibernate, 2nd Edition), make the best out of the book, from a reader's perspective.

Out of respect to the original authors, we leave the original `README.md` untouched.

### How to setup and run it

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

Add the JVM options, for example, to mvn test command line:

```
mvn test -Ddatabase=MYSQL -DconnectionURL=jdbc:mysql://jpwh_2e_examples:MyNewPass4!@localhost/jpwh_2e_examples
```

* Test NG @Test groups attribute is a way to group tests, such that we could selectively run tests that belong to specific group(s).
* Here we don't need to define a JVM arg `-Dgroups=MYSQL `, because it will be implied by the database arg. See DatabaseTestMethodSelector as defined as method-selector in AllTests.tng.xml.
* If we define JVM arg `-Dtest=TestClassFoo`, tests belong to POSTGRESQL group will be run as well. Example: CallStoredProcedures#callReturningRefCursorNative().

For IntelliJ IDEA, add it to the TestNG template.

For command line, use it:

```
# maven-surefire-plugin unit tests
mvn test -DsurefireArgLine="-Ddatabase=mysql -DconnectionURL=jdbc:mysql://xxx"

# maven-failsafe-plugin integration tests
mvn verify -DargLine="-Ddatabase=mysql -DconnectionURL=jdbc:mysql://xxx"
```

### Architecture notes

#### It uses BTM JTA and no Spring stuff

Fortunately, btm still works, after so many years.

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

#### hibernate-ehcache is deprecated

Port hibernate-ehcache to the new caching SPI, but deprecate https://hibernate.atlassian.net/browse/HHH-12441

```
Deprecate hibernate-ehcache module as it is using Ehcache 2 as its back-end, which is deprecated itself in favor of Ehcache 3.  Ehcache 3 can be easily used instead by using the hibernate-jcache module and have Ehcache 3 (which is a JCache implementor) properly registered with JCache.
```

### Problems, solutions and changes

#### Intro

Considerable unsolved problems in database and hibernate as an ORM have been left unsolved for years. We could not hope to solve them all. Just keep them here such that we won't be surprised by the same problems more than once.

#### Dependency updates

#### [solved] Hibernate: 2nd level statistics API changes

Known breaking changes:

* getElementCountInMemory() will always return NO_EXTENDED_STAT_SUPPORT_RETURN, because no cache providers implement ExtendedStatisticsSupport.

#### [workaround] JPA Criteria API to call H2 DATE_DIFF() broken on H2 >= version-1.4.199

Test case: Restriction#executeQueriesWithFunctions().

Current status: query via JPA Criteria API is replaced with native query via SQL, at least until this problem is solved.

* But any solution shall involve modification to h2 and/or hibernate.

* Not many people use hibernate with H2 in industry use. This problem and any potential solution (being as important and elegant as it can be) won't draw much attention.

Phenomenon:

```
2023-06-09 16:13:47,477 DEBUG [main] o.hibernate.SQL o.h.e.j.s.SqlStatementLogger.logStatement():144 
    /* select
        generatedAlias0 
    from
        Item as generatedAlias0 
    where
        function('DATEDIFF', :param0, generatedAlias0.createdOn, generatedAlias0.auctionEnd)>1 */ select
            item0_.id as id1_6_,
            item0_.approved as approved2_6_,
            item0_.auctionEnd as auctione3_6_,
            item0_.auctionType as auctiont4_6_,
            item0_.buyNowPrice as buynowpr5_6_,
            item0_.createdOn as createdo6_6_,
            item0_.name as name7_6_,
            item0_.seller_id as seller_i8_6_ 
        from
            Item item0_ 
        where
            datediff(?, item0_.createdOn, item0_.auctionEnd)>1
2023-06-09 16:13:47,477 TRACE [main] o.h.t.d.s.BasicBinder o.h.t.d.s.BasicBinder.bind():64 binding parameter [1] as [VARCHAR] - [DAY]
2023-06-09 16:13:47,477 WARN  [main] o.h.e.j.s.SqlExceptionHelper o.h.e.j.s.SqlExceptionHelper.logExceptions():137 SQL Error: 1582, SQLState: 42000
2023-06-09 16:13:47,477 ERROR [main] o.h.e.j.s.SqlExceptionHelper o.h.e.j.s.SqlExceptionHelper.logExceptions():142 Incorrect parameter count in the call to native function 'datediff'
```

Syntax error as also reported in:

https://stackoverflow.com/questions/60589903/issue-with-h2-datediff-function-using-criteria-api

```
It completes without any issue. Moreover if I switch version of H2 to 1.3.171, Criteria API works fine. I use the following versions of H2 and Hibernate 1.4.200, 5.4.8.Final accordingly. Could somebody help with this?
```

We've bisect the releases, to find out the last good release is version-1.4.198, and oldest bad is version-1.4.199.

Examined the commit history, bisect to find the first commit that breaks it:

```
$ git bisect start
status: waiting for both good and bad commits

$ git bisect good version-1.4.198
status: waiting for bad commit, 1 good commit known

$ git bisect bad version-1.4.199
Bisecting: 68 revisions left to test after this (roughly 6 steps)
[2cc2aac4707198390a21e981baa3ee75931a586e] Merge pull request #1790 from katzyn/trim

...

$ git bisect bad
5a6cec1dbfc56f5750c90cc6ab3556bcca00aed3 is the first bad commit
commit 5a6cec1dbfc56f5750c90cc6ab3556bcca00aed3
Author: Evgenij Ryazanov <katzyn@gmail.com>
Date:   Wed Mar 6 21:02:33 2019 +0800

    Don't try to parse expressions as date part argument of DATE_ADD and DATE_DIFF

 h2/src/docsrc/help/help.csv            |  2 +-
 h2/src/main/org/h2/command/Parser.java | 13 +++++--------
 2 files changed, 6 insertions(+), 9 deletions(-)
 
$ git bisect reset
```

#### [workaround] Unexpected behavior of being lazy loaded or byte enhanced or both

Test case affected: LazyProxyCollections#lazyEntityProxies().

* Also: FetchLoadGraph#loadBidBidderItem().

Current status: these verifications against the returned entity objects are disabled:

* being lazy loaded: persistenceUtil.isLoaded(item) and persistenceUtil.isLoaded(item, "seller").
* being initialized or not: Hibernate.isInitialized(item).
  * Byte enhanced entity objects are treated as uninitialized. See its implementation.

Phenomenon:

* It draw our attention because of test failure.
* In day one during playing with the code, the entity object returned by em.getReference() is byte enhanced (having the member "$$_hibernate_attributeInterceptor"), which surprises me. I treat this discovery as a newly found feature with joy.
* In day two, suddenly, it become a proxy (as expected by Googled web pages and ChatGPT). Absolutely no code changed; just the dev machine rebooted (C940 got garbage contents in GUI and I decided to upgrade the iGPU driver to latest stable in the hope to solve it).
  * What makes us mad is that at the same time, entity objects in FetchLoadGraph#loadBidBidderItem() are byte-enhanced.

More info:

* Status control shall be considered part of implementation details, may change quickly and without notice.
* However, the authors are geeks, willing to explore such details.
* But we as ordinary readers shall keep ourselves in safe zone.

#### [unsolved] IncompatibleClassChangeError: Class org.hibernate.collection.internal.PersistentMap does not implement the requested interface java.util.Collection



#### [unsolved] Random test failures

| Test Class                     | Failure                                                |      |      |      |
| ------------------------------ | ------------------------------------------------------ | ---- | ---- | ---- |
| org.jpwh.test.filtering.Envers | Envers.auditLogging:177 expected [Foo] but found [Bar] |      |      |      |
| org.jpwh.test.filtering.Envers | Envers.auditLogging:186 NullPointer                    |      |      |      |
|                                |                                                        |      |      |      |

