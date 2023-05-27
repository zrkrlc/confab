(ns confab.core
  (:require [clojure.set :as set]
            [clojure.pprint :refer [pprint]]

            #?(:clj [clojure.spec.alpha :as spec]
               :cljs [cljs.spec.alpha :as spec])
            #?(:clj [clojure.spec.gen.alpha :as gen]
               :cljs [cljs.spec.gen.alpha :as gen])))

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
                :confab/radix
                :confab/binary}

   :internet  #{:confab/username
                :confab/email
                :confab/url
                :confab/ipv4
                :confab/ipv6}})

(def ^:private directory-keys
  (->> directory
     vals
     (apply set/union)))


;;; --------------------------------
;;; Core fns
;;; --------------------------------

(defmulti confab
  (fn [arg & _]
    (cond
      (and (keyword? arg)
           (not (spec/get-spec arg))
           (directory-keys arg))         arg

      (and (vector? arg)
           (directory-keys (first arg))) :confab/schema-vector

      (map? arg)                         :confab/schema-map

      :else                              :confab/spec-or-identity)))

(defmethod confab :confab/spec-or-identity [arg & _]
  (if (spec/get-spec arg)
    (gen/generate (spec/gen arg))
    arg))

(defmethod confab :confab/schema-vector [[keyword opts] & _]
  (confab keyword opts))


(defmethod confab :confab/schema-map [arg & _]
  (update-vals arg confab))



;;; --------------------------------
;;; Datatype
;;; --------------------------------

;; Boolean
(defmethod confab :confab/boolean 
  [_ & _] 
  (gen/generate (gen/boolean)))


;; String
;; TODO: add :length option
(defmethod confab :confab/string 
  [_ & _] 
  (gen/generate (gen/string-alphanumeric)))


;; Inst
;; TODO: fix this
(defmethod confab :confab/inst
  [_ & [{:keys [start end] :or {start #?(:clj (java.util.Date. 0)
                                         :cljs (js/Date. 0))
                                end   #?(:clj (java.util.Date.)
                                         :cljs (js/Date.))}}]]
  (gen/generate ( start end)))

(defmethod confab :confab/integer
  [_ & [{:keys [min max] :or {min #?(:clj Integer/MIN_VALUE
                                     :cljs js/Integer.MIN_VALUE)
                              max #?(:clj Integer/MAX_VALUE
                                     :cljs js/Integer.MAX_VALUE)}}]]
  (gen/generate (gen/choose min max)))

(defmethod confab :confab/float
  [_ & [{:keys [min max] :or {min #?(:clj Float/MIN_VALUE
                                     :cljs js/Number.MIN_VALUE)
                              max #?(:clj Float/MAX_VALUE
                                     :cljs js/Number.MAX_VALUE)}}]]
  (gen/generate (gen/double min max)))

(defmethod confab :confab/hexadecimal
  [_ & [{:keys [length] :or {length 6}}]]
  (gen/generate (gen/fmap #(apply str %) (gen/vector gen/hex-digit length))))

(defmethod confab :confab/octal
  [_ & [{:keys [length] :or {length 6}}]]
  (gen/generate (gen/fmap #(apply str %) (gen/vector (gen/choose 0 7) length))))

(defmethod confab :confab/radix
  [_ & [{:keys [length radix] :or {length 6
                                   radix 10}}]]
  (gen/generate (gen/fmap #(apply str %) 
                          (gen/vector (gen/choose 0 (dec radix)) length))))

(defmethod confab :confab/binary
  [_ & [{:keys [length] :or {length 8}}]]
  (gen/generate (gen/fmap #(apply str %) (gen/vector gen/bit length))))




(comment
  (map gen/generate
       [(gen/ gen/vector gen/pos-int)])
  )