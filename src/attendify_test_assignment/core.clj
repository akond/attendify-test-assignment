(ns attendify-test-assignment.core
	(:require [clj-http.client :as client]
			  [clojure.pprint :refer :all]
			  [clojure.data.json :as json]
			  [clojure.core.async :as async :refer [<! >! <!! >!! timeout chan alt! go]]
			  [clojure.java.io :as io]
			  [clojure.string :as str]))


(def client-id "8f998845c37e52ead2c3c2fa47e87951214d147aa0dc2aced4e6a0595e172bc8")

(defn- api-request [access-token path & [query]]
	(let [path (if (str/starts-with? path "https://") path (str "https://api.dribbble.com/v1/" path))
		  response (client/get path
							   {:query-params (or query {"per_page" 100})
								:insecure?    true
								:headers      {"Authorization" (str "Bearer " access-token)}
								:accept       :json
								})
		  json (try (json/read-str (:body response)) (catch Exception e nil))]
		(assoc response :content json)
		))


(defn get-hash [type data]
	(.digest (java.security.MessageDigest/getInstance type) (.getBytes data)))

(defn sha1-hash [data]
	(get-hash "sha1" data))

(defn get-hash-str [data-bytes]
	(apply str
		   (map
			   #(.substring
					(Integer/toString
						(+ (bit-and % 0xff) 0x100) 16) 1)
			   data-bytes)
		   ))

(defn- create-api-channel [api]
	(let [in (chan)
		  out (chan)]
		(go
			(while true
				(let [path (<! in)]
					(println "Fetching " path)
					(let [id (get-hash-str (sha1-hash path))
						  file-name (str ".cache/" id)
						  exists? (.exists (io/as-file file-name))]

						(when-not (.exists (io/as-file file-name))
							(spit file-name (pr-str (api path))))

						(>! out (read-string (slurp file-name)))

						(when-not exists?
							(<! (timeout 1150)))
						))
				))

		(fn [path]
			(future
				(>!! in path)
				(<!! out)))
		))

(def api (create-api-channel (partial api-request client-id)))


(defn get-user [id]
	@(api (str "users/" id)))


(defn fetch-sequence
	([url]
	 (fetch-sequence identity url))

	([f url]
	 {:pre [(or (fn? f) (vector? f))]}

	 (let [f (if (fn? f) f #(get-in % f))]
		 (loop [r []
				response @(api url)]
			 (let [records (concat r (map (or f identity) (:content response)))]
				 (if-let [next (get-in response [:links :next :href])]
					 (recur records @(api next))
					 records))
			 ))
		))


(defn add-likes-up [likes]
	(->> likes
		 (map #(apply hash-map (interleave % (repeat 1))))
		 (apply merge-with +)))


(defn top-10 [likers-counters]
	(let [result (into {} (take-last 10 (sort-by val likers-counters)))]
		(into (sorted-map-by (fn [key1 key2]
								 (compare [(get result key2) key2]
										  [(get result key1) key1]))) result
			  ))
	)

(defn task1 []
	(let [user (get-user 1579965)
		  follower-urls (get-in user [:content "followers_url"])
		  follower-shot-urls (fetch-sequence ["follower" "shots_url"] follower-urls)
		  all-shots (mapcat fetch-sequence follower-shot-urls)
		  all-like-urls (map #(get % "likes_url") all-shots)
		  likes (map (partial fetch-sequence ["user" "id"]) all-like-urls)
		  counters (add-likes-up likes)
		  ]
		;(pprint user)
		;(pprint follower-urls)
		;(pprint (count follower-shot-urls))
		;(pprint (map (comp user-votes (partial fetch-sequence ["user" "id"])) (take 2 all-like-urls)))
		;(pprint @(api (second follower-shot-urls)))

		;(pprint likes)
		;(pprint counters)
		(pprint (top-10 counters))))


(defn -main []
	(println "Greetings!")
	(println "Look into task1 function in src/attendify_test_assignment/core.clj")
	(println "and task2 in test/attendify_test_assignment/core_test.clj")
	
	(shutdown-agents))
