(ns wordsmith.data
  (:require
   [clojure.java.io :as io]
   [crypto.password.bcrypt :as password])
  (:import
   [java.io File]))

(def ^:private data-path "./wordsmith/")

(defn- fetch-record
  "Fetches a record from a given `path`.
  Returns `nil` if the record does not exist."
  [path]
  (let [full-path (str data-path path ".edn")
        file ^File (io/file full-path)]
    (when (.exists file)
      (read-string (slurp file)))))

(defn- fetch-records
  "Fetches all records from a given `path`.
  Returns an empty vector if the path does not exist or is empty."
  [path]
  (let [full-path (str data-path path)]
    (->> (.list (io/file full-path))
         (map #(str full-path File/separatorChar (.getPath (io/file %))))
         (pmap #(if (.isDirectory %) (fetch-records %) (fetch-record %)))
         flatten
         vec)))

(defn- update-record!
  "Updates a record at a given `path` with given `data`.
  If the record does not exist, it is created.
  If the record exists, it is merged with the given `data`."
  [path data]
  (let [existing-data (fetch-record path)
        full-path (str data-path path ".edn")
        file ^File (io/file full-path)]
    (io/make-parents full-path)
    (spit file (pr-str (merge existing-data data)))))

(defn setup!
  "Sets up the data directory."
  []
  (let [account-path (str data-path "account.edn")]
    (when-not (.exists (io/file account-path))
      (io/make-parents account-path)
      (spit account-path {:email "wordsmith@wordsmith"
                          :password (password/encrypt "wordsmith")}))))

(defn authenticate-account
  "Authenticates an account with given `email` and `password`.
  Upon successful authentication, a new token is generated and stored
  in the account record, and the token is returned."
  [email password]
  (when-let [account-record (fetch-record "account")]
    (when (and (= (:email account-record) email)
               (password/check password (:password account-record)))
      (let [token (str (random-uuid))]
        (update-record! "account" {:token token})
        token))))









