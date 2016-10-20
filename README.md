frontend-bootstrap
==================

[![Build Status](https://travis-ci.org/hmrc/frontend-bootstrap.svg)](https://travis-ci.org/hmrc/frontend-bootstrap) [ ![Download](https://api.bintray.com/packages/hmrc/releases/frontend-bootstrap/images/download.svg) ](https://bintray.com/hmrc/releases/frontend-bootstrap/_latestVersion)

This library implements a basic Play Global object and related functionality for frontend applications.

### Creating a Global object for your frontend application

Simply create an object extending `DefaultFrontendGlobal`. That will provide you with the common filters and error handling.
You can also override `frontendFilters` attribute if you need to alter the default set of filters.

Note: the play SecurityHeadersFilter is not provided by default. To enable it add 'security.headers.filter.enabled=true' in your application's configuration file (`application.conf`)

### Default Play configuration

This library provides a default configuration for your Play frontend applications. Use it in your application's configuration file (`application.conf`):

```scala
include "common.conf"
```

### Metrics plugin

To enable the Metrics plugin in your application, add this line to your `play.plugins` file:

```scala
1:com.kenshoo.play.metrics.MetricsPlugin
```

You can also enable the plugin's admin servlet by adding this line to your `routes` file:

```scala
GET     /admin/metrics          com.kenshoo.play.metrics.MetricsController.metrics
```

#### Publishing metrics to Graphite

By default Graphite publishing is disabled. To enable if for your application, add a block like this to your application `conf` file:

```scala
microservice {

  metrics {
   graphite {
     host = graphite
     port = 2003
     prefix = play.tax.
     enabled = true
   }
  }

}
```

and point your Global object (inherited from `DefaultFrontendGlobal` ) to it

```scala
override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig("microservice.metrics")
```

### Installing

Add the following to your SBT build:
```scala
resolvers += Resolver.bintrayRepo("hmrc", "releases")

libraryDependencies += "uk.gov.hmrc" % "frontend-bootstrap" % "[INSERT-VERSION]"
```

## License ##

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

