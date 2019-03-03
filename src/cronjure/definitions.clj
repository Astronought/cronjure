(ns cronjure.definitions
  (:import [com.cronutils.model CronType]
           [com.cronutils.model.definition CronDefinitionBuilder]))

(def quartz (CronDefinitionBuilder/instanceDefinitionFor CronType/QUARTZ))

(def cron4j (CronDefinitionBuilder/instanceDefinitionFor CronType/CRON4J))

(def spring (CronDefinitionBuilder/instanceDefinitionFor CronType/SPRING))

(def unix (CronDefinitionBuilder/instanceDefinitionFor CronType/UNIX))
