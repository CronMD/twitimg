(ns twitter-images.core 
	(:require [clj-http.client :as client]
            [clojure.data.json :as json])
	(:gen-class)
)

(defn get-images [html]
	(map #(str "https://pbs.twimg.com/media/" (% 1)) (re-seq #"src=\"https:\/\/pbs\.twimg\.com\/media\/(.*)\"\salt" html)) ;"
)

(defn get-position [html]
	((last (re-seq #"data-tweet-id=\"(.*)\"" html)) 1) ; "	
)

(defn get-media [username]
	(let [
			url (str "https://twitter.com/" username "/media")
			html ((client/get url) :body)
			images-list (get-images html)
			images (into [] images-list)
			max-position (get-position html)
		 ]	
		 (loop [pos max-position images images]
		 	(let [
		 			url (str "https://twitter.com/i/profiles/show/" username "/media_timeline?include_available_features=1&include_entities=1&max_position=" pos "&reset_error_state=false")
		 		 	data (json/read-str ((client/get url) :body))
		 		 	images (concat images (get-images (data "items_html")))
		 		 ]

		 		 (if (true? (data "has_more_items"))
		 		 	(recur (data "min_position") images)
		 		 	images
		 		 )
		 	)
		 )
	)
)

(defn -main[username & args] 
	(let [
			large (some #(= % "large") args)
		 ]
		(doseq [url (get-media username)]
			(if (nil? large)
				(println url)
				(println (str url ":large"))
			)
		)
	)
)