(ns wordsmith.handlers)

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

