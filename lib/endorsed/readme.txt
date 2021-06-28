sun java se 6內建jaxb 2.0
所以使用jaxb2.1時需要替換調javase 6內建的api
所以create 此目錄放置2.1的api
並於執行時加上
-Djava.endorsed.dirs="lib\endorsed"
可解決此問題