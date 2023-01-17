![](https://cloud.githubusercontent.com/assets/2366334/4677025/64ae592a-55e2-11e4-8a31-31c2941ff995.png)

[![Jenkins build status](https://libgdx.badlogicgames.com/jenkins/buildStatus/icon?job=gdx-ai&.png)](https://libgdx.badlogicgames.com/jenkins/job/gdx-ai/) (Jenkins)

An artificial intelligence framework, entirely written in Java, for game development with [libGDX](https://github.com/libgdx/libgdx).

The gdxAI project is a libGDX extension living under the [libGDX umbrella](https://github.com/libgdx). However it does not force you to use that specific framework if you do not wish to do so. The libGDX jar remains an essential requirement, mostly due to the use of libGDX collections which are optimized for mobile platforms by limiting garbage creation and supporting primitive types directly, so avoiding boxing and unboxing.

GdxAI tries to be a high-performance framework providing some of the most common AI techniques used by game industry.
However, in the present state of the art, the gdxAI framework covers only part of the entire game AI area, which is really huge. We've tried to focus on what matters most in game AI development, though. And more stuff will come soon.

Currently supported features are:
- Movement AI
  * Steering Behaviors
  * Formation Motion
- Pathfinding
  * A*
  * Hierarchical Pathfinding
  * Path Smoothing
  * Interruptible Pathfinding
- Decision Making
  * State Machine
  * Behavior Trees
- Infrastructure
  * Message Handling
  * Scheduling


### Getting Started

* [Use gdxAI in your project](https://github.com/libgdx/gdx-ai/wiki/Getting-started-with-gdxAI)
* [Read the wiki](https://github.com/libgdx/gdx-ai/wiki)
* [Refer to the javadocs](https://javadoc.io/doc/com.badlogicgames.gdx/gdx-ai/latest/index.html)
* [Read the examples](https://github.com/libgdx/gdx-ai/tree/master/tests)
* [Useful Links and Resources](https://github.com/libgdx/gdx-ai/wiki/Useful-Links-and-Resources)


### News & Community

Check the [libGDX blog](https://libgdx.com/news/) for news and updates. You can get help on our [Discord server](https://libgdx.com/community/discord/).

### Reporting Issues

Something not working quite as expected? Do you need a feature that has not been implemented yet? Check the [issue tracker](https://github.com/libgdx/gdx-ai/issues) and add a new one if your problem is not already listed. Please try to provide a detailed description of your problem, including the steps to reproduce it.

### Contributing

Awesome! If you would like to contribute with a new feature or a bugfix, [fork this repo](https://help.github.com/articles/fork-a-repo) and [submit a pull request](https://help.github.com/articles/using-pull-requests).
Also, before we can accept substantial code contributions, we need you to sign the [libGDX Contributor License Agreement](https://libgdx.com/dev/contributing/#contributor-license-agreement).

### License

The gdxAI project is licensed under the [Apache 2 License](https://github.com/libgdx/gdx-ai/blob/master/LICENSE), meaning you
can use it free of charge, without strings attached in commercial and non-commercial projects. We love to
get (non-mandatory) credit in case you release a game or app using gdxAI!

