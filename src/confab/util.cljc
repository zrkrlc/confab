(ns confab.util
  (:require [cljc.java-time.instant :as instant]))


;;; --------------------------------
;;; Datatypes
;;; --------------------------------

;; TODO: use cljc.java-time to fix this
(defn- inst->days [inst]
  #?(:clj  (.toEpochMilli inst)
     :cljs (.getTime inst)))

;; TODO: use cljc.java-time to fix this
(defn- days->inst [days]
  #?(:clj  (java.time.Instant/ofEpochMilli days)
     :cljs (js/Date. days)))

;; TODO: use cljc.java-time to fix this
(defn- random-date [start end]
  (let [start-time (inst->days start)
        end-time (inst->days end)
        random-time (+ start-time (rand-int (- end-time start-time)))]
    (days->inst random-time)))

(comment
  (inst->days (java.util.Date. .now))

  (-> (new java.util.Date)
      (.getTime)
      (instant/of-epoch-milli)))
