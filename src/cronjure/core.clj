(ns cronjure.core
  (:refer-clojure :exclude [and])
  (:require [clojure.string :as string]
            [cronjure.definitions :as definitions])
  (:import [com.cronutils.builder CronBuilder]
           [com.cronutils.descriptor CronDescriptor]
           [com.cronutils.model.definition CronDefinitionBuilder CronConstraintsFactory CronDefinition]
           [com.cronutils.model.field CronFieldName]
           [com.cronutils.model.field.expression FieldExpressionFactory]
           [com.cronutils.model.time ExecutionTime]
           [com.cronutils.parser CronParser]
           [java.time ZonedDateTime]
           [java.util Locale]))

(defn constraints->map [constraints]
  {:start-range (.getStartRange constraints)
   :end-range (.getEndRange constraints)
   :special-chars (mapv #(.toString %) (.getSpecialChars constraints))})

(defn- field->map [field]
  {:order (.getOrder (.getField field))
   :expression (.asString (.getExpression field))
   :constraints (constraints->map (.getConstraints field))})

(defn- field-definition->map [field-definition]
  {:optional (.isOptional field-definition)
   :constraints (constraints->map (.getConstraints field-definition))})

(defn- format-duration [duration format]
  (case format
    :nanos (.toNanos duration)
    :millis (.toMillis duration)
    :seconds (.getSeconds duration)
    :minutes (.toMinutes duration)
    :hours (.toHours duration)
    :days (.toDays duration)
    (throw (Exception. "Format not supported."))))

(defn as-string [cron-instance]
  (.asString cron-instance))

(defn describe
  ([cron-instance]
   (describe cron-instance (Locale/getDefault)))
  ([cron-instance locale]
   (.describe (CronDescriptor/instance locale) cron-instance)))

(defn get-instance-field [cron-instance field-name]
  (when-let [field (.retrieve cron-instance (CronFieldName/valueOf (-> field-name name (string/replace #"-" "_") string/upper-case)))]
    (field->map field)))

(defn get-instance-field-mapping [cron-instance]
  (->> cron-instance
      .retrieveFieldsAsMap
      (map
        (fn [[field-name field]]
          (hash-map (.toString field-name) (field->map field))))
      (into {})))

(defn get-definition [cron-instance]
  (.getCronDefinition cron-instance))

(defn get-definition-field [definition field-name]
  (when-let [field-definition (.getFieldDefinition definition (CronFieldName/valueOf (-> field-name name (string/replace #"-" "_") string/upper-case)))]
    (field-definition->map field-definition)))

(defn get-definition-field-mapping [definition]
  (->> definition
      .retrieveFieldDefinitionsAsMap
      (map
        (fn [[field-name field-definition]]
          (hash-map (.toString field-name) (field-definition->map field-definition))))
      (into {})))

(defn next-execution-date [cron-instance]
  (-> (ExecutionTime/forCron cron-instance)
      (.nextExecution (ZonedDateTime/now))
      .get))

(defn last-execution-date [cron-instance]
  (-> (ExecutionTime/forCron cron-instance)
      (.lastExecution (ZonedDateTime/now))
      .get))

(defn time-to-next-execution
  ([cron-instance]
   (time-to-next-execution cron-instance nil))
  ([cron-instance format]
   (cond-> (-> (ExecutionTime/forCron cron-instance) (.timeToNextExecution (ZonedDateTime/now)) .get)
           format (format-duration format))))

(defn time-since-last-execution
  ([cron-instance]
   (time-since-last-execution cron-instance nil))
  ([cron-instance format]
   (cond-> (-> (ExecutionTime/forCron cron-instance) (.timeFromLastExecution (ZonedDateTime/now)) .get)
           format (format-duration format))))

(defn parse [definition string-expression]
  (-> definition
      CronParser.
      (.parse string-expression)))

; field expressions

(def always (FieldExpressionFactory/always))

(def question-mark (FieldExpressionFactory/questionMark))

(defn between [from to]
  (FieldExpressionFactory/between from to))

(defn on
  ([value]
   (FieldExpressionFactory/on value))
  ([time special-char]
   (FieldExpressionFactory/on time special-char))
  ([time special-char nth]
   (FieldExpressionFactory/on time special-char nth)))

(defn every
  ([time]
   (FieldExpressionFactory/every time))
  ([expression time]
   (FieldExpressionFactory/every expression time)))

(defn and [expressions]
  (FieldExpressionFactory/and expressions))

; instance builder

(defn with-second [builder expression]
  (.withSecond builder expression))

(defn with-minute [builder expression]
  (.withMinute builder expression))

(defn with-hour [builder expression]
  (.withHour builder expression))

(defn with-dow [builder expression]
  (.withDoW builder expression))

(defn with-month [builder expression]
  (.withMonth builder expression))

(defn with-dom [builder expression]
  (.withDoM builder expression))

(defn with-year [builder expression]
  (.withYear builder expression))

(defn with-doy [builder expression]
  (.withDoY builder expression))

; definition builder

(defmacro with-seconds [builder & opts]
  `(-> ~builder
       .withSeconds
       ~@opts
       .and))

(defmacro with-minutes [builder & opts]
  `(-> ~builder
       .withMinutes
       ~@opts
       .and))

(defmacro with-hours [builder & opts]
  `(-> ~builder
       .withHours
       ~@opts
       .and))

(defmacro with-day-of-month [builder & opts]
  `(-> ~builder
       .withDayOfMonth
       ~@opts
       .and))

(defmacro with-months [builder & opts]
  `(-> ~builder
       .withMonth
       ~@opts
       .and))

(defmacro with-day-of-week [builder & opts]
  `(-> ~builder
       .withDayOfWeek
       ~@opts
       .and))

(defmacro with-years [builder & opts]
  `(-> ~builder
       .withYear
       ~@opts
       .and))

(defmacro with-day-of-year [builder & opts]
  `(-> ~builder
       .withDayOfYear
       ~@opts
       .and))

(defmacro with-cron-validation [builder & args]
  `(-> ~builder
       (.withCronValidation ~@args)))

(defn optional [builder]
  (.optional builder))

(defn with-valid-range [builder from to]
  (.withValidRange builder from to))

(defn with-monday-dow-value [builder monday-dow]
  (.withMondayDoWValue builder monday-dow))

(defn with-int-mapping [builder from to]
  (.withIntMapping builder from to))

(defn supports-question-mark [builder]
  (.supportsQuestionMark builder))

(defn supports-hash [builder]
  (.supportsHash builder))

(defn supports-l [builder]
  (.supportsL builder))

(defn supports-w [builder]
  (.supportsW builder))

(defn supports-lw [builder]
  (.supportsLW builder))

(def ensure-either-day-of-year-or-month (CronConstraintsFactory/ensureEitherDayOfYearOrMonth))

(def ensure-either-day-of-week-or-day-of-month (CronConstraintsFactory/ensureEitherDayOfWeekOrDayOfMonth))

(defn enforce-strict-ranges [builder]
  (.enforceStrictRanges builder))

(defn match-day-of-week-and-day-of-month [builder]
  (.matchDayOfWeekAndDayOfMonth builder))

; builders

(defmacro build-definition [& forms]
  `(-> (CronDefinitionBuilder/defineCron)
       ~@forms
       .instance))

(defmacro build-instance [definition & forms]
  `(-> (CronBuilder/cron ~definition)
       ~@forms
       .instance))
