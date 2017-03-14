(ns attendify-test-assignment.dribbble-test
	(:require [clojure.test :refer :all]
			  [attendify-test-assignment.dribbble :as dribbble]
			  [attendify-test-assignment.transport :as transport]))

(def Izabella {"comments_received_count" 6,
			   "bio"                     "",
			   "shots_count"             5,
			   "can_upload_shot"         true,
			   "followings_count"        4,
			   "followers_url"           "https://api.dribbble.com/v1/users/1579965/followers",
			   "likes_received_count"    98,
			   "avatar_url"
										 "https://d13yacurqjgara.cloudfront.net/users/1579965/avatars/normal/223d5afb64a26950603e8c2be7864e0a.jpg?1488357624",
			   "username"                "IZAVINCZE",
			   "buckets_count"           0,
			   "pro"                     false,
			   "id"                      1579965,
			   "projects_url"            "https://api.dribbble.com/v1/users/1579965/projects",
			   "name"                    "Izabella Vincze",
			   "likes_url"               "https://api.dribbble.com/v1/users/1579965/likes",
			   "location"                nil,
			   "buckets_url"             "https://api.dribbble.com/v1/users/1579965/buckets",
			   "updated_at"              "2017-03-12T08:57:56Z",
			   "html_url"                "https://dribbble.com/IZAVINCZE",
			   "teams_count"             0,
			   "links"                   {"web" "http://vizart.hu"},
			   "likes_count"             16,
			   "shots_url"               "https://api.dribbble.com/v1/users/1579965/shots",
			   "following_url"           "https://api.dribbble.com/v1/users/1579965/following",
			   "type"                    "Player",
			   "created_at"              "2017-01-31T17:24:03Z",
			   "teams_url"               "https://api.dribbble.com/v1/users/1579965/teams",
			   "followers_count"         4,
			   "rebounds_received_count" 0,
			   "projects_count"          0})



(deftest Dribbble
	(testing "Top 10 returns"
		(let [liker-counters (apply list (concat
											 (repeat 4 1251)
											 (repeat 200 1252)
											 (repeat 200 1253)
											 (repeat 5 1254)
											 (repeat 200 1255)
											 (repeat 200 1256)
											 (repeat 200 1257)
											 (repeat 200 1258)
											 (repeat 6 1259)
											 (repeat 200 1260)
											 (repeat 200 1261)
											 (repeat 200 1262)
											 (repeat 200 1263)
											 ))
			  result (dribbble/top-ten liker-counters)]

			(is (seq? result))
			(is (every? vector? result))
			(is (every? (comp (partial <= 100) second) result)))
		)

	(testing "follower-urls"
		(is (= "https://api.dribbble.com/v1/users/1579965/followers" (dribbble/user-followers-url Izabella))))
	)
