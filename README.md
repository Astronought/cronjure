# Cronjure

Cronjure is a thin Clojure wrapper for the popular [cron-utils](https://github.com/jmrozanec/cron-utils) library.

## Installation

### Leiningen

```clojure
[cronjure "0.1.0"]
```

### Deps.edn

```clojure
{cronjure {:mvn/version "0.1.0"}}
```

## Usage

```clojure
(require '[cronjure.core :refer :all])
```

### Define a custom cron definition

```clojure
(def my-cron-definition
  (build-definition
    with-seconds
    (with-minutes
      (with-valid-range 0 60)
      (with-int-mapping 60 0))
    with-hours
    with-day-of-month
    with-months
    (with-day-of-week
      supports-l
      supports-question-mark)))
```

### Create a cron instance

Creating a cron instance requires a cron definition, pre-defined definitions can be found
under ```cronjure.definitions```.


```clojure
(def my-cron-instance
  (build-instance my-cron-definition
    (with-second (on 59))
    (with-minute (between 15 30))
    (with-hour (on 10))
    (with-dow (on 6 cronjure.special-characters/l))
    (with-month always)))
```

### String representation

```clojure
(as-string my-cron-instance) ; => "59 15-30 10 * 6L"
```

### Describe

Takes an optional locale, uses the default locale if not specified.

```clojure
(describe my-cron-instance) ; => "every minute between 10:15 and 10:30 last Saturday of every month"

(describe my-cron-instance (java.util.Locale/FRENCH)) ; => "chaque minute entre 10:15 et 10:30 dernier samedi de chaque mois"
```

### Execution date/time

```clojure
(next-execution-date my-cron-instance) ; => java.time.ZonedDateTime

(last-execution-date my-cron-instance) ; => java.time.ZonedDateTime
```

Two similar functions are also provided which return instances of a ```java.time.Duration```.

```clojure
(time-to-next-execution my-cron-instance) ; => java.time.Duration

(time-since-last-execution my-cron-instance) ; => java.time.Duration
```

A format can be optionally supplied.

```clojure
(time-to-next-execution my-cron-instance :minutes) ; => e.g. 42

(time-to-next-execution my-cron-instance :seconds) ; => e.g. 2520
```

### Parsing

Cron instances can also be created using strings.

```clojure
(->> "0/30 55 18 ? * 1"
     (parse cronjure.definitions/quartz)
     describe
; => "every 30 seconds at 55 minute at 18 hour at Sunday day"
```

### Field information

Information about specific fields can be retrieved from either a cron definition/instance.

```clojure
(get-instance-field my-cron-instance :minute)
; => {:order 1,
;    :expression "15-30",
;    :constraints {:start-range 0, :end-range 60, :special-chars ["NONE"]}}

(get-definition-field my-cron-definition :minute)
;=> {:optional false,
;    :constraints {:start-range 0, :end-range 60, :special-chars ["NONE"]}}
```

## License

Copyright © 2019 Astronought

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
