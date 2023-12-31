(ns wordsmith.components
  (:require
   [hiccup.page :as hpage]))

(defn auth-container
  "Renders a container for authentication pages."
  [{:keys [title]} & content]
  (hpage/html5
   [:head
    [:title title]
    [:link {:rel "stylesheet"
            :href "/css/auth.css"}]]
   [:body
    [:div.auth-container
     content]]))

(def *csrf (atom nil))

(defn csrf []
  (let [csrf (str (random-uuid))]
    (reset! *csrf csrf)
    [:input {:type "hidden"
             :name "_csrf"
             :value csrf}]))

(defn login-form
  "Renders a login form."
  [{:keys [flash]}]
  [:form {:method "post"}
   (csrf)
   (when (:error flash)
     [:p {:class "error"} (:error flash)])
   [:label {:for "email"} "E-mail"]
   [:input {:type "email" :name "email"}]
   [:label {:for "password"} "Password"]
   [:input {:type "password" :name "password"}]
   [:button {:type "submit"} "Login"]])

(defn favicon
  [letter]
  [:svg {:xmlns "http://www.w3.org/2000/svg"
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
    letter]])


