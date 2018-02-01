(ns attendify-test-assignment.dribble-manifold
	(:require [org.httpkit.client :as http]
			  [cheshire.core :as cheshire]
			  [manifold.deferred]
			  [clojure.walk]))

(defn- add-likes-up [likes]
	(assert (or (vector? likes) (list? likes)))
	(->> likes
		 (map #(apply hash-map [% 1]))
		 (apply merge-with +)))


(defn top-ten [likes]
	(assert (or (vector? likes) (list? likes)))
	(take-last 10 (sort-by val (add-likes-up likes))))


(defn fetch-dribbble [access-token url]
	(let [result (manifold.deferred/deferred)
		  url (if (clojure.string/starts-with? url "http") url (str "https://api.dribbble.com/v1" url))]
		(http/get
			url
			{:insecure? true
			 :headers   {"Authorization" (str "Bearer " access-token)
						 "Accept"        "application/json"}}
			#(manifold.deferred/success! result
										 (-> %
											 :body
											 cheshire/parse-string
											 clojure.walk/keywordize-keys)))
		result))

(defn top-ten-likers [dribble id]
	(assert (fn? dribble))
	(top-ten
		(vec (->> [id]
				  (manifold.stream/->source)
				  (manifold.stream/map (partial str "/users/"))
				  (manifold.stream/map dribble)
				  (manifold.stream/realize-each)
				  (manifold.stream/map :followers_url)

				  (manifold.stream/map dribble)
				  (manifold.stream/realize-each)
				  (manifold.stream/map #(map :follower %))
				  (manifold.stream/mapcat #(map :shots_url %))

				  (manifold.stream/map dribble)
				  (manifold.stream/realize-each)
				  (manifold.stream/mapcat #(map :likes_url %))

				  (manifold.stream/map dribble)
				  (manifold.stream/realize-each)
				  (manifold.stream/map #(map :user %))
				  (manifold.stream/mapcat #(map :id %))

				  (manifold.stream/stream->seq)))))
