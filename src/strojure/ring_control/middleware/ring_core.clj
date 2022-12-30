(ns strojure.ring-control.middleware.ring-core
  "Builder configuration functions for the middlewares from the
  `ring.middleware` namespace."
  (:require [ring.middleware.content-type :as content-type]
            [ring.middleware.keyword-params :as keyword-params]
            [ring.middleware.params :as params]
            [strojure.ring-control.config :as config]))

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn- tag-options-fn
  [tag]
  (-> (fn [& {:as options}]
        (-> (or options {})
            (config/with-type-tag tag)))
      (config/with-type-tag tag)))

(defn as-wrap-request
  "Returns wrap method implementation for ring request function."
  [tag, ring-request-fn, as-wrap-opts]
  (letfn [(wrap-fn [obj]
            (let [options (if (map? obj) obj {})]
              (fn wrap-request [request]
                (ring-request-fn request options))))]
    (config/as-wrap-request tag wrap-fn as-wrap-opts)
    (tag-options-fn tag)))

(defn as-wrap-response
  "Returns wrap method implementation for ring response function."
  [tag, ring-response-fn, as-wrap-opts]
  (letfn [(wrap-fn [obj]
            (let [options (if (map? obj) obj {})]
              (fn wrap-response [response request]
                (ring-response-fn response request options))))]
    (config/as-wrap-response tag wrap-fn as-wrap-opts)
    (tag-options-fn tag)))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(def ^{:arglists '([& {:as options}])}
  params-request
  "Middleware to parse urlencoded parameters from the query string and form
  body (if the request is an url-encoded form). Adds the following keys to
  the request map:

  - `:query-params` – a map of parameters from the query string
  - `:form-params`  – a map of parameters from the body
  - `:params`       – a merged map of all types of parameter

  Accepts the following options:

  - `:encoding` – encoding to use for url-decoding. If not specified, uses
                  the request character encoding, or \"UTF-8\" if no request
                  character encoding is set.
  "
  (as-wrap-request `params-request params/params-request
                   {:tags [::params-request]}))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(def ^{:arglists '([& {:as options}])}
  keyword-params-request
  "Middleware that converts any string keys in the :params map to keywords.
  Only keys that can be turned into valid keywords are converted.

  This middleware does not alter the maps under `:*-params` keys. These are left
  as strings.

  Accepts the following options:

  - `:parse-namespaces?` – if true, parse the parameters into namespaced
                           keywords (defaults to false)
  "
  (as-wrap-request `keyword-params-request keyword-params/keyword-params-request
                   {:tags [::keyword-params-request]
                    :requires {:enter [`params-request]}}))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(def ^{:arglists '([& {:as options}])}
  content-type-response
  "Middleware that adds a content-type header to the response if one is not
  set by the handler. Uses the `ring.util.mime-type/ext-mime-type` function to
  guess the content-type from the file extension in the URI. If no
  content-type can be found, it defaults to 'application/octet-stream'.

  Accepts the following options:

  - `:mime-types` – a map of filename extensions to mime-types that will be
                    used in addition to the ones defined in
                    `ring.util.mime-type/default-mime-types`
  "
  (as-wrap-response `content-type-response content-type/content-type-response
                    {:tags [::content-type-response]}))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
