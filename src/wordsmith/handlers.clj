(ns wordsmith.handlers
  (:require
   [hiccup2.core :as h]
   [wordsmith.components :as components]
   [wordsmith.data :as data]))

(def login
  ^{:request/method :get
    :request/path "/admin/login"
    :response/status 200
    :response/type :html}
  (fn [{:keys [flash]}]
    (components/auth-container
     {:title "Login"}
     (components/login-form {:flash flash}))))

(def do-login
  ^{:request/method :post
    :request/path "/admin/login"
    :response/status 200
    :response/type :redirect}
  (fn [{:keys [body]}]
    (let [{:keys [email password]} body]
      (if-let [auth-token (data/authenticate-account email password)]
        {:to "/admin/posts"
         :cookie [:token auth-token]}
        {:to "/admin/login"
         :flash {:error "Invalid e-mail or password."}}))))

(def logout
  ^{:request/method :get
    :request/path "/admin/logout"
    :response/status 200
    :response/type :redirect}
  (fn [_]
    {:to "/admin/login"
     :cookie [:token nil]}))

(def admin-posts
  ^{:request/method :get
    :request/path "/admin/posts"
    :response/status 200
    :response/type :html}
  (fn [_]
    "Admin posts go here."))

(def admin-create-post
  ^{:request/method :get
    :request/path "/admin/posts/new"
    :response/status 200
    :response/type :redirect}
  (fn [_]
    (let [id nil]
      {:to (str "/admin/posts/" id)})))

(def admin-edit-post
  ^{:request/method :get
    :request/path "/admin/posts/:id"
    :response/status 200
    :response/type :html}
  (fn [req]
    (prn (:route-params req))
    "Admin post goes here."))

(def admin-delete-post
  ^{:request/method :get
    :request/path "/admin/posts/:id/delete"
    :response/status 200
    :response/type :redirect}
  (fn [req]
    (prn (:route-params req))
    {:to "/admin/posts"}))

;; TODO: Get site title from config.
(def favicon
  ^{:request/method :get
    :request/path "/favicon.ico"
    :response/status 200
    :response/type "image/svg+xml"}
  (fn [_]
    (-> (components/favicon "A")
        h/html
        str)))

(def blog-posts
  ^{:request/method :get
    :request/path "/"
    :response/status 200
    :response/type :html}
  (fn [_]
    "Posts go here."))

(def blog-post
  ^{:request/method :get
    :request/path "/:id"
    :response/status 200
    :response/type :html}
  (fn [req]
    (prn (:route-params req))
    "Post goes here."))




