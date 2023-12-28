(ns wordsmith.handlers
  (:require
    [hiccup2.core :as h]))

(def login 
  ^{:request/method :get
    :request/path "/admin/login"
    :response/status 200
    :response/type :html}
  (fn [req]
    "Login goes here."))

(def do-login
  ^{:request/method :post
    :request/path "/admin/login"
    :response/status 200
    :response/type :html}
  (fn [req]
    "Login post goes here."))

(def logout
  ^{:request/method :get
    :request/path "/admin/logout"
    :response/status 200
    :response/type :html}
  (fn [req]
    "Logout goes here."))

(def admin-posts
  ^{:request/method :get
    :request/path "/admin/posts"
    :response/status 200
    :response/type :html}
  (fn [req]
    "Admin posts go here."))

(def admin-post
  ^{:request/method :get
    :request/path "/admin/posts/:id"
    :response/status 200
    :response/type :html}
  (fn [req]
    (prn (:route-params req))
    "Admin post goes here."))

;; TODO: Get site title from config.
(def favicon 
  ^{:request/method :get
    :request/path "/favicon.ico"
    :response/status 200
    :response/type "image/svg+xml"}
  (fn [_]
    (-> [:svg {:xmlns "http://www.w3.org/2000/svg"
               :width "100"
               :height "100"
               :viewBox "0 0 100 100"}
         [:circle {:cx "50"
                   :cy "50"
                   :r "50"
                   :fill "#111"}]
         [:text {:x "50%"
                 :y "50%"
                 :text-anchor "middle"
                 :fill "#fff"
                 :font-size "4em"
                 :font-family "Arial, Helvetica, sans-serif"
                 :font-weight "bold"
                 :dy ".3em"}        
          "A"]]
        h/html
        str)))

(def blog-posts
  ^{:request/method :get
    :request/path "/"
    :response/status 200
    :response/type :html}
  (fn [req]
    "Posts go here."))

(def blog-post
  ^{:request/method :get
    :request/path "/:id"
    :response/status 200
    :response/type :html}
  (fn [req]
    (prn (:route-params req))
    "Post goes here."))

