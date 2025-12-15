# Advent of Code Solutions

Here I will be sharing my solutions to the [Advent of Code](https://adventofcode.com) online puzzle.

The solutions might be implemented in different languages, out of curiosity, for practice, comparison or fun.

I tend to mostly focus on Functional Programming style.\
My goal isn't to solve each puzzle as fast as possible. I want to create a beautiful solutions that satisfies me.
 The speed and performance of how fast a solution runs is also not my priority. I try to keep general performance in mind but I don't want to optimize the code for every bit of performance.

## How to run the Clerk notebooks

### With Clojure CLI
```sh
clj -M:nextjournal/clerk nextjournal.clerk/serve! --watch-paths src --port 7777
```

### From the REPL
Take a look at the `dev/user.clj`for running the server, evaluating individual notebooks, etc.

Evaluate expressions from within your editor if you can, or start a REPL and run clerk commands
```sh

```

## How to build the Clerk notebooks
run
```sh
clj -X:nextjournal/clerk
```

Then run the output with an http server like `http-serve`
```sh
http-serve public/build
```


## How to run each solution separately

### With Clojure CLI

The `runner.clj` file helps run each day.
```sh
clj -M:run <year> <day> <& args>
```
