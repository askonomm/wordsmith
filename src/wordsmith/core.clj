(ns wordsmith.core
  (:require
   [clojure.data.json :as json]
   [clojure.string :as string]
   [hiccup2.core :as h]
   [org.httpkit.server :as http]
   [wordsmith.components :refer [*csrf]]
   [wordsmith.handlers :as handlers])
  (:import
   (java.net URLDecoder)))

(def registered-handlers
  [handlers/login
   handlers/do-login
   handlers/logout
   handlers/admin-posts
   handlers/admin-post
   handlers/favicon
   handlers/blog-posts
   handlers/blog-post])

(defn uri-matches-path?
  "Predicate fn that checks if a given `uri` matches a given `path`. 
  It also has support for path parameters, which are prefixed with a colon."
  [uri path]
  (let [uri-parts (string/split uri #"/")
        path-parts (string/split path #"/")
        joined-parts (into [] (map #(vector %1 %2) uri-parts path-parts))]
    (and (= (count uri-parts) (count path-parts))
         (every? #(or (= (first %) (second %))
                      (string/starts-with? (second %) ":"))
                 joined-parts))))

(defn matches-handler?
  "Predicate fn that checks if a given `req` matches a given `handler` by 
  comparing the request's URI and method with the handler's URI and method."
  [handler req]
  (let [m (meta handler)]
    (and (uri-matches-path? (:uri req) (:request/path m))
         (= (:request-method req) (:request/method m)))))

(defn uri-kw-parts
  "Returns a map of path parameters and their values from a given `uri` 
  and `path`."
  [uri path]
  (let [uri-parts (string/split uri #"/")
        path-parts (string/split path #"/")
        joined-parts (into [] (map #(vector %1 %2) uri-parts path-parts))]
    (->> (map #(when (string/starts-with? (second %) ":")
                 {(keyword (subs (second %) 1)) (first %)})
              joined-parts)
         (remove nil?)
         (into []))))

(defn find-handler
  "Returns the first handler that matches a given `req` from a given 
  list of `handlers`."
  [req]
  (prn "REQ: " req)
  (->> registered-handlers
       (filter #(matches-handler? % req))
       first))

(defn parse-response-type
  "Returns the correct MIME type for a given `response-type`."
  [response-type]
  (if (string? response-type)
    response-type
    (case response-type
      :html "text/html"
      :hiccup "text/html"
      :json "application/json"
      "text/plain")))

(defn parse-response-body
  "Returns the correct response body for a given `response` and 
  `response-type`."
  [response response-type]
  (if (string? response-type)
    response
    (case response-type
      :hiccup (if (string? response)
                response
                (-> response h/html str))
      :json (json/write-str response)
      response)))

(defn resolve-handler
  "Resolves a given `handler` with a given `req` by calling the handler 
  with the request and adding the path parameters to the request."
  [handler req]
  (let [m (meta handler)
        result (handler (assoc req :route-params (uri-kw-parts (:uri req) (:request/path m))))]
    {:status (:response/status m)
     :headers {"Content-Type" (parse-response-type (:response/type m))}
     :body (parse-response-body result (:response/type m))}))

(defn parse-request-post-body
  "Parses the request's POST body and returns a map of the form data."
  [req]
  (let [body (slurp (:body req))
        params (string/split body #"&")
        params (map #(string/split % #"=") params)
        params (map #(vector (keyword (first %)) (URLDecoder/decode (or (second %) "") "UTF-8")) params)]
    (into {} params)))

(defn parse-req
  "Parses the request and returns a map of the request's data.
  If the request is a POST request, it also parses the POST body and
  adds it to the map. It also checks the CSRF token and adds a `csrf-ok?`
  key to the map."
  [req]
  (merge req
         (when (= (:request-method req) :post)
           (let [parsed-body (parse-request-post-body req)]
             {:parsed-body parsed-body}
             {:csrf-ok? (let [csrf-token @*csrf]
                          (reset! *csrf (str (random-uuid)))
                          (= (:_csrf parsed-body) csrf-token))})))) 

(defn app-handler
  [req]
  (let [parsed-req (parse-req req)]
    (if-let [handler (find-handler parsed-req)]
      (resolve-handler handler parsed-req)
      {:status 404
       :headers {"Content-Type" "text/html"}
       :body "Not Found"})))

#_:clj-kondo/ignore
(defn run [_]
  (println "Starting server on port 8080")
  (http/run-server app-handler {:port 8080}))











