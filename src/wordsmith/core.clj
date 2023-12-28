(ns wordsmith.core
  (:require
   [clojure.string :as string]
   [org.httpkit.server :as http]
   [wordsmith.handlers :as handlers]))

(def registered-handlers
  [handlers/login
   handlers/do-login
   handlers/logout
   handlers/admin-posts
   handlers/admin-post
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
  (->> registered-handlers
       (filter #(matches-handler? % req))
       first))

(defn parse-response-type
  "Returns the correct MIME type for a given `response-type`."
  [response-type]
  (case response-type
    :html "text/html"
    :json "application/json"
    "text/plain"))

(defn resolve-handler
  "Resolves a given `handler` with a given `req` by calling the handler 
  with the request and adding the path parameters to the request."
  [handler req]
  (let [m (meta handler)
        result (handler (assoc req :route-params (uri-kw-parts (:uri req) (:request/path m))))]
    {:status (:response/status m)
     :headers {"Content-Type" (parse-response-type (:response/type m))}
     :body result}))

(defn app-handler
  [req]
  (if-let [handler (find-handler req)]
    (resolve-handler handler req)
    {:status 404
     :headers {"Content-Type" "text/html"}
     :body "Not Found"}))

(defn run [_]
  (println "Starting server on port 8080")
  (http/run-server app-handler {:port 8080}))





