Dottydoc Client
===============
This repository is home to the default implementation of the dottydoc client.
Dottydoc supports different front ends, and this is an example of how to create
one for your project. It is also the default one that sbt uses when generating
the documentation for your project.

How To
======
Dottydoc works by supplying a mustache [template](./resources/template.html)
and a list of [resources](./resources). These are then used to create a static
website. As such, this project is published as a static blob without any
dependencies and without any classfiles or runnables.

The project has the following structure:

```
.
├── js
├── resources
│   ├── index.css
│   └── template.html
├── scripts
│   └── clonelib.sh
└── test
    ├── GenerateCollections.scala
    └── GenerateOption.scala
```

All scala files in [js](./js) are used to create `dottydoc.js` which is
published as a resource.

## Generating a site ##
This project currently relies on
[#1453](https://github.com/lampepfl/dotty/pull/1453) getting merged - as such,
to build the project you need to do `publishLocal` for my
[topic/dottydoc](https://github.com/felixmulder/dotty/tree/topic/dottydoc)
branch.

To facilitate development, this project relies on dotty - but will purge these
dependencies on publish - and is, as such, able to generate a static website
using the dottydoc interface without creating a cyclic dependency.

To generate the whitelisted standard collections:

```
$ sbt generateCollections
```

This will clone a specific fork of the [scala](http://github.com/scala/scala)
repository and generate the collections. As this takes quite some time, there
is also functionality for generating a single page like `scala.Array`:

```
$ sbt generateArray
```

Or if you want to choose:

```
$ sbt
> generateArgs ./scala-scala/src/library/scala/Option.scala
```

Calling either of these generators will clone the standard library, for now
this is a good fit since it contains a lot of useful code that we want to be
able to render documentation for correctly - but in the long run this will
probably go away.
