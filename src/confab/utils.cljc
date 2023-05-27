(ns confab.utils
  (:require [clojure.test.check.generators :as gen]
            [cljc.java-time.instant :as instant]))

;;; --------------------------------
;;; Main
;;; --------------------------------

(defn generate
  "A wrapper around clojure.test.check.generators/generate to accommodate seeds"
  ([{:keys [size seed], :or {size 30}} generator]
   (let [args (filter some? [size seed])] 
     (apply gen/generate generator args)))
  ([generator] (generate {} generator)))