(ns wordsmith.core
  (:require
   [clojure.data.json :as json]
   [clojure.string :as string]
   [hiccup2.core :as h]
   [org.httpkit.server :as http]
   [wordsmith.components :refer [*csrf]]
   [wordsmith.handlers :as handlers]
   [wordsmith.data :as data])
  (:import
   (java.net URLDecoder)))

(def registered-handlers
  [handlers/login
   handlers/do-login
   handlers/logout
   handlers/admin-posts
   handlers/admin-create-post
   handlers/admin-edit-post
   handlers/admin-delete-post
   handlers/favicon
   handlers/blog-posts
   handlers/blog-post])

(defn- uri-matches-path?
  "Predicate fn that checks if a given `uri` matches a given `path`. 
  It also has support for path parameters, which are prefixed with a colon."
  [^String uri ^String path]
  (let [uri-parts (string/split uri #"/")
        path-parts (string/split path #"/")
        joined-parts (into [] (map #(vector %1 %2) uri-parts path-parts))]
    (and (= (count uri-parts) (count path-parts))
         (every? #(or (= (first %) (second %))
                      (string/starts-with? (second %) ":"))
                 joined-parts))))

(defn- req-matches-handler?
  "Predicate fn that checks if a given `req` matches a given `handler` by 
  comparing the request's URI and method with the handler's URI and method."
  [req handler]
  (let [m (meta handler)]
    (and (uri-matches-path? (:uri req) (:request/path m))
         (= (:request-method req) (:request/method m)))))

(defn- uri-kw-parts
  "Returns a map of path parameters and their values from a given `uri` 
  and `path`."
  [^String uri ^String path]
  (let [uri-parts (string/split uri #"/")
        path-parts (string/split path #"/")
        joined-parts (into [] (map #(vector %1 %2) uri-parts path-parts))]
    (->> (map #(when (string/starts-with? (second %) ":")
                 {(keyword (subs (second %) 1)) (first %)})
              joined-parts)
         (remove nil?)
         (into {}))))

(defn- find-handler
  "Returns the first handler that matches a given `req` from a given 
  list of `handlers`."
  [req]
  (->> registered-handlers
       (filter #(req-matches-handler? req %))
       first))

(defn- with-headers
  "Returns a new map with the given `headers` merged with the given `map`."
  [{:keys [cookie]} headers]
  (merge
   headers
   (when-let [[k v] cookie]
     (if (nil? v)
       {"Set-Cookie" (str (subs (str k) 1) "=; max-age=0; Path=/")}
       {"Set-Cookie" (str (subs (str k) 1) "=" v "; max-age=31536000; Path=/")}))))

(def ^:private *flash-data (atom nil))

(defmulti parse-response
  (fn [{:keys [flash]} response-type]
    (if flash
      (reset! *flash-data flash)
      (reset! *flash-data nil))
    response-type))

(defmethod parse-response :redirect
  [response _]
  {:status (or (:response/status (meta response)) 302)
   :headers (with-headers response {"Location" (:to response)})
   :body ""})

(defmethod parse-response :hiccup
  [response _]
  {:status (or (:response/status (meta response)) 200)
   :headers (with-headers response {"Content-Type" "text/html"})
   :body (-> response h/html str)})

(defmethod parse-response :html
  [response _]
  {:status (or (:response/status (meta response)) 200)
   :headers (with-headers response {"Content-Type" "text/html"})
   :body response})

(defmethod parse-response :json
  [response _]
  {:status (or (:response/status (meta response)) 200)
   :headers (with-headers response {"Content-Type" "application/json"})
   :body (json/write-str response)})

(defmethod parse-response :default
  [response _]
  {:status (or (:response/status (meta response)) 200)
   :headers (with-headers response {"Content-Type" "text/plain"})
   :body response})

(defn- resolve-handler
  "Resolves a given `handler` with a given `req` by calling the handler 
  with the request and adding the path parameters to the request."
  [handler req]
  (let [m (meta handler)
        parsed-req (assoc req :route-params (uri-kw-parts (:uri req) (:request/path m)))
        request-middleware (:request/middleware m)]
    (if request-middleware
      (let [middlewared-handler-fn (request-middleware parsed-req handler)
            response-type (:response/type (meta middlewared-handler-fn))]
        (parse-response (middlewared-handler-fn parsed-req) response-type))
      (parse-response (handler parsed-req) (:response/type m)))))

(defn- parse-request-post-body
  "Parses the request's POST body and returns a map of the form data."
  [req]
  (let [body (slurp (:body req))
        params (string/split body #"&")
        params (map #(string/split % #"=") params)
        params (map #(vector (keyword (first %))
                             (URLDecoder/decode (or (second %) "") "UTF-8"))
                    params)]
    (into {} params)))

(defn- parse-request-cookies
  "Parses the request's cookies and returns a map of the cookies."
  [req]
  (let [cookies (get-in req [:headers "cookie"])
        cookies (string/split cookies #"; ")
        cookies (map #(string/split % #"=") cookies)
        cookies (map #(vector (keyword (first %))
                              (second %))
                     cookies)]
    (into {} cookies)))

(defn- parse-req
  "Parses the request and returns a map of the request's data.
  If the request is a POST request, it also parses the POST body and
  adds it to the map. It also checks the CSRF token and adds a `csrf-ok?`
  key to the map."
  [req]
  (merge req
         {:cookies (parse-request-cookies req)}
         {:flash @*flash-data}
         (when (= (:request-method req) :post)
           (let [parsed-body (parse-request-post-body req)]
             (merge
              {:body parsed-body}
              {:csrf-ok? (let [csrf-token @*csrf]
                           (reset! *csrf (str (random-uuid)))
                           (= (:_csrf parsed-body) csrf-token))})))))

(defn- app-handler
  "The main handler for the application. It parses the request, checks
  the CSRF token, and then tries to find a handler for the request. If
  a handler is found, it resolves the handler with the request. If no
  handler is found, it returns a 404 response."
  [req]
  (let [{:keys [csrf-ok?] :as parsed-req} (parse-req req)
        req (dissoc parsed-req :csrf-ok?)]
    (if (and (not (nil? csrf-ok?))
             (not csrf-ok?))
      {:status 403
       :headers {"Content-Type" "text/html"}
       :body "Forbidden"}
      (if-let [handler (find-handler req)]
        (resolve-handler handler req)
        {:status 404
         :headers {"Content-Type" "text/html"}
         :body "Not Found"}))))

#_:clj-kondo/ignore
(defn run [_]
  (println "Starting server on port 8080")
  (data/setup!)
  (http/run-server app-handler {:port 8080}))





