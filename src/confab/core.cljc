(ns confab.core
  (:require [clojure.set :as set]
            [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]
            [cljc.java-time.instant :as instant]

            [confab.utils :as utils]))

;;; --------------------------------
;;; Directory
;;; --------------------------------

(def ^:private directory
  {:datatypes #{:confab/boolean
                :confab/string
                :confab/inst
                :confab/integer
                :confab/float
                :confab/hexadecimal
                :confab/octal
                :confab/binary
                :confab/radix}

   :internet  #{:confab/username
                :confab/email
                :confab/url
                :confab/ipv4
                :confab/ipv6}})

(def ^:private directory-keys
  (->> directory
       vals
       (apply set/union)))

(declare confab)
(defn ^:private tuple
  "Tag reader for confab tuples"
  [[k opts]]
  (confab k opts))




;;; --------------------------------
;;; Core fns
;;; --------------------------------

(defmulti confab
  (fn [arg & _]
    (cond
      (and (keyword? arg)
           (not (spec/get-spec arg))
           (directory-keys arg))         arg

      (and (keyword? arg)
           (spec/get-spec arg))          :confab/schema-spec
      
      (sequential? arg)                  :confab/schema-sequential

      (map? arg)                         :confab/schema-map

      :else                              :confab/schema-identity)))


(defmethod ^:private confab :confab/schema-spec [arg & [{:keys [size seed]}]]
  (utils/generate {:size size :seed seed} (spec/gen arg)))

(defmethod ^:private confab :confab/schema-sequential
  [pairs & _]
  (let [container (cond (vector? pairs) []
                        (list? pairs)   '())]
    (into container (for [pair pairs] (confab pair)))))

(defmethod ^:private confab :confab/schema-map [arg & _]
  (update-vals arg confab))

(defmethod ^:private confab :confab/schema-identity [arg & _]
  arg)



;;; --------------------------------
;;; Datatype
;;; --------------------------------

(defmethod confab :confab/boolean
  [_ & [{:keys [seed]}]]
  (utils/generate {:seed seed} gen/boolean))


(defmethod confab :confab/string
  [_ & [{:keys [seed length]}]]
  (if-not length
    (utils/generate {:seed seed} gen/string-alphanumeric)
    (utils/generate {:seed seed} (gen/fmap #(apply str %)
                                           (gen/vector gen/char-alpha length)))))


(defmethod confab :confab/inst
  [_ & [{:keys [seed start end]
         :or {start (instant/of-epoch-milli 0)
              end   (instant/now)}}]]
  (let [start-epoch (instant/to-epoch-milli start)
        end-epoch (instant/to-epoch-milli end)
        max-delta (- end-epoch start-epoch)

        milli-to-day (fn [ms] (/ ms (* 1000 60 60 24)))
        day-to-milli (fn [day] (* day (* 1000 60 60 24)))

        max-delta-day (milli-to-day max-delta)
        random-delta-day (utils/generate {:seed seed :size max-delta-day} gen/nat)]

    (instant/of-epoch-milli (+ start-epoch (day-to-milli random-delta-day)))))


(defmethod confab :confab/integer
  [_ & [{:keys [seed min max]}]]
  (let [min-default #?(:clj Integer/MIN_VALUE
                       :cljs js/Number.MIN_SAFE_INTEGER)
        max-default #?(:clj Integer/MAX_VALUE
                       :cljs js/Number.MAX_SAFE_INTEGER)]
    (if-not (or min max)
      (utils/generate {:seed seed} gen/small-integer)
      (utils/generate {:seed seed} (gen/choose (or min min-default) (or max max-default))))))


(defmethod confab :confab/float
  [_ & [{:keys [seed min max NaN? infinite?]
         :or   {NaN?      false
                infinite? false}}]]
  (let [min-default #?(:clj Float/MIN_VALUE
                       :cljs js/Number.MIN_VALUE)
        max-default #?(:clj Float/MAX_VALUE
                       :cljs js/Number.MAX_VALUE)]
    (utils/generate
     {:seed seed}
     (gen/double* {:min       (or min min-default)
                   :max       (or max max-default)
                   :NaN?      NaN?
                   :infinite? infinite?}))))


(defmethod confab :confab/hexadecimal
   [_ & [{:keys [seed min max]}]]
   (let [min-default #?(:clj Integer/MIN_VALUE
                        :cljs js/Number.MIN_SAFE_INTEGER)
         max-default #?(:clj Integer/MAX_VALUE
                        :cljs js/Number.MAX_SAFE_INTEGER)
         convert     #?(:clj (fn [x] (Integer/toHexString x))
                        :cljs #(.. % (toString 16)))]
     (-> (if-not (or min max)
           (utils/generate {:seed seed} gen/small-integer)
           (utils/generate {:seed seed} (gen/choose (or min min-default) (or max max-default))))
         convert)))


(defmethod confab :confab/octal
   [_ & [{:keys [seed min max]}]]
   (let [min-default #?(:clj Integer/MIN_VALUE
                        :cljs js/Number.MIN_SAFE_INTEGER)
         max-default #?(:clj Integer/MAX_VALUE
                        :cljs js/Number.MAX_SAFE_INTEGER)
         convert     #?(:clj (fn [x] (Integer/toOctalString x))
                        :cljs #(.. % (toString 8)))]
     (-> (if-not (or min max)
           (utils/generate {:seed seed} gen/small-integer)
           (utils/generate {:seed seed} (gen/choose (or min min-default) (or max max-default))))
         convert)))


(defmethod confab :confab/binary
   [_ & [{:keys [seed min max]}]]
   (let [min-default #?(:clj Integer/MIN_VALUE
                        :cljs js/Number.MIN_SAFE_INTEGER)
         max-default #?(:clj Integer/MAX_VALUE
                        :cljs js/Number.MAX_SAFE_INTEGER)
         convert     #?(:clj (fn [x] (Integer/toBinaryString x))
                        :cljs #(.. % (toString 2)))]
     (-> (if-not (or min max)
           (utils/generate {:seed seed} gen/small-integer)
           (utils/generate {:seed seed} (gen/choose (or min min-default) (or max max-default))))
         convert)))


(defmethod confab :confab/radix
   [_ & [{:keys [seed min max radix]
          :or   {radix 10}}]]
   (let [min-default #?(:clj Integer/MIN_VALUE
                        :cljs js/Number.MIN_SAFE_INTEGER)
         max-default #?(:clj Integer/MAX_VALUE
                        :cljs js/Number.MAX_SAFE_INTEGER)
         convert     #?(:clj (fn [x] (Integer/toString x radix))
                        :cljs #(.. % (toString radix)))]
     (-> (if-not (or min max)
           (utils/generate {:seed seed} gen/small-integer)
           (utils/generate {:seed seed} (gen/choose (or min min-default) (or max max-default))))
         convert)))

