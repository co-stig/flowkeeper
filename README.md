# Flowkeeper v2

⚠️ **The work on the application is in progress, I haven't produced any working version of it yet** 

If you need something that works right now, then you are welcome to check out Flowkeeper v1 -- a 10-years-old design, 
which still works great: http://flowkeeper.org/

Flowkeeper is an open-source cross-platform Pomodoro timer with the following features:

1. Configurable timer (obviously);
2. Task lists AKA *Backlogs*;
3. Launch at startup;
4. Notification area / System tray integration;
5. Configurable sounds;
6. Simple plaintext database, easy to store and backup;
7. (Optional) Support of XMPP / Jabber servers for synchronizing data and sharing status information;
8. (Optional) Support for multiple simultaneous clients via XMPP;
9. (Optional) Automatic updates;

This is a complete rewrite of the original Flowkeeper timer, which was implemented in Java, and which I've never had time to maintain (nor even
release its sources). This project is properly Open Source from day 1, and I hope that it will help its evolution.

The aim is to implement a simple yet powerful, lightweight and portable application with elegant internal design and user-friendly interface. 
Flowkeeper is released under a free software license (GPLv3).

# Installation

## Windows

Windows installer and portable ZIP version are available on Releases page.

# History

**2020-04-19**: This GitHub repository is created, starting the work on Flowkeeper v2;

**2014-??-??**: [Linux Magazine](https://www.linux-magazine.com/Issues/2014/158/Bitparade-Pomodoro-Tools) publishes an article, comparing Flowkeeper with other 
Pomodoro timers of the era. The review is mostly positive, but mentions the lack of support from the developer. More shame.

**2013-02-14**: Thanks to Luken Shiro, Flowkeeper becomes part of [SlackBuilds](https://slackbuilds.org/repository/14.0/office/flowkeeper/). Amazing, but it is still 
available in Slackware's most recent (he-he) version 14.2!

**2010-12-24**: [Flowkeeper](http://flowkeeper.org/) is released. The first version is a desktop-only Java application, which only works in offline mode. The
project gains some following, but unfortunately there were too many things going on in my life at that moment, and I de-facto abandoned the project
right after release. Shame on me.

# Design

Flowkeeper is a Qt 5 application. Its data model is built around the concept of *Strategy*, which can be replayed to reconstruct the state of the 
application at a given moment. Here's a sample list of strategies:

```python
# Comments and empty lines are supported
# Each strategy is strictly one line of text
CreateBacklog("2020-04-19")  # All arguments are in quotes, regardless of their type

# CreateTask(TITLE, POMODORI)
CreateTask("Setup GitHub repository", "2")
CreateTask("Write the first \"README.md\"", "2")  # The inner " and \ should be escaped

# This will start today's backlog
StartPomodoro("Setup GitHub repository")  # Titles as identifiers

# ...25 minutes later
# It also starts 5-minute rest interval automatically
CompletePomodoro()

# ...5 minutes later
CompleteRest()
```

There are few reasons for applying such approach: 

1. (the main one) It allows to use XMPP middleware to store and share the database as the history of messages (each strategy is a message);
2. Application database is essentially a log, which contains all details with very little effort;
3. No need to care about changing schema, etc.
4. The database can be kept as a simple human-readable plaintext file, to which the application simply appends data line by line;
5. Synchronization between devices becomes almost trivial;

Thanks to Qt framework the application looks great and supports all modern operating systems.

# Build

Install Qt Creator, open `flowkeeper.pro` and press CTRL+R.

# Contribute

Pull requests are welcome.

# License

GPLv3.