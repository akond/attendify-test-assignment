(ns attendify-test-assignment.transport
	(:require [clojure.string :as string]
			  [clojure.core.async :as async]
			  [cemerick.url :as url]
			  [org.httpkit.client :as http]
			  [cheshire.core :as cheshire]))

(defprotocol TransportApi
	(fqn [this path])
	(GET [this path]))

(defrecord Transport [host access-token throttle]
	TransportApi
	(fqn [this path]
		(let [u (try (url/url path)
					 (catch Exception e (url/url (str (.toString host) "/" path))))
			  fqn? (= (:host u) (:host host))]
			(if fqn?
				(.toString u)
				(str (.toString host)
					 (:path u)
					 (when (:query u)
						 (str "?" (url/map->query (:query u))))))))

	(GET [this path]
		(let* [out (async/chan)
			   get-impl (fn GetImpelemntation [a]
							(http/get
								(.fqn this path)
								{:insecure? true
								 :headers   {"Authorization" (str "Bearer " access-token)
											 "Accept"        "application/json"}}

								(fn get-callback [{:keys [status headers body error] :as response}]
									(if (= status 200)
										(let [value (update response :body cheshire/parse-string)]
											(assert (some? value))
											(async/put! out value))
										(do
											(println "Got an error" status)
											(async/put! out false)))
									(async/close! out)
									)))]

			(if-let [c throttle]
				(async/take! c get-impl true)
				(get-impl false))

			out)))


(defn create-throttle [msec]
	(let [tokens (async/chan)]
		(async/go-loop []
			(async/>! tokens :token)
			(async/<! (async/timeout msec))
			(recur))
		tokens))


(defn create-transport [host access-token throttle]
	(assert (or (string/starts-with? host "http://")
				(string/starts-with? host "https://")))

	(->Transport (url/url host) access-token throttle))
