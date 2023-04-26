# Confab

[![Clojars Project](https://img.shields.io/clojars/v/com.zrkrlc/confab.svg)](https://clojars.org/com.zrkrlc/confab)

[![Cljdoc](https://cljdoc.org/badge/com.zrkrlc/confab)](https://cljdoc.org/d/com.zrkrlc/confab)

– a Clojure/Script library for generating mock data

## Usage

Quick examples:

```clojure
(confab :confab/username)
; => "lazy_maplekoi98"

(confab :confab/sentence {:length 6})
; => "Nulla in orci non sapien hendrerit."

(confab :my.spec/blog-post {:seed 478868574082066804
                            :list? true
                            :count 2})
; => [{:post/title "Ornare taciti semper"
;      :post/body  "Primis iaculis elementum conubia feugiat venenatis dolor..."}
;     {:post/title "Curabitur semper venenatis"
;      :post/body  "Duis lectus porta mattis imperdiet..."}]
```

The entry point is a multimethod `confab` that takes in a keyword and an optional `opts` map. If `clojure.spec.alpha` is present, the spec corresponding to the keyword will, if it exists, be used to generate the data instead.

Confab can also take in a (possibly nested) schema of keys with `:confab/*` as values and it will generate a corresponding map:

```clojure
(confab {:user/name  :confab/user
         :user/email :confab/email
         :user/note  "This string will not be changed."
         :user/data  {:image-url :confab/image
                      :about     [:confab/sentence {:length 5, :question? true}]}})

; => {:user/name  "alt.sam626"
;     :user/email "sydney@openai.com"
;     :user/about "This string will not be changed."
;     :user/data  {:image-url "https://picsum.photos/200"
;                  :about     "Was I a good Bing?"}}
```

Constant values will not be changed, and singleton/pairs will be interpreted like they were arguments to a `confab` call. Invalid schemas will throw an exception, and `opts` map passed to the original `confab` call will in general have options that modify only the resulting map itself.

See [Modules](#Modules) for the available keywords and the options they accept.


## Modules

### Datatypes

#### `:confab/boolean`

#### `:confab/string`

#### `:confab/inst`
Not to be confused with `:confab/date` which spits out human-readable dates.

#### `:confab/integer`

#### `:confab/float`

#### `:confab/hexadecimal`

#### `:confab/octal`

#### `:confab/radix`

#### `:confab/binary`

### Internet

#### `:confab/username`

#### `:confab/email`

#### `:confab/url`

#### `:confab/ipv4`

#### `:confab/ipv6`

### Text

#### `:confab/word`

#### `:confab/phrase`

#### `:confab/sentence`

#### `:confab/paragraph`


### Time

#### `:confab/date`

#### `:confab/year`

#### `:confab/month`

#### `:confab/day`

#### `:confab/time`

#### `:confab/hour`

#### `:confab/minute`

#### `:confab/second`

#### `:confab/millisecond`

#### `:confab/week-number`

#### `:confab/week-day`


### Person

#### `:confab/name`

#### `:confab/first-name`

#### `:confab/last-name`

#### `:confab/gender`

#### `:confab/sex`

#### `:confab/job-title`

### Image

#### `:confab/image-url`

#### `:confab/avatar-url`

## Roadmap

* [ ] 1.0 - implement basic modules (datatype, internet, image, text, time, person)
* [ ] 1.1 - allow `confab` to consume specs; nested map ingestion 
* [ ] 1.2 - allow specs in maps; allow configs in maps; support vector generation in maps
* [ ] 1.3 - configs for basic modules
* [ ] 1.4 - more modules (identifier, language, address, phone, finance, color, quotes)
* [ ] ?.? - support for non-English languages, generate mock data from unlabeled maps, mocks for popular libraries (e.g. Hiccup), offline image generation, constraint-based generation (e.g., sentences without certain characters) transformer-based generation

## Attribution

Confab borrows heavily from the [faker.js](https://github.com/faker-js/faker) library.


## License

Copyright © 2023 Clark Urzo

Distributed under the Eclipse Public License version 1.0.