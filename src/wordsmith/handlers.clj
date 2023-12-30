(ns wordsmith.handlers
  (:require
   [hiccup2.core :as h]
   [wordsmith.components :refer [auth-container csrf]]
   [wordsmith.data :as data]))

(def login
  ^{:request/method :get
    :request/path "/admin/login"
    :response/status 200
    :response/type :html}
  (fn [_]
    (auth-container
     "Login"
     [:form {:method "post"}
      (csrf)
      [:label {:for "email"} "E-mail"]
      [:input {:type "email" :name "email"}]
      [:label {:for "password"} "Password"]
      [:input {:type "password" :name "password"}]
      [:button {:type "submit"} "Login"]])))

(def do-login
  ^{:request/method :post
    :request/path "/admin/login"
    :response/status 200
    :response/type :redirect}
  (fn [{:keys [body]}]
    (let [{:keys [email password]} body]
      (if-let [auth-token (data/authenticate-account email password)]
        {:to "/admin/posts"
         :cookie {:token auth-token}}
        {:to "/admin/login"
         :flash {:error "Invalid e-mail or password."}}))))

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




