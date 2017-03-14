(ns attendify-test-assignment.dribbble
	(:require
		[clojure.core.async :as async :refer [<! >! <!! >!! timeout chan alt! go go-loop take!]]))


(defn merge+
	"Takes a *channel* of source channels and returns a channel which
	contains all values taken from them. The returned channel will be
	unbuffered by default, or a buf-or-n can be supplied. The channel
	will close after all the source channels have closed."
	([in-ch] (merge+ in-ch nil))
	([in-ch buf-or-n]
	 (let [out-ch (async/chan buf-or-n)]
		 (async/go-loop [cs [in-ch]]
			 (if-not (empty? cs)
				 (let [[v c] (async/alts! cs)]
					 (cond
						 (nil? v)
						 (recur (filterv #(not= c %) cs))

						 (= c in-ch)
						 (recur (conj cs v))

						 :else
						 (do
							 (async/>! out-ch v)
							 (recur cs))))
				 (async/close! out-ch)))
		 out-ch)))


(defn user-followers-url [user]
	(get user "followers_url"))


(defn get-follower-urls [user]
	(get user "followers_url"))

(defn get-follower-shots [follower]
	(get-in follower ["follower" "shots_url"]))

(defn get-follower-likes-url [follower]
	(get-in follower ["likes_url"]))


(defn- add-likes-up [likes]
	(assert (or (vector? likes) (list? likes)))
	(->> likes
		 (map #(apply hash-map [% 1]))
		 (apply merge-with +)))


(defn top-ten [likes]
	(assert (or (vector? likes) (list? likes)))
	(take-last 10 (sort-by val (add-likes-up likes))))


(defprotocol DribbbleApi
	(get-user [this id])
	(fetch-sequence [this url])
	(top-ten-likers [this user-id]))

(defrecord Dribbble [transport]
	DribbbleApi

	(get-user [this id]
		(async/map :body [(.GET transport (str "/v1/users/" id))] 100))

	(fetch-sequence [this url]
		(let [out (chan)]
			(go-loop [urls (flatten [url])]
				(if (empty? urls)
					(async/close! out)
					(do
						(let [response (<! (.GET transport (first urls)))
							  next (get-in response [:links :next :href])]

							(when response
								(when-let [furhter-links (:body response)]
									(doseq [link furhter-links]
										(>! out link))))

							(recur (filterv some? (cons next (rest urls))))
							))
					))
			out))

	(top-ten-likers [this user-id]
		(top-ten
			(<!!
				(->>
					(get-user this user-id)

					vector
					(async/map get-follower-urls)

					vector
					(async/map #(fetch-sequence this %))
					merge+

					vector
					(async/map get-follower-shots)

					vector
					(async/map #(fetch-sequence this %))

					merge+

					vector
					(async/map get-follower-likes-url)

					vector
					(async/map #(fetch-sequence this %))

					merge+

					vector
					(async/map #(get-in % ["user" "id"]))

					(async/into [])
					))))
	)


(defn create-dribbble-api [transport]
	(->Dribbble transport))
