# scheduler

Schedule events (Dungeons and Dragons)

## Overview

Ever try to schedule events?  It sucks.  People don't use calendars, they're disorganized, and it's a whole mess.  This is to help me organize dates for my Dungeons and Dragons group.  Type in a name, select some days, save it to the "database".

Future Ideas

* Slack notifications of available times

* Show all times on the calendar (use color variations to show light/dark)

* allow updates

## Setup

To get an interactive development environment run:

	lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

	(js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

	lein clean

To create a production build run:

	lein do clean, cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL.

## Reddit

You'll have to configure reddit to communicate with it.

* Create a private subreddit to post to.

* Create a `bot_username` to post to that subreddit with (you can't create a
  subreddit until an account is at least 30 days old or has _x_ karma, so you'll
  want to use an existing account to create it).

* Create a reddit application and set it up to be a bot

* Put your application secrets into a `secrets.cljs` file

## License

Copyright © 2017

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
