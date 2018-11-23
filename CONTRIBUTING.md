Contributing
============

Want to contribute? Great! First, read this page (including the small print at the end).

# Code reviews
All submissions, including submissions by project members, require review. We
use Github pull requests for this purpose.

# The small print
Contributions made by corporations are covered by a different agreement than
the one above, the Software Grant and Corporate Contributor License Agreement.

# Building

The following commands will clone the GitHub repository and run all the RE2/J
tests:

```
$ git clone https://github.com/smmckay/re2jit.git
$ cd re2jit
$ ./gradlew check
```

# Running the benchmarks

You can run the benchmarks by checking out the GitHub repository and then
running the following command:

```
$ ./gradlew benchmarks
```
