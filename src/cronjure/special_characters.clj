(ns cronjure.special-characters
  (:refer-clojure :exclude [hash])
  (:import [com.cronutils.model.field.value SpecialChar]))

(def lw (SpecialChar/LW))

(def l (SpecialChar/L))

(def hash (SpecialChar/HASH))

(def question-mark (SpecialChar/QUESTION_MARK))

(def none (SpecialChar/NONE))
